package org.cloudgraph.examples.wikicorpus.index;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.cloudgraph.examples.corpus.parse.Dependency;
import org.cloudgraph.examples.corpus.parse.DependencySet;
import org.cloudgraph.examples.corpus.parse.Document;
import org.cloudgraph.examples.corpus.parse.PageParse;
import org.cloudgraph.examples.corpus.parse.Sentence;
import org.cloudgraph.examples.corpus.parse.WordDependency;
import org.cloudgraph.examples.corpus.parse.WordRelation;
import org.cloudgraph.examples.corpus.parse.WordRelationType;
import org.cloudgraph.examples.corpus.parse.query.QDocument;
import org.cloudgraph.examples.corpus.parse.query.QPageParse;
import org.cloudgraph.hbase.graph.MetricCollector;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphServiceDelegate;
import org.cloudgraph.hbase.mapreduce.GraphWritable;
import org.plasma.query.Query;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataGraphVisitor;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.plasma.sdo.xml.StreamUnmarshaller;
import org.plasma.sdo.xml.UnmarshallerException;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

public class ParseTreeWriterMapper extends GraphMapper<Text, IntWritable> {
	private static Log log = LogFactory.getLog(ParseTreeWriterMapper.class);

  	private DefaultOptions unmarshalOptions; 
	private StreamUnmarshaller unmarshaler;
	private GraphServiceDelegate service;

	public ParseTreeWriterMapper() {
		this.service = new GraphServiceDelegate();
        this.unmarshalOptions = new DefaultOptions(Document.NAMESPACE_URI);
        this.unmarshalOptions.setRootNamespacePrefix("ns1");
        this.unmarshalOptions.setValidate(false);
        this.unmarshalOptions.setFailOnValidationError(false);
        this.unmarshaler = 
        		new StreamUnmarshaller(this.unmarshalOptions, null);       
	}

	/** Counter enumeration to count the actual rows. */
	public static enum Counters {
		PAGES_STARTED, PAGES_PROCESSED, PAGES_IGNORED, PAGES_ALREADY_EXIST, PAGES_FAILED,
		NLP_NODES_CREATED,
	}	

	@Override
	public void map(ImmutableBytesWritable row, GraphWritable graph,
			Context context) throws IOException {
		
		context.getCounter(Counters.PAGES_STARTED).increment(1);
		
		try {
			
			PageParse pageParse = (PageParse) graph.getDataGraph().getRootObject();
			
			DataGraph[] graphs = this.service.find(createDocumentExistsQuery(pageParse.getPageId()), context);
			if (graphs != null && graphs.length > 0) {
				context.getCounter(Counters.PAGES_ALREADY_EXIST).increment(1);
				context.getCounter(Counters.PAGES_IGNORED).increment(1);
				return;
			}						

			// go ahead and pull the XML, can be huge
			graphs = this.service.find(createPageParseXmlQuery(pageParse.getPageId()), context);
			pageParse = (PageParse)graphs[0].getRootObject();
			
			log.info(pageParse.getPageTitle());
						
			String bodyXml = pageParse.getXml();
			Document parseDoc = deserialize(bodyXml, Document.NAMESPACE_URI);
			parseDoc.setPageId(pageParse.getPageId());
			parseDoc.setPageTitle(pageParse.getPageTitle());
			MetricCollector collector = new MetricCollector();
			parseDoc.accept(collector);
			log.info("unmarsheled " + collector.getCount() + " NLP nodes");			 
			
			Document reducedDoc = createReducedDocument(parseDoc, context);
			
			collector = new MetricCollector();
			reducedDoc.accept(collector);
			context.getCounter(Counters.NLP_NODES_CREATED).increment(collector.getCount());
			log.info("created " + collector.getCount() + " NLP nodes");
			
			this.commit(reducedDoc.getDataGraph(), context);
			
			context.getCounter(Counters.PAGES_PROCESSED).increment(1);

		} catch (Throwable t) {
			context.getCounter(Counters.PAGES_FAILED).increment(1);
			log.info(t.getMessage(), t);
		}
	}
	
