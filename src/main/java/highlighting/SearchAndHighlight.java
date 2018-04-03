package highlighting;

import java.io.IOException;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import constant.Constant;


public class SearchAndHighlight {

	public static void main(String[] args) {
		while (true) {
			try {
				// User input search keyword
				Scanner scanner = new Scanner(System.in);
				System.out.println("Input Your Keyword : ");
				String keyword = scanner.nextLine();
				
				// Get directory reference
				Directory directory = FSDirectory.open(Constant.INDEX_LOCATION);
				
				// Create index reader
				IndexReader indexReader = DirectoryReader.open(directory);
				
				// Create lucene searcher. It search over a single IndexReader.
				IndexSearcher indexSearcher = new IndexSearcher(indexReader);
				
				// create analyzer with the default stop words
				Analyzer analyzer = new StandardAnalyzer();
				
				// Query parser to be used for creating TermQuery
				QueryParser queryParser = new QueryParser("contents", analyzer);
				
				// Create the query from input keyword
				Query query = queryParser.parse(keyword);
				
				// Search the lucene documents
				TopDocs topDocs = indexSearcher.search(query, 10);
				
				
				/** Highlighter Code Start ****/
				// Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
				Formatter formatter = new SimpleHTMLFormatter();
				
				
				// It scores text fragments by the number of unique query terms found
				QueryScorer scorer = new QueryScorer(query);
				
				
				// Used to markup highlighted terms found in the best sections of a text
				Highlighter highlighter = new Highlighter(formatter, scorer);
				
				
				// It breaks text up into same-size texts but does not split up spans
				Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
				
				
				// Set fragmenter to highlighter
				highlighter.setTextFragmenter(fragmenter);
				
				// get array of ScoreDoc from topDocs
				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
				int length = scoreDocs.length;
				// Iterate over found results
				for (int i = 0; i < length; i++) {
					int docId = scoreDocs[i].doc;
					Document doc = indexSearcher.doc(docId);
					String title = doc.get("path");
					System.out.println("Path " + " : " + title);
					
					// Get stored text from found document
					String text = doc.get("contents");
					
					
					// Create token stream
					TokenStream tokenStream = TokenSources.getAnyTokenStream(indexReader, docId, "contents", analyzer);
					
					//Get highlighted text fragments
					String[] frags = highlighter.getBestFragments(tokenStream, text, 10);
					for (String frag : frags) {
						System.out.println("=======================");
						System.out.println(frag);
					}
				}
				directory.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (InvalidTokenOffsetsException e) {
				e.printStackTrace();
			}
		}
	}

}
