package org.cloudgraph.examples.wikicorpus.service.corpus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wikicorpus.service.QueueAdapter;
import org.cloudgraph.examples.corpus.parse.Dependency;
import org.cloudgraph.examples.corpus.parse.Node;

public class Sentence extends QueueAdapter {
	private static Log log = LogFactory.getLog(Sentence.class);
    protected Dependency dependency;
    protected Node node;
    private Sentence() {}
    protected Sentence(Dependency dependency, Node node) {
		super();
		this.dependency = dependency;
		this.node = node;
	}
    
    public boolean getIsGovernor() {
    	return this instanceof GovernorSentence;
    }
    
    public boolean getIsDependent() {
    	return this instanceof DependentSentence;
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
