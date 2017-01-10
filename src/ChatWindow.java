import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.*;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;

public class ChatWindow extends JFrame implements RemoteEventListener {
	public JPanel loginPanel, roomsPanel, usersPanel, chatPanelNorth, chatPanelSouth;
	public JLabel welcomeMessage, roomsLabel, usersLabel;
	public JScrollPane chatScroll, roomsListScroll, usersListScroll;
	public JButton joinRoomBtn, createRoomBtn, deleteRoomBtn, loginBtn;
	public JTextArea messageIn;
	public JList<String> roomsList, usersList;
	public JCheckBox privateMessage;

	private DefaultListModel<String> roomJList;
	private RoomList roomList;
	private MessageList messageList;

	private JavaSpace space;
	private RemoteEventListener theStub;
	//private Transaction txn;

	public ChatWindow(String username, RoomList roomList) {
		super();
		space = SpaceUtils.getSpace();
		if (space == null) {
			System.out.println("Space not found");
			System.exit(1);
		}

		Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(),
				false, true);

		try {
			// register this as a remote object
			// and get a reference to the 'stub'
			theStub = (RemoteEventListener) myDefaultExporter.export(this);

			// add the listener
			RoomList template = new RoomList();
			space.notify(template, null, this.theStub, Lease.FOREVER, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

		this.roomList = roomList;
		this.messageList = new MessageList();
		System.out.println("The roomlist I received from LoginWindow is " + this.roomList.getDefaultList());
		roomJList = this.roomList.getDefaultList();

		initComponents(username, roomList);
	}

	private void initComponents(String username, RoomList roomList) {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// Say hi to the connected User
		welcomeMessage = new JLabel();
		welcomeMessage.setText("Welcome " + username + "!");
		welcomeMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
		JPanel welcome = new JPanel();
		welcome.add(welcomeMessage);

		// Rooms panel
		roomsPanel = new JPanel();
		roomsPanel.setBorder(BorderFactory.createTitledBorder("Rooms List"));
		roomsListScroll = new JScrollPane();
		roomsListScroll.setPreferredSize(new Dimension(200, 350));
		roomsList = new JList<String>();
		roomsList.setModel(roomJList);
		roomsList.setVisibleRowCount(20);
		roomsList.setCellRenderer(new SelectedListCellRenderer());
		roomsList.setBorder(BorderFactory.createEmptyBorder());
		UIManager.put("List.focusCellHighlightBorder", BorderFactory.createEmptyBorder());
		roomsListScroll.setViewportView(roomsList);
		roomsPanel.add(roomsListScroll);

		joinRoomBtn = new JButton();
		joinRoomBtn.setText("Join Room");
		joinRoomBtn.setSize(40, 20);
		joinRoomBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Get room that is selected from the list
				String selectedRoom = roomsList.getSelectedValue().toString();

				//Add user to connected users for that room
				addConnectedUser(username, selectedRoom);
				
				// Open room with that name
				new RoomWindow(selectedRoom, username, roomList);
			}
		});

		deleteRoomBtn = new JButton();
		deleteRoomBtn.setText("Delete Room");
		deleteRoomBtn.setSize(40, 20);
		deleteRoomBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Action when deleting a room
				String selectedRoom = roomsList.getSelectedValue().toString();
				// confirm dialog
				int reply = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to delete the room " + selectedRoom + "?", "Delete room",
						JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION) {
					// Check if it's the owner
					if (isOwner(selectedRoom, username)) {
						// yep he has the right to delete it
						deleteAllMessages(selectedRoom);
						deleteRoom(selectedRoom);
					}
				} else {
					System.exit(0);
				}
			}
		});

		createRoomBtn = new JButton();
		createRoomBtn.setText("Create Room");
		createRoomBtn.setSize(40, 20);
		createRoomBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String roomname = JOptionPane.showInputDialog(cp, "Enter room name:", "Create room",
						JOptionPane.PLAIN_MESSAGE);
				if (roomname != null) {
					addNewRoom(roomname, username);
				}
			}
		});

		JPanel roomButtons = new JPanel();
		roomButtons.add(joinRoomBtn);
		roomButtons.add(deleteRoomBtn);
		roomButtons.add(createRoomBtn);

		cp.add(welcome, "North");
		cp.add(roomsPanel, "Center");
		cp.add(roomButtons, "South");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(410, 500);
	}

	public RoomList getRoomListFromTheSpace() {
		RoomList roomListTemplate = new RoomList();
		try {
			RoomList list = (RoomList) space.takeIfExists(roomListTemplate, null, Long.MAX_VALUE);

			if (list != null) {
				this.roomList = list;
				System.out.println("The list I took from space is " + this.roomList.getDefaultList());
			} else {
				System.out.println("Cannot find a list in the space");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return this.roomList;
	}

	public void writeRoomListBackToSpace(RoomList list) {
		try {
			space.write(list, null, Lease.FOREVER);
			System.out.println("I wrote the rooms list back to space and what I wrote is " + list.getDefaultList());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addNewRoom(String roomName, String owner) {
		System.out.println("Before I add something, here is the room list I have " + this.roomList);
		this.roomList = getRoomListFromTheSpace();
		ArrayList<String> users = new ArrayList<String>();
		Room newRoom = new Room(roomName, owner, users);

		System.out.println("Does the room exist already? " + roomList.roomExists(newRoom));
		if (!roomList.roomExists(newRoom)) {
			roomList.addRoom(newRoom);
		} else {
			System.out.println("This room already exists in the rooms list");
		}

		writeRoomListBackToSpace(roomList);
	}
	
	public void addConnectedUser(String username, String roomName){
		this.roomList = getRoomListFromTheSpace();
		Room openedRoom = roomList.getRoomByName(roomName);
		openedRoom.addUser(username);
		writeRoomListBackToSpace(roomList);

		System.out.println(openedRoom.getRoomName() + " now has the following connected users: " + openedRoom.getConnectedUsers());
		
	}

	public void deleteRoom(String roomName) {
		this.roomList = getRoomListFromTheSpace();
		if (isOwner(roomName, roomList.getOwnerByRoomName(roomName))) {
			roomList.deleteRoomByName(roomName);
			writeRoomListBackToSpace(roomList);
		} else {
			JOptionPane.showMessageDialog(null, "You are not the owner of the room, you cannot delete it.");
		}
	}

	public void deleteAllMessages(String roomName) {
		this.messageList = takeMessageListFromSpace();
		if (messageList != null) {
			messageList.deleteMessagesForRoom(roomName);
			writeListBackToSpace(messageList);
		}
	}

	public MessageList readMessageListFromSpace() {
		MessageList template = new MessageList();
		MessageList list = new MessageList();
		try {
			list = (MessageList) space.readIfExists(template, null, Long.MAX_VALUE);
			System.out.println("I read the message list from the space list is " + list.getDefaultList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public void writeListBackToSpace(MessageList list) {
		try {
			space.write(list, null, Lease.FOREVER);
			System.out.println("I wrote the message list back to space");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public MessageList takeMessageListFromSpace() {
		MessageList template = new MessageList();
		MessageList list = new MessageList();
		try {
			list = (MessageList) space.takeIfExists(template, null, Long.MAX_VALUE);
			System.out.println("I took the message list from the space the list is " + list);
			if (list != null) {
				this.messageList = list;
			} else {
				System.out.println("I am making a new msg list");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public Room getRoomByName(String roomName) {
		System.out.println(
				"The room I am returning for name " + roomName + " is " + this.roomList.getRoomByName(roomName));
		return this.roomList.getRoomByName(roomName);
	}

	public boolean isOwner(String roomName, String owner) {
		String roomOwner = this.roomList.getRoomOwner(roomName);
		if (roomOwner.equals(owner)) {
			System.out.println(owner + " is truly the owner of the room " + roomName);
			return true;
		} else {
			System.out.println("Sorry, you are not the owner of the room");
			JOptionPane.showMessageDialog(null,
					"Sorry, you are not the owner of the room so you are not allowed to delete it", "Error",
					JOptionPane.WARNING_MESSAGE);
		}
		return false;
	}

	public void notify(RemoteEvent ev) {
		RoomList template = new RoomList();

		try {
			RoomList result = (RoomList) space.readIfExists(template, null, Long.MAX_VALUE);
			System.out.println("The rooms we have now are " + result.getDefaultList());
			roomJList.clear();
			for (int i = 0; i < result.getDefaultList().size(); i++) {
				roomJList.addElement(result.getDefaultList().getElementAt(i));
			}
			this.roomList = result;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class SelectedListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (isSelected) {
				c.setBackground(new Color(218, 243, 236));
				c.setForeground(Color.black);
			}
			return c;
		}
	}

}
