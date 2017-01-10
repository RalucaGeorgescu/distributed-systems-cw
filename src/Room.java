import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.DefaultListModel;

public class Room implements Serializable{
	private String roomName;
	private String owner;
	private ArrayList<String> connectedUsers;
	private static final long serialVersionUID = 7526471155622776147L;
	
	public Room(String roomName, String owner, ArrayList<String> connectedUsers){
		this.roomName = roomName;
		this.owner = owner;
		this.connectedUsers = connectedUsers;
	}
	
	public String getRoomName(){
		return roomName;
	}
	
	public String getOwner(){
		return owner;
	}
	
	public ArrayList<String> getConnectedUsers(){
		return connectedUsers;
	}
	
	public void addUser(String username){
		connectedUsers.add(username);
		System.out.println(connectedUsers);
	}
	
	public void removeUser(String username){
		int indexToRemove=0;
		for(int i=0;i<connectedUsers.size();i++){
			if(connectedUsers.get(i).equals(username)){
				indexToRemove = i;
				break;
			}
		}
		this.connectedUsers.remove(indexToRemove);
	}
}
