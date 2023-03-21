package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCatalog {
	private static final String SERVERPATH = "server_files//";
	private static final String MSGPATH = SERVERPATH +"messages//";
	
	public ConcurrentHashMap<String, ArrayList<Message>> messages;	
	
	public MessageCatalog() {
		this.messages = new ConcurrentHashMap<String,ArrayList<Message>>();
	}
	
	public ArrayList<Message> getMessages(Client cli) {		
		return this.messages.get(cli.getUser());
	}
	
	public void delMessages(Client cli) {		
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
	
	public void addMessage(Client dest, Message msg,boolean isLoading) {
		if(this.messages.containsKey(dest.getUser())) {
			synchronized(this.messages.get(dest.getUser())){
				this.messages.get(dest.getUser()).add(msg);
				File msg_data = new File(MSGPATH + dest.getUser() + "_msgs.txt");
				if(!isLoading) {
					add_msg(msg_data,dest);
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
							add_msg(msg_data,dest);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		
	}

	private void add_msg(File msg_data,Client dest) {
		synchronized(msg_data) {
			try {
				FileWriter fw = new FileWriter(msg_data,true);
				Message m = this.messages.get(dest.getUser()).get(
						this.messages.get(dest.getUser()).size()-1);
				//src=OLA
				fw.append(m.getSrc().getUser());
				fw.append("=");
				fw.append(m.getMessage()+"\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	public void load_msgs(ConcurrentHashMap<String,Client> clients) {
		
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
