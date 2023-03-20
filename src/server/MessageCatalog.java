package server;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCatalog {
	
	public ConcurrentHashMap<String, ArrayList<Message>> messages;	
	
	public MessageCatalog() {
		this.messages = new ConcurrentHashMap<String,ArrayList<Message>>();
	}
	
	public ArrayList<Message> getMessages(Client cli) {		
		return this.messages.get(cli.getUser());
	}
	
	public void delMessages(Client cli) {		
		this.messages.remove(cli.getUser());
	}
	
	public void addMessage(Client dest, Message msg) {
		if(this.messages.containsKey(dest.getUser())) {
			this.messages.get(dest.getUser()).add(msg);
		} else {
			ArrayList<Message> new_msg = new ArrayList<Message>();
			new_msg.add(msg);
			this.messages.put(dest.getUser(), new_msg);
		}
		
	}
}
