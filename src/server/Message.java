package server;

public class Message {
	
	private Client src;
	private Client dest;
	private String message;
	
	public Message(Client src, Client dest, String message) {
		this.src = src;
		this.dest = dest;
		this.message = message;
	}

	public Client getSrc() {
		return src;
	}	

	public Client getDest() {
		return dest;
	}	

	public String getMessage() {
		return message;
	}	
	
}
