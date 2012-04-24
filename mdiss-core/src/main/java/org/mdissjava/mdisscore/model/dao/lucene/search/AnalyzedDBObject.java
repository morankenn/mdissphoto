package org.mdissjava.mdisscore.model.dao.lucene.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import com.mongodb.BasicDBObject;

public class AnalyzedDBObject extends BasicDBObject {

	private static final long serialVersionUID = 5414250287587698127L;

	public static enum Condition {ALL,IN}

	   private Analyzer analyzer;

	   public AnalyzedDBObject(Analyzer analyzer) {
	       this.analyzer = analyzer;
	   }

	   public AnalyzedDBObject createQuery(String name,String text) throws IOException {
	       return createQuery(name, text, Condition.ALL);
	   }

	   public AnalyzedDBObject createQuery(String name,String text,Condition condition) throws IOException {
	       List<String> tokens = tokenize(analyzer.tokenStream(name, new StringReader(text)));

	       append(name,new BasicDBObject(
	               String.format("$%s",condition.toString().toLowerCase()),
	               tokens.toArray(new String[0])));
	       return this;
	   }

	   public AnalyzedDBObject appendAndAnalyzeFullText(String name, String text)
	           throws IOException {
	       append(name,
	               tokenize(analyzer.tokenStream(name, new StringReader(text))));
	       return this;
	   }

	   private List<String> tokenize(TokenStream stream) throws IOException {
	       List<String> tokens = new ArrayList<String>();
	       TermAttribute term = (TermAttribute) stream
	               .addAttribute(TermAttribute.class);
	       while (stream.incrementToken()) {
	           tokens.add(term.term());
	       }
	       return tokens;
	   }
	
}
