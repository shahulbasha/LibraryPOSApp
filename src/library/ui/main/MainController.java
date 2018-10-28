package library.ui.main;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.effects.JFXDepthManager;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import library.database.DatabaseHandler;
import library.model.BookModel;
import library.model.IssueBookModel;
import library.model.MemberModel;

public class MainController implements Initializable{
    @FXML
    private HBox book_info;

    @FXML
    private HBox member_info;
    
    @FXML
    private TextField bookId;

    @FXML
    private Text bookName;

    @FXML
    private Text bookAuthor;

    @FXML
    private Text bookStatus;

    @FXML
    private TextField memberId;

    @FXML
    private Text memberName;

    @FXML
    private Text memberContact;
    
    @FXML
    private JFXTextField bookSubmitRenew;
    

    @FXML
    private ListView<String> issueRenewListView;
    


	@FXML
    void loadAddBook(ActionEvent event) {

		loadWindow("/library/ui/addbook/LibraryAddBook.fxml", "Add New Book");
    }

    @FXML
    void loadAddMember(ActionEvent event) {
		loadWindow("/library/ui/addmember/LibraryAddMember.fxml", "Add New Member");
    }

    @FXML
    void loadBookList(ActionEvent event) {
    	loadWindow("/library/ui/listbook/LibraryBookList.fxml", "View Book List");
    }

    @FXML
    void loadMemberList(ActionEvent event) {
    	loadWindow("/library/ui/listmember/LibraryMemberList.fxml", "View Member List");
    }

    @FXML
    void loadSettings(ActionEvent event) {

    }
    
