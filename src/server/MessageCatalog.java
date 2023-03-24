package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 
 * Alexandre Müller - FC56343
 * Diogo Ramos - FC56308
 * Francisco Henriques - FC56348 
 *
 */

/**
 * Class that represents a catalog of messages (Message) in the Tinotlmarket system
 */
public class MessageCatalog {
	
	/**
	 * Path to the server's directory
	 */
	private static final String SERVERPATH = "server_files//";
	
	/**
	 * Path to the server's message directory
	 */
	private static final String MSGPATH = SERVERPATH +"messages//";
	
	/**
	 * Thread Safe implementation of an HashMap storing all the messages sent  
	 * by Tintolmarket users.
	 * For each user this map will store an array with all the messages sent to him.
	 */
	protected ConcurrentHashMap<String, ArrayList<Message>> messages;	
	
	/**
	 * MessageCatalog constructor - initializes the field messages with a thread safe HashMap
	 */
	protected MessageCatalog() {
		this.messages = new ConcurrentHashMap<String,ArrayList<Message>>();
	}
	
	/**
	 * Gets an array of messages that a client has received since the last time 
	 * the messages were read.
	 * @param cli - Client that received the messages
	 * @return array with all client messages
	 */
	protected ArrayList<Message> getMessages(Client cli) {		
		return this.messages.get(cli.getUser());
	}
	
	/**
	 * Deletes all messages that a client has, from the memory and the data files.
	 * @param cli - client to delete messages from
	 */
	protected void delMessages(Client cli) {		
		this.messages.remove(cli.getUser());
		File msg_data = new File(MSGPATH + cli.getUser() + "_msgs.txt");
		synchronized(msg_data) {
			msg_data.delete();
			try {
				msg_data.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Adds a new message by the destination client
	 * @param dest
	 * @param msg
	 * @param isLoading -  if :
	 * 
	 * 		true	-	The method will just load a new message in the data 
	 * 					 structures and will not write it in the data file.
	 * 
	 * 		false	-	The method will load the messages in the data structures
	 * 					 and write it to the correspondent data file.	
	 */
	protected void addMessage(Client dest, Message msg,boolean isLoading) {
		if(this.messages.containsKey(dest.getUser())) {
			synchronized(this.messages.get(dest.getUser())){
				this.messages.get(dest.getUser()).add(msg);
				File msg_data = new File(MSGPATH + dest.getUser() + "_msgs.txt");
				if(!isLoading) {
					write_last_msg(msg_data,dest);
				}
				
			}
		} else {
			ArrayList<Message> new_msg = new ArrayList<Message>();
			new_msg.add(msg);
			synchronized(new_msg){
				this.messages.put(dest.getUser(), new_msg);
				
				File msg_data = new File(MSGPATH + dest.getUser() + "_msgs.txt");
					try {
						msg_data.createNewFile();
						if(!isLoading) {
							write_last_msg(msg_data,dest);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		
	}
	
	/**
	 * Writes the last message that a client has, to the correspondente data file.
	 * @param msg_data - file to be written.
	 * @param dest - Client to get the message from.
	 */
	private void write_last_msg(File msg_data,Client dest) {
		synchronized(msg_data) {
			try {
				FileWriter fw = new FileWriter(msg_data,true);
				Message m = this.messages.get(dest.getUser()).get(
						this.messages.get(dest.getUser()).size()-1);
				fw.append(m.getSrc().getUser());
				fw.append("=");
				fw.append(m.getMessage()+"\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Loads in the data structures all messages that have been created in a previous state.
	 * @param clients - Thread safe HashMap of clients registed to the system.
	 */
	protected void load_msgs(ConcurrentHashMap<String,Client> clients) {
			File msgsDir = new File(SERVERPATH+"messages");
			if(msgsDir.listFiles()!=null) {
				for (File msgFile : msgsDir.listFiles()) {
					synchronized(msgFile) {
						//fc56348_msgs.txt;
						Client dest = clients.get(msgFile.getName().split("\\.")[0].split("_")[0]);
						try {
							Scanner sc = new Scanner(msgFile);
							while (sc.hasNextLine()) {
								String line = sc.nextLine();
								Message m = new Message(clients.get(line.split("=")[0]),
										dest, line.split("=")[1]);
								addMessage(dest,m,true);
							}
							sc.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
	
				}
			}
		
		}
	}