	private Document createReducedDocument(Document source, Context context) throws IOException {
		DataGraph docDataGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
		docDataGraph.getChangeSummary().beginLogging();
		Type rootType = PlasmaTypeHelper.INSTANCE.getType(Document.class);
		Document target = (Document) docDataGraph.createRootObject(rootType);
		target.setPageId(source.getPageId());
		target.setPageTitle(source.getPageTitle());
		int maxNodes = 500;
		int newNodeCount = 0;
		
		for (Sentence sourceSentnece : source.getSentence()) {
			Sentence targetSentence = target.createSentence();
			newNodeCount++;
			targetSentence.setId(sourceSentnece.getId());
			if (sourceSentnece.isSetParse())
			    targetSentence.setParse(sourceSentnece.getParse());
			targetSentence.setCharacterOffsetBegin(sourceSentnece.getCharacterOffsetBegin());
			targetSentence.setCharacterOffsetEnd(sourceSentnece.getCharacterOffsetEnd());
			if (sourceSentnece.isSetText())
			    targetSentence.setText(sourceSentnece.getText());
			if (sourceSentnece.isSetXml())
			    targetSentence.setXml(sourceSentnece.getXml());
			
			for (DependencySet sourceDSet : sourceSentnece.getDependencySet()) {
				for (Dependency sourceDep : sourceDSet.getDependency()) {
					WordRelation sourceGovernor = getGovernor(sourceDep.getRelation());	
					if (sourceGovernor != null) {
					    WordDependency governorWordDep = targetSentence.createWordDependency();
					    copy(governorWordDep, WordRelationType.GOVERNOR, sourceGovernor, sourceDep, sourceDSet);
						newNodeCount++;
					}
					else
						log.debug("no governor relation found - continuing");
					
					WordRelation sourceDependent = getDependent(sourceDep.getRelation());	
					if (sourceDependent != null) {
						WordDependency depententWordDep = targetSentence.createWordDependency();
						copy(depententWordDep, WordRelationType.DEPENDENT, sourceDependent, sourceDep, sourceDSet);
						newNodeCount++;
					}
					else
						log.debug("no dependent relation found - continuing");
				}
			}
			
			if (newNodeCount > maxNodes) {
				log.info("comitting " + newNodeCount + " NLP nodes");
				this.service.commit(target.getDataGraph(), context);
				newNodeCount = 0;
			}
		}
		
		return target;
	}
	
	private void copy(WordDependency targetWordDep, WordRelationType relationType, WordRelation sourceRelation, Dependency sourceDep, DependencySet sourceDSet) {
		targetWordDep.setRepresentation(sourceDSet.getRepresentation());
		targetWordDep.setType_(sourceDep.getType_());
		targetWordDep.setRelationType(relationType.getInstanceName());
		targetWordDep.setId(sourceRelation.getNode().getId());
		targetWordDep.setWord(sourceRelation.getNode().getWord());
		if (sourceRelation.getNode().isSetLemma())
		    targetWordDep.setLemma(sourceRelation.getNode().getLemma());
		if (sourceRelation.getNode().isSetPos())
		    targetWordDep.setPos(sourceRelation.getNode().getPos());
		targetWordDep.setCharacterOffsetBegin(sourceRelation.getNode().getCharacterOffsetBegin());
		targetWordDep.setCharacterOffsetEnd(sourceRelation.getNode().getCharacterOffsetEnd());
		if (sourceRelation.getNode().isSetNamedEntity())
		    targetWordDep.setNamedEntity(sourceRelation.getNode().getNamedEntity());
		if (sourceRelation.getNode().isSetNormalizedNamedEntity())
		    targetWordDep.setNormalizedNamedEntity(sourceRelation.getNode().getNormalizedNamedEntity());		
	}
	
	private WordRelation getGovernor(WordRelation[] relations) {
		for (WordRelation relation : relations) {
			if (WordRelationType.GOVERNOR.getInstanceName().equals(relation.getRelationType()))
				return relation;
		}
		return null;	
	}
	
	private WordRelation getDependent(WordRelation[] relations) {
		for (WordRelation relation : relations) {
			if (WordRelationType.DEPENDENT.getInstanceName().equals(relation.getRelationType()))
				return relation;
		}
		return null;	
	}
	
	private Query createPageParseXmlQuery(int id) {
		QPageParse query = QPageParse.newQuery();
		query.select(query.pageTitle())
	         .select(query.pageId())
	         .select(query.xml())
	    ;
		query.where(query.pageId().eq(id));
		return query;		
	}
	
	private Query createDocumentExistsQuery(int id) {
		QDocument query = QDocument.newQuery();
		query.select(query.pageId())
	    ;
		query.where(query.pageId().eq(id));
		return query;		
	}
	
	private Document deserialize(String xml, String uri) throws IOException {

     	ByteArrayInputStream xmlloadis = new ByteArrayInputStream(xml.getBytes());
     	try {
			this.unmarshaler.unmarshal(xmlloadis);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		} catch (UnmarshallerException e) {
			throw new IOException(e);
		}
     	XMLDocument doc = this.unmarshaler.getResult();
     	doc.setNoNamespaceSchemaLocation(null);
     	
		return (Document)doc.getRootObject();		
	}
}
