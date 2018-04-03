package indexing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import constant.Constant;

public class Indexer {

	public static void main(String[] args) {
		try {
			// create Directory instance
			Directory directory = FSDirectory.open(Constant.INDEX_LOCATION);
			// analyzer with the default stop words
			Analyzer analyzer = new StandardAnalyzer();
			// IndexWriter Configuration
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
			indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
			// create IndexWriter and writes new index files to the directory
			IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
			indexDocs(Constant.FILE_LOCATION, indexWriter);
			// close writer
			indexWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void indexDocs(Path path, IndexWriter indexWriter) throws IOException {
		if (Files.isDirectory(path)) {
			// Iterate directory
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					// Index this file
					indexDoc(file, indexWriter, attrs.lastModifiedTime().toMillis());
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			// Index this file
			indexDoc(path, indexWriter, Files.getLastModifiedTime(path).toMillis());
		}
	}

	public static void indexDoc(Path file, IndexWriter indexWriter, long lastModified) {
		try (InputStream inputStream = Files.newInputStream(file)) {
			// Create lucene Document
			Document document = new Document();
			document.add(new StringField("path", file.toString(), Field.Store.YES));
			document.add(new LongPoint("modified", lastModified));
			document.add(new TextField("contents", new String(Files.readAllBytes(file)), Field.Store.YES));
			// Updates a document by first deleting the document(s) containing <code>term</code> and then adding the new document. 
			// The delete and then add are atomic as seen by a reader on the same index
			indexWriter.updateDocument(new Term("path", file.toString()), document);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
