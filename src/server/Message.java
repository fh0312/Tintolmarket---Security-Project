package server;

/**
 * @author 
 * Alexandre Müller - FC56343
 * Diogo Ramos - FC56308
 * Francisco Henriques - FC56348 
 *
 */

/**
 * Class that represents a message in the Tinotlmarket system
 * This implementation of Message only accepts :
 * 		- 1 source client
 * 		- 1 destination client
 * 		- A String with the body (only ASCCI chars accepted)
 */
public class Message {
	/**
	 * Message's source client
	 */
	private Client src;
	
	/**
	 * Message's destination client
	 */
	private Client dest;
	
	/**
	 * Message's body text
	 */
	private byte[] message;
	
	/**
	 * Message's constructor
	 * @param src - source client
	 * @param dest - destination client
	 * @param message - body text
	 */
	protected Message(Client src, Client dest, byte[] message) {
		this.src = src;
		this.dest = dest;
		this.message = message;
	}
	
	/**
	 * Gets the message's source client 
	 * @return source client
	 */
	protected Client getSrc() {
		return src;
	}	
	
	/**
	 * Gets the message's destination client 
	 * @return destination client
	 */
	protected Client getDest() {
		return dest;
	}	
	
	/**
	 * Gets the message's body 
	 * @return body 
	 */
	protected byte[] getMessage() {
		return message;
	}	
	
}
