package org.cloudgraph.examples.corpus.nlp;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SentenceUtil {
	public static SentenceBreak[] getBreaks(String plainText) {
		BreakIterator iterator =
                BreakIterator.getSentenceInstance(Locale.US);
		iterator.setText(plainText);
		List<SentenceBreak> list = new ArrayList<SentenceBreak>();

        int lastIndex = iterator.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = iterator.next();

            if (lastIndex == BreakIterator.DONE)
            	continue;
            list.add(new SentenceBreak(firstIndex, lastIndex)); 
        }
        SentenceBreak[] result = new SentenceBreak[list.size()];
        list.toArray(result);
        return result;
 	}

}