    @FXML
    void loadMemberInfo(ActionEvent event) {
    	ObjectMapper mapper=new ObjectMapper();
    	String text = memberId.getText();
    	if(text!=null && !text.isEmpty()) {
    	MongoCollection<Document> memberCollection = DatabaseHandler.getInstance().setUpMemberCollection();
    	Document doc = memberCollection.find(eq("memberId",text)).first();
    	if(doc!=null) {
    	try {
			MemberModel memberModel = mapper.readValue(doc.toJson(), MemberModel.class);
			memberName.setText(memberModel.getMemberName());
			memberContact.setText(memberModel.getEmailId());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	}else {

			memberName.setText("No such Member Available");
			memberContact.setText("");
    	}
    	}
    }
    
    
    @FXML
    void loadBookInfo(ActionEvent event) {
    	ObjectMapper mapper=new ObjectMapper();
    	String text = bookId.getText();
    	if(text!=null && !text.isEmpty()) {
    	MongoCollection<Document> bookCollection = DatabaseHandler.getInstance().setUpBookCollection();
    	Document doc=bookCollection.find(eq("bookId",text)).first();
    	if(doc!=null) {
    	try {
			BookModel bookModel = mapper.readValue(doc.toJson(), BookModel.class);
			bookName.setText(bookModel.getBookTitle());
			bookAuthor.setText(bookModel.getAuthor());
			bookStatus.setText(bookModel.isAvailable()?"true":"false");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	}
    	else {
			bookName.setText("No such Book Available");
			bookAuthor.setText("");
			bookStatus.setText("");
    	}
    	}
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		JFXDepthManager.setDepth(book_info, 1);
		JFXDepthManager.setDepth(member_info, 1);
		
	}
	
	void loadWindow(String loc,String title) {
		try {
			System.out.println(loc+title);
			Parent root=FXMLLoader.load(getClass().getResource(loc));
			Stage stage=new Stage(StageStyle.DECORATED);
			Scene scene=new Scene(root);
			stage.setTitle(title);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

    @FXML
    void loadIssueBook(ActionEvent event) {

    
    	String bookIdValue=bookId.getText();
    	String memberIdValue=memberId.getText();
    	
    	IssueBookModel ibModel=new IssueBookModel();
    	ibModel.setBookId(bookIdValue);
    	ibModel.setMemberId(memberIdValue);
    	ibModel.setDate(new Date());
    	ibModel.setRenewCount(0);

    	
    	 DatabaseHandler dBhandler = DatabaseHandler.getInstance();
    	 
    	Alert alert=new Alert(AlertType.CONFIRMATION);
    	alert.setTitle("Issue Confirmation");
    	alert.setContentText("Are you sure you want to issue the book "+bookIdValue+" to "+memberIdValue+" ?");
    	Optional<ButtonType> response = alert.showAndWait();
    	
    	ObjectMapper mapper=new ObjectMapper();


    	
    	if(response.get()==ButtonType.OK) {
    	//	MongoCollection<Document> issueBookCollection = dBhandler.setUpIssueBookCollection();
    		MongoCollection<Document> bookCollection = dBhandler.setUpBookCollection();
    		try {
				Document doc = Document.parse(mapper.writeValueAsString(ibModel));
				boolean issueBook = dBhandler.issueBook(doc);
		    	if(issueBook) {
		    		//set Is available to false once book issue
		    		bookCollection.updateOne(eq("bookId",bookIdValue),new Document("$set",new Document("available",false)));
		    		Alert alert1=new Alert(AlertType.INFORMATION);
		    		alert1.setContentText("Book Issued Successfully");
		    		alert1.setHeaderText(null);
		    		alert1.showAndWait();
		    	}else {
		    		Alert alert2=new Alert(AlertType.ERROR);
		    		alert2.setContentText("Could not issue a Book at this time. Please try again later!!");
		    		alert2.setHeaderText(null);
		    		alert2.showAndWait(); 		
		    	}
				
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}else {
    		Alert alert3=new Alert(AlertType.ERROR);
    		alert3.setContentText("Issue operation Cancelled!!");
    		alert3.setHeaderText(null);
    		alert3.showAndWait(); 
    		
    	}
    	
    	

    }
    
    @FXML
    void loadBookSubmitRenewInfo(ActionEvent event) {
    	
        ObservableList<String> issueRenewData=FXCollections.observableArrayList();
        
    	String bookId=bookSubmitRenew.getText();
    	MongoCollection<Document> issueBookCollection = DatabaseHandler.getInstance().setUpIssueBookCollection();
    	MongoCollection<Document> memberCollection = DatabaseHandler.getInstance().setUpMemberCollection();
    	MongoCollection<Document> bookCollection = DatabaseHandler.getInstance().setUpBookCollection();
    	
    	System.out.println("input"+bookId);

    	ObjectMapper mapper=new ObjectMapper();


    	try {
        	Document doc = issueBookCollection.find(eq("bookId",bookId)).first();
        	System.out.println(doc.toJson());
        	Document doc1 = bookCollection.find(eq("bookId",bookId)).first();
        	System.out.println("****************************************");
			IssueBookModel issueBook = mapper.readValue(doc.toJson(), IssueBookModel.class);
			Document doc2 = memberCollection.find(eq("memberId",issueBook.getMemberId())).first();
			MemberModel memberModel = mapper.readValue(doc2.toJson(),MemberModel.class);
			BookModel bookModel = mapper.readValue(doc1.toJson(),BookModel.class);
			
			
			issueRenewData.add("Issue Date and Time is "+issueBook.getDate().toString());
			issueRenewData.add("Renew Count : "+issueBook.getRenewCount());
			issueRenewData.add("Book Information :");
			issueRenewData.add("Book ID : "+bookModel.getBookId());
			issueRenewData.add("Book Name : "+bookModel.getBookTitle());
			issueRenewData.add("Author : "+bookModel.getAuthor());
			issueRenewData.add("Publisher : "+bookModel.getPublisher());
			issueRenewData.add("Member Information :");
			issueRenewData.add("Member ID : "+issueBook.getMemberId());
			issueRenewData.add("Member Name : "+memberModel.getMemberName());
			issueRenewData.add("Member Contact : "+memberModel.getEmailId());
			
			issueRenewListView.getItems().setAll(issueRenewData);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

    }

}
