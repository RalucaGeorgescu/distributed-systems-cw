import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.DefaultListModel;

import net.jini.core.entry.Entry;

public class RoomList implements Entry{
	public ArrayList<Room> rooms;
	private static final long serialVersionUID = 7526471155622776147L;
	
	public RoomList(){
		//Empty constructor
	}
	
	public void initializeArrayList(){
		rooms = new ArrayList<Room>();
	}
	
	public ArrayList<Room> getRooms(){
		return rooms;
	}
	
	public void addRoom(Room room){
		rooms.add(room);
	}
	
	public void removeRoom(Room room){
		rooms.remove(room);
	}
	
	public void deleteRoomByName(String roomName){
		if(rooms != null) {
			for(Room room : rooms){
				if(room.getRoomName().equals(roomName)){
					rooms.remove(room);
					break;
				}
			}
		}
	}
	
	public String getOwnerByRoomName(String roomName){
		if(rooms != null) {
			for(Room room : rooms){
				if(room.getRoomName().equals(roomName)){
					return room.getOwner();
				}
			}
		}
		
		return null;
	}
	
	public boolean roomExists(Room newRoom){
		if(rooms != null) {
			for(Room room : rooms){
				if(room.getRoomName().equals(newRoom.getRoomName())&&room.getOwner().equals(newRoom.getOwner())){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public String getRoomOwner(String roomName){
		if(rooms != null){
			for(Room room : rooms){
				if(room.getRoomName().equals(roomName)){
					return room.getOwner();
				}
			}
		}
		
		return null;
	}
	
	public Room getRoomByName(String roomName){
		if(rooms != null){
			for(Room room : rooms){
				if(room.getRoomName().equals(roomName)){
					return room;
				}
			}	
		}
		
		return null;
	}
	
	public void printRoomListElements(){
		for(Room room : rooms){
			System.out.println(room.getRoomName() + " has owner " + room.getOwner());
		}
	}
	
	public String getLastRoomName(){
		int lastIndex = rooms.size()-1;
		return rooms.get(lastIndex).getRoomName();
	}
	
	public DefaultListModel<String> getDefaultList(){
		DefaultListModel<String> list = new DefaultListModel<String>();
		if(rooms == null){
			System.out.println("I have no rooms here to display");
			list.addElement("No rooms to display yet. Create one!");
		} else {
			for(Room room : rooms){
				list.addElement(room.getRoomName());
			}
		}
		
		return list;
	}
}
