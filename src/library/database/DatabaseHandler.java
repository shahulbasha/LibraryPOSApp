package library.database;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoServerException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class DatabaseHandler {
	static DatabaseHandler handler=null;
	private MongoClient mongoClient=new MongoClient("localhost",27017);
	
	private DatabaseHandler() {
		setUpBookCollection();
		setUpMemberCollection();
	}
	
	public static DatabaseHandler getInstance() {
		if(handler==null) {
			return new DatabaseHandler();
		}
		return handler;
	}
	
	
	public MongoClient getMongoConnection() {
		return this.mongoClient;
	}
	
	//only database used for this entire application
	public MongoDatabase getMongoDatabase() {
		return this.mongoClient.getDatabase("Library");
	}
	
	//return the collection if it exists or create it and return it
	public MongoCollection<Document> setUpBookCollection() {
		MongoCollection<Document> collection = this.getMongoDatabase().getCollection("book");
		return collection;
	}
	
	public MongoCollection<Document> setUpMemberCollection() {
		MongoCollection<Document> collection = this.getMongoDatabase().getCollection("member");
		return collection;
	}
	
/*	public void retrieveMemberById(String memberId){
		MongoCollection<Document> memberCollection = setUpMemberCollection();
		Document doc = memberCollection.find(eq("memberId",memberId)).first();
		doc.toJson();
		
	}
	
	public void retrieveBookById(String bookId){
		MongoCollection<Document> setUpBookCollection = setUpBookCollection();
	}*/
	
	//method checks for availability of a particular document in a collection
	public boolean executeAction(String bookTitle) {
		MongoCollection<Document> collection=setUpBookCollection();
		for (Document doc :  collection.find()) {
			if(doc.containsKey(bookTitle)) {
				return true;
			}
		}
		return false;
	}
	
	public void executeQuery(String bookTitle) {
		
	}

	//get the book collection and insert the received record
	public boolean insertBook(Document document) {
		MongoCollection<Document> bookCollection = setUpBookCollection();
		try {
			bookCollection.createIndex(new Document("bookId",1),new IndexOptions().unique(true));
			bookCollection.insertOne(document);
			System.out.println("Collection Inserted"+bookCollection.count());
		} catch (MongoWriteException e) {
			System.out.println(e.getMessage());
			return false;
		}catch(MongoServerException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean insertMember(Document doc) {
		try {
			MongoCollection<Document> memberCollection = setUpMemberCollection();
			memberCollection.createIndex(new Document("memberID", 1), new IndexOptions().unique(true));
			memberCollection.insertOne(doc);
			System.out.println("Collection Inserted"+memberCollection.count());
		} catch (MongoWriteException e) {
			System.out.println(e.getMessage());
			return false;
		}catch(MongoServerException e) {
			e.printStackTrace();
		}

		return true;
	}

}
