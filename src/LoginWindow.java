import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import java.rmi.RemoteException;

import javax.swing.*;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;

public class LoginWindow extends JFrame implements RemoteEventListener{
	private JPanel loginPanel;
	private JLabel username, password;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JButton loginBtn;
	
	private DefaultListModel<String> list;
	private Boolean connected = false;
	private User currentUser;
	private JavaSpace space; 
	private TransactionManager mgr; 
	private RemoteEventListener listener;
	
	public UserList userList;
	public RoomList roomList;
	
	public LoginWindow(){
		super();
		space = SpaceUtils.getSpace();
		if(space == null){
			System.out.println("Space not found");
			System.exit(1);
		}
	
		initComponents();
		list = new DefaultListModel<String>();
		userList = new UserList();
		roomList = new RoomList();
		
		Exporter myExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(), false, true);
		
		try{
			listener = (RemoteEventListener) myExporter.export(this);
		} catch (Exception e){
			System.out.println("Listener was not exported correctly");
			e.printStackTrace();
		}
	}

	private void initComponents(){ 
		loginPanel = new JPanel();
		this.setTitle("Login");

		username = new JLabel();
		username.setText("Username:");
		
		password = new JLabel();
		password.setText("Password:");
		
		usernameField = new JTextField("",12);
		passwordField = new JPasswordField("",12);
		
		loginBtn = new JButton();
		loginBtn.setText("Login");
		loginBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt){
				//authenticate the user
				login(getUsername(), getPassword());
				
				//and open the ChatWindow if user is correct
				if(connected){				
					//pass on username and roomlist to chatwindow
					roomList = readRoomListFromSpace();
					ChatWindow chat = new ChatWindow(getUsername(), roomList);
					chat.setTitle("Chat Application - " + getUsername());
					chat.setVisible(true);
					setVisible(false);
				}
			}
		});
		
		JPanel userfields = new JPanel();
		userfields.add(username);
		userfields.add(usernameField);
		
		JPanel passfields = new JPanel();
		passfields.add(password);
		passfields.add(passwordField);
		
		Container cp = getContentPane();
		cp.setLayout (new BorderLayout ());
		loginPanel.add(loginBtn);
		
		cp.add(userfields, "North");
		cp.add(passfields, "Center");
		cp.add(loginPanel, "South");
		
		this.setLocationRelativeTo(null);
		this.setSize(300, 150);
		passfields.setBackground(new Color(218,243,236));
		userfields.setBackground(new Color(218,243,236));
		loginPanel.setBackground(new Color(218,243,236));
	}
	
	public String getUsername(){
		return usernameField.getText().trim();
	}
	
	public char[] getPassword(){
		return passwordField.getPassword();
	}
	
	public void login(String username, char[] password){
		if(!authenticate(username, password)){
			System.out.println("We could not find the details for you");
			return;
		}

		connected = true;
	}
	
	private boolean authenticate(String username, char[] password){
		try{
			UserList list = getUserListFromSpace();
			if(list.userExists(list.getUserByNameAndPassword(username,password))){
				System.out.println("User is already in the users list");
			} else{
				System.out.println("User was not in the users list but we added him/her");
				currentUser = new User(username,password, true);
				list.addUser(currentUser);
				System.out.println("The current user is connected: " + currentUser.getConnected());
				writeUserListBackToSpace(list);
			}	
			return true;
		} catch (Exception e){
			System.out.println("Error: " + e);
		}
		
		return false;
	}
	
	public UserList getUserListFromSpace(){
		UserList template = new UserList();
		try {
			UserList list = (UserList) space.takeIfExists(template, null, Long.MAX_VALUE);
			System.out.println("I have taken user list from the space "+ list);
			
			if(list != null){
				this.userList = list;
			}else{
				System.out.println("I am making a new user list because I couldn't find any existing one");
				userList.initializeArrayList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return userList;
	}
	
	private void writeUserListBackToSpace(UserList list){
		try {
			space.write(list, null, Lease.FOREVER);
			System.out.println("I wrote the user list back to space");
			System.out.println("The list of users: " + list.getDefaultList());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public UserList sendUpdatedList(UserList list){
		userList = list;
		return userList;
	}
	
	public void notify(RemoteEvent ev){
		try{
			UserList userListTemplate = new UserList();
			UserList result = (UserList) space.readIfExists(userListTemplate, null, Long.MAX_VALUE);
			System.out.println("I am being notified here");
			sendUpdatedList(result);
		}catch(Exception e){
			
		}
	}
	
	public RoomList readRoomListFromSpace(){
		RoomList template = new RoomList();
		try {
			RoomList list = (RoomList) space.readIfExists(template, null, Long.MAX_VALUE);
			System.out.println("I have read the room list from the space "+ list);
			
			if(list != null){
				this.roomList = list;
			}else{
				System.out.println("I am making a new room list because I couldn't find any existing one");
				roomList.initializeArrayList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return roomList;
	}
	
	public static void main(String[] args){
		new LoginWindow().setVisible(true);
	}
}