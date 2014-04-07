package org.cloudgraph.examples.wikicorpus.index;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;

import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLDOMParser;

public class WordMapper extends Mapper<LongWritable, Text, NullWritable, Text> implements PageCallbackHandler {
	private static Log log = LogFactory.getLog(WordMapper.class);


	private Context context;

	public WordMapper() {
	}

	/** Counter enumeration to count the actual rows. */
	public static enum Counters {
		PAGES_INPUT,
		PAGES_SUCCESS,
		PAGES_FAILED,
		PAGES_PARSE_SUCCESS,
		PAGES_PARSE_FAILED,
		PAGES_IGNORED,
		REDIRECT_PAGES_IGNORED,
		FILE_PAGES_IGNORED,
		WORDS,
	}
	
	@Override
	public void map(LongWritable key, Text value1, Context context) throws IOException {
		
		context.getCounter(Counters.PAGES_INPUT).increment(1);
		try {
		this.context = context;
			//log.info(value1.toString());
			ByteArrayInputStream is = new ByteArrayInputStream(value1.getBytes());
			
			WikiXMLDOMParser parser = new WikiXMLDOMParser(is);
			parser.setPageCallback(this);
			parser.parse();        		
		} catch (Exception ex) {
			log.error(ex);
		}
		
	}

	@Override
	public void process(WikiPage wikiPage) {
		try {
			
			String redirectPage = wikiPage.getRedirectPage();			

			// skip parsing
			if (redirectPage != null && redirectPage.length() > 0)
			{
				context.getCounter(Counters.REDIRECT_PAGES_IGNORED).increment(1);
				return; // have no text
			}					
			if (wikiPage.getTitle().startsWith("File:")) {
				context.getCounter(Counters.FILE_PAGES_IGNORED).increment(1);
				return; // have no text
			}			
			
			byte[] bytes = wikiPage.getText().getBytes(Charset.forName("UTF-8"));
			String plainText = new String(bytes, "UTF-8");
			if (plainText == null || plainText.trim().length() == 0) {
				context.getCounter(Counters.PAGES_IGNORED).increment(1);
				return;
			}
			Map<String, Long> map = collect(plainText, context);			
			
			context.getCounter(Counters.PAGES_SUCCESS).increment(1);
		} catch (Exception ex) {
			context.getCounter(Counters.PAGES_FAILED).increment(1);
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	private Map<String, Long> collect(String text, Context context) throws IOException
	{
		Map<String, Long> result = new HashMap<String, Long>();
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);     
		StringReader in = new StringReader(text);
		TokenStream ts = analyzer.tokenStream("body", in);
		OffsetAttribute offsetAttribute = ts.addAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
		ts.reset();
		while (ts.incrementToken()) {
			context.getCounter(Counters.WORDS).increment(1);
			int startOffset = offsetAttribute.startOffset();
		    int endOffset = offsetAttribute.endOffset();
		    String term = charTermAttribute.toString();
		    Long count = result.get(term);
			if (count == null) 
				count = new Long(1);
			else 
				count = new Long(count.longValue() + 1);	
			result.put(term, count);
		}
		analyzer.close();
		
		return result;
	}
	 
	
}
