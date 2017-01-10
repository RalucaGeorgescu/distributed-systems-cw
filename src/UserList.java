import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultListModel;

import net.jini.core.entry.Entry;

public class UserList implements Entry{
	public ArrayList<User> users;
	private static final long serialVersionUID = 7526471155622776147L;
	
	public UserList(){
		//Empty constructor
	}
	
	public void initializeArrayList(){
		users = new ArrayList<User>();
	}
	
	public ArrayList<User> getUserList(){
		return users;
	}
	
	public void addUser(User user){
		users.add(user);
	}
	
	public void removeUser(User user){
		users.remove(user);
	}
	
	public boolean userExists(User user){
		return users.contains(user);
	}
	
	public User getUserByName(String userName){
		for(User user : users){
			if(user.getUsername().equals(userName)){
				return user;
			}
		}
		
		return null;
	}
	
	public void toggleConnected(String userName, char[] password){
		User user = getUserByNameAndPassword(userName, password);
		if(user.getConnected()){
			user.setConnected(false);
		} else {
			user.setConnected(true);
		}
	}
	
	public User getUserByNameAndPassword(String userName, char[] password){
		for(User user : users){
			if(user.getUsername().equals(userName) && Arrays.equals(user.getPassword(),password)){
				return user;
			}
		}
		
		return null;
	}
	
	public void printUserNames(){
		for(User user : users){
			System.out.println("User: " + user.getUsername());
		}
	}
	
	public DefaultListModel<String> getConnectedUsers(){
		DefaultListModel<String> list = new DefaultListModel<String>();
		for(User user : users){
			if(user.getConnected()){
				list.addElement(user.getUsername());
			}
		}
		return list;
	}
	
	public DefaultListModel<String> getDefaultList(){
		DefaultListModel<String> list = new DefaultListModel<String>();
		for(User user : users){
			list.addElement(user.getUsername());
		}
		return list;
	}
}
