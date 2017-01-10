import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import net.jini.core.entry.Entry;

public class MessageList implements Entry, Serializable{
	public ArrayList<Message> messageList;
	private static final long serialVersionUID = 7526471155622776147L;
	
	public MessageList(){
		//Empty constructor
	}
	
	public void initializeList(){
		messageList = new ArrayList<Message>();
	}
	
	public void addMessage(Message msg){
		messageList.add(msg);
	}
	
	public ArrayList<Message> getAllMessagesForRoom(String roomName){
		ArrayList<Message> roomMessages = new ArrayList<Message>();
		for(Message msg : messageList){
			if(msg.roomName.equals(roomName)){
				roomMessages.add(msg);
			}
		}
		return roomMessages;
	}
	
	public void deleteMessagesForRoom(String roomName){
		ArrayList<Message> tobeDeleted = new ArrayList<Message>();
		for(Message msg : messageList){
			if(msg.roomName.equals(roomName)){
				tobeDeleted.add(msg);
			}
		}
		messageList.removeAll(tobeDeleted);
		System.out.println("I deleted all messages for room " + roomName);
	}
	
	public ArrayList<String> findMessageByRoom(String roomName){
		ArrayList<String> roomMessages = new ArrayList<String>();
		for(Message msg : messageList){
			if(msg.roomName.equals(roomName)){
				roomMessages.add(msg.contents);
			}
		}
		return roomMessages;
	}
	
	public String getLastMessageByRoom(String roomName){
		String lastMessage = null;
		ArrayList<String> roomMessages = findMessageByRoom(roomName);
		if(roomMessages != null){
			lastMessage = roomMessages.get(roomMessages.size()-1);
		}
		
		return lastMessage;
	}
	
	public String getLastSenderByRoom(String roomName){
		String lastSender;
		ArrayList<String> roomMessages = new ArrayList<String>();
		for(Message msg : messageList){
			if(msg.roomName.equals(roomName)){
				roomMessages.add(msg.sender);
			}
		}
		lastSender = roomMessages.get(roomMessages.size()-1);
		return lastSender;
	}
	
	public Boolean getPrivateFlagByRoom(String roomName){
		Boolean isPrivate;
		ArrayList<Boolean> roomMessages = new ArrayList<Boolean>();
		for(Message msg : messageList){
			if(msg.roomName.equals(roomName)){
				roomMessages.add(msg.isPrivate());
			}
		}
		isPrivate = roomMessages.get(roomMessages.size()-1);
		return isPrivate;
	}
	
	public DefaultListModel<String> getDefaultList(){
		DefaultListModel<String> list = new DefaultListModel<String>();
		for(Message msg : messageList){
			list.addElement(msg.contents);
		}
		return list;
	}
}
