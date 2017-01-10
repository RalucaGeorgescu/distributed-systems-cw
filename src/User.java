import java.io.Serializable;

public class User implements Serializable{
	private String username;
	private char[] password;
	private Boolean connected = false;
	private static final long serialVersionUID = 7526471155622776147L;
	
	public User(String username, char[] password, Boolean connected){
		this.username = username;
		this.password = password;
		this.connected = connected;
	}
	
	public String getUsername(){
		return username;
	}
	
	public char[] getPassword(){
		return password;
	}
	
	public Boolean getConnected(){
		return connected;
	}
	
	public void setConnected(Boolean value){
		this.connected = value;
	}
}
