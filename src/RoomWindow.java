import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;

public class RoomWindow extends JFrame implements RemoteEventListener, WindowListener{
	private String roomName;
	private String username;
	private String roomOwner;
	private RoomList roomList;
	private MessageList messageList;
	
	private JavaSpace space;
	private RemoteEventListener theStub;
	
	private JPanel introPanel,chatPanel,sendingPanel;
	public JTextArea chatArea, messageArea;
	private JScrollPane chatAreaScroll;
	private JButton sendBtn;
	private JCheckBox privateBox;
	private JLabel roomInformationLabel;
	
	public RoomWindow(String roomName, String username, RoomList roomList){
		super();
		space = SpaceUtils.getSpace();
		if(space == null){
			System.out.println("Could not find space for RoomWindowHandler");
			System.exit(1);
		}
		
		Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(), false, true);
		
		try {
			// register this as a remote object 
			// and get a reference to the 'stub'
			theStub = (RemoteEventListener) myDefaultExporter.export(this);
			
			// add the listener
			MessageList template = new MessageList();
			space.notify(template, null, this.theStub, Lease.FOREVER, null);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.username = username;
		this.roomName = roomName;	
		this.roomList = roomList;
		this.roomOwner = getOwnerForRoom(roomName);
		this.messageList = new MessageList();
		
		chatArea = new JTextArea(15,48);
    	chatArea.setEditable(false);
    	
		if(readMessageListFromSpace() !=null){
			this.messageList = readMessageListFromSpace();
			initializeChatArea(chatArea);
		} else {
			this.messageList = new MessageList();
			this.messageList.initializeList();
			writeListBackToSpace(this.messageList);
			System.out.println("In the constructor, after the read, the msg list is " + this.messageList);
		}
	
		initComponents();
		this.setVisible(true);
	}

	public void initComponents(){
		this.setTitle(roomName + " room - " + username);
		Border roundedBorder = new LineBorder(Color.lightGray, 1,true);		
		Container cp = getContentPane();
    	cp.setLayout (new BorderLayout ());
    	
    	introPanel = new JPanel(new BorderLayout());
    	chatPanel = new JPanel();
    	sendingPanel = new JPanel();

    	//IntroPanel
    	roomInformationLabel = new JLabel();
    	Room currentRoom = roomList.getRoomByName(roomName);
    	if(this.roomOwner!=null){
    		roomInformationLabel.setText("Owner: " + this.roomOwner + ". Connected users: " + currentRoom.getConnectedUsers());
    	} else if(username.equals(this.roomOwner)) {
    		roomInformationLabel.setText("You are the owner of this room");
    	}
    	
    	introPanel.add(roomInformationLabel,BorderLayout.WEST);
    	introPanel.setBorder(new EmptyBorder(10, 10, 10, 12));
    	
    	//ChatPanel
    	chatAreaScroll = new JScrollPane();
    	chatAreaScroll.setViewportView(chatArea); 
    	chatPanel.add(chatAreaScroll);
    	
    	//SendingPanel
    	
    	messageArea = new JTextArea(5,30);
    	messageArea.setBorder(roundedBorder);
    	messageArea.setEditable(true);  	
		sendBtn = new JButton();
		sendBtn.setText("Send");
		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt){
				//send message to handler
				handleMessage(getMessage(), getPrivateFlag(), username);
				messageArea.setText("");
			}
		});	
		privateBox = new JCheckBox();
		privateBox.setText("private message");
		sendingPanel.add(messageArea);
		sendingPanel.add(sendBtn);
		sendingPanel.add(privateBox);
		
		//BorderLayout
		cp.add(introPanel, "North");
		cp.add(chatPanel, "Center");
		cp.add(sendingPanel, "South");
		
		this.addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		        System.out.println("I am closing the room");
		    }
		});
		
		this.setSize(600,400);
	}
	
	public String getMessage(){
		return messageArea.getText();
	}
	
	public boolean getPrivateFlag(){
		return privateBox.isSelected();
	}
	
	public String getOwnerForRoom(String roomName){
		return roomList.getRoomOwner(roomName);
	}
	
	public void initializeChatArea(JTextArea chatArea){
		ArrayList<Message> roomMessages = messageList.getAllMessagesForRoom(this.roomName);
		System.out.println("I have all the msges for this room: " + roomMessages);
		for(Message msg : roomMessages){
			if(msg.isPrivate()){
				if(username.equals(this.roomOwner) || username.equals(msg.sender)){
					chatArea.append("[private message] " + msg.sender+" said: "+msg.contents+"\n");
				}			
			} else{
				chatArea.append(msg.sender+" said: "+msg.contents+"\n");
			}
		}
		chatArea.append(">>> " + username + " joined the room");
	}
	
	public void handleMessage(String message, boolean privateFlag, String username){
		//add message to Message list
		MessageList list = takeMessageListFromSpace();
		Message newMessage = new Message(message,username,roomName,privateFlag);
		System.out.println("The new message is "+ newMessage);
		list.addMessage(newMessage);
		
		writeListBackToSpace(list);
	}
	
	public MessageList takeMessageListFromSpace(){
		MessageList template = new MessageList();
		MessageList list = new MessageList();
		try {
			list = (MessageList) space.takeIfExists(template, null, Long.MAX_VALUE);
			System.out.println("I took the message list from the space the list is " + list);
			if(list!=null){
				this.messageList = list;
			}
			else {
				System.out.println("I am making a new msg list");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return list;
	}
	
	public MessageList readMessageListFromSpace(){
		MessageList template = new MessageList();
		MessageList list = new MessageList();
		try {
			list = (MessageList) space.readIfExists(template, null, Long.MAX_VALUE);
			System.out.println("I read the message list from the space list is " + list);
			this.messageList = list;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return list;
	}
	
	public void notify(RemoteEvent ev) {
		MessageList template = new MessageList();

		try {
			MessageList result = (MessageList) space.readIfExists(template, null, Long.MAX_VALUE);
			String msgContents = result.getLastMessageByRoom(roomName);
			String msgSender = result.getLastSenderByRoom(roomName);
			Boolean msgPrivate = result.getPrivateFlagByRoom(roomName);
			if(msgPrivate){
				//send message to chatArea only if user name = owner
				if (roomOwner.equals(username)||msgSender.equals(username)){
					chatArea.append("[private message] " + msgSender + " said: " +msgContents + "\n");
				}
			}else {
				if(msgSender.equals(this.username)){
					chatArea.append("You said: " + msgContents + "\n");
				}else{
					chatArea.append(msgSender + " said: " + msgContents + "\n");
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void windowDeactivated(WindowEvent we){
		System.out.println("WindowListener method called: windowDeactivated.");
	}
	
	public void writeListBackToSpace(MessageList list){
		try {
			space.write(list, null, Lease.FOREVER);
			System.out.println("I wrote the message list back to space");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
