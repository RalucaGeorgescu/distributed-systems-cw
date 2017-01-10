import java.io.Serializable;

public class Message implements Serializable{
	public String contents;
	public String sender;
	public String roomName;
	private Boolean isPrivate;
    
    public Message() {
	   // No Arg constructor
    }

    public Message (String content, String sender, String roomName, Boolean privateflag) {
    	this.contents = content;
    	this.sender = sender;
    	this.roomName = roomName;
    	this.isPrivate = privateflag;
    } 
    
    public String displayMessage(){
    	return sender + " said: " + contents;
    }
    
    public Boolean isPrivate(){
    	return isPrivate;
    }
}
