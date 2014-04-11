package org.cloudgraph.examples.wikicorpus.service.corpus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.corpus.parse.Dependency;
import org.cloudgraph.examples.corpus.parse.Node;
import org.cloudgraph.examples.corpus.parse.WordRelation;
import org.cloudgraph.examples.corpus.parse.WordRelationType;
import org.cloudgraph.examples.wikicorpus.service.QueueAdapter;

public class Sentence extends QueueAdapter {
	private static Log log = LogFactory.getLog(Sentence.class);
	protected WordRelation relation;
    protected Dependency dependency;
    protected Node node;
    private Sentence() {}
    public Sentence(WordRelation relation) {
		super();
		this.relation = relation;
		this.dependency = this.relation.getDependency();
		this.node = relation.getNode();
	}
    
    public boolean getIsGovernor() {
    	return WordRelationType.GOVERNOR.getInstanceName().equals(
    			this.relation.getRelationType());
    }
    
    public boolean getIsDependent() {
    	return WordRelationType.DEPENDENT.getInstanceName().equals(
    			this.relation.getRelationType());
    }
    
	public String getPOS() {
		return this.node.getPos();
	}
	
	public String getWord() {
		return this.node.getWord();
	}
	
	public String getLemma() {
		return this.node.getLemma();
	}

	public String getDependencyTypeDisplayName() {
		try {
		    return org.cloudgraph.examples.wikicorpus.service.corpus.Dependency.getDisplayableType(
				this.dependency.getType_());
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			return this.dependency.getType_();
		}
	}
	
	public String getDependencyTypeName() {
		return this.dependency.getType_();
	}
	
	public String getParse() {
		return this.dependency.getDependencySet().getSentence().getParse();
	}
	
	public String getText() {
		return this.dependency.getDependencySet().getSentence().getText();
	}
	
	public String getTextWithMarkup() {
		String text = this.dependency.getDependencySet().getSentence().getText();
		if (text != null) {
			//log.info(text);
			StringBuilder buf = new StringBuilder(text.length());
			int sentenceBegin = this.dependency.getDependencySet().getSentence().getCharacterOffsetBegin();
			int nodeBegin = this.node.getCharacterOffsetBegin();		
			int nodeEnd = this.node.getCharacterOffsetEnd();
			//log.info(sentenceBegin + "/" + nodeBegin + "/" + nodeEnd);
			
			try {
				buf.append(text.substring(0, nodeBegin));
				buf.append("<b>");
				buf.append(text.substring(nodeBegin, nodeEnd));
				buf.append("</b>");
				buf.append(text.substring(nodeEnd));
			}
			catch (StringIndexOutOfBoundsException e) {
				log.error(e.getMessage());
				return text;
			}
			catch (Exception e) {
				log.error(e.getMessage(), e);
				return text;
			}
			
			return buf.toString();
		}
		else
			return null;
	}
	
	public String getPageTitle() {
		return this.dependency.getDependencySet().getSentence().getDocument().getPage().getPageTitle();
	}
}
