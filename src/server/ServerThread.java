package server;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerThread extends Thread {
		
		private static final String SERVERPATH = "server_files//";
		private static final String WINESPATH = SERVERPATH +"wines//";
		private static final String CLIPATH = SERVERPATH +"users//";

		

		/**
		 * Socket to connect with clients using TCP
		 */
		private Socket socket = null;
		
		
		
		private TintolmarketServer server;
		
		/**
		 * File that contains user/password information from logged-in users.
		 */
		private File users;
		
		/**
		 * Current client in this thread
		 */
		private Client currentCli;
		
		/**
		 * Channel to send messages to the client
		 */
		private ObjectOutputStream outStream;
		
		/**
		 * Channel to receive messages from the client
		 */
		private ObjectInputStream inStream;
		
		
		

		public ServerThread(Socket inSoc,TintolmarketServer s) {
			this.socket = inSoc;
			this.server=s;
			this.users = s.users;
			System.out.println("server:\tServer_thread initiated!!");
		}
		
		public void run(){
			try {
				this.outStream = new ObjectOutputStream(socket.getOutputStream());
				this.inStream = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String passwd = null;
				
				try {
					user = (String)inStream.readObject();
					passwd = (String)inStream.readObject();
					
					Scanner scanner = new Scanner(users);
					String currentLine;
					String userFound = "";
					while(scanner.hasNextLine()){
						currentLine = scanner.nextLine();
						if(currentLine.contains(user+":")){
					         userFound= currentLine;
					         break;
					    }
					}
					scanner.close();

					if(userFound.equals("")) { 					// New User
						Client newCli = new Client(user,passwd);
						server.clients.put(user, newCli);
						this.currentCli=newCli;
						FileWriter writer = new FileWriter(users,true);
						
						synchronized(writer) {
							writer.write(user+":"+passwd+"\n");
							writer.close();
						}
						outStream.writeObject("true");
						
						
					}
					else {										// Current User
						this.currentCli = server.clients.get(user);
						if(this.currentCli.validate(passwd)) {				// login bem sucedido
							outStream.writeObject("true");
						}
						else {
							outStream.writeObject("false");
							//FECHAR E MANDAR TENTAR NOVAMENTE
							//System.exit(-1);
						}
					}
					String cmd ="";
					
					
					while((cmd = (String) inStream.readObject())!="-1") {
						
						System.out.println("server:\tCommand received: "+cmd);
						String op = cmd.split("\\s+")[0];
						if(op.equals("a") || op.equals("add")) {
							addWine(cmd);
						}
						else if(op.equals("s") || op.equals("sell")){
							sell(cmd);
						}
						else if(op.equals("v") || op.equals("view")){
							view(cmd);
						}
						else if(op.equals("b") || op.equals("buy")){
							buy(cmd);
						}
						else if(op.equals("w") || op.equals("wallet")){
							wallet();
						}
						else if(op.equals("c") || op.equals("classify")){
							classify(cmd);
						}
						else if(op.equals("t") || op.equals("talk")){
							talk(cmd);
						}
						else if(op.equals("r") || op.equals("read")){
							read(cmd);
						}
					}
					
					
				}
				catch(SocketException socket) {
					System.out.println("server:\t"+user+" logged out! - Connection ended");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			catch (Exception e) {
				System.out.println("Error");
				e.printStackTrace();
			}
		}

		private void read(String cmd) {
			StringBuilder sb = new StringBuilder();
			ArrayList<Message> msgs = server.messages.getMessages(this.currentCli);
			sb.append("\tUnread messages:\n");
			if(msgs!=null) {
				for(Message m : msgs) {
					sb.append(m.getSrc().getUser() + " sent: " + m.getMessage()+"\n");
				}
				server.messages.delMessages(this.currentCli);
				try {
					outStream.writeObject(sb.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					outStream.writeObject("Your Inbox is Clear!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
		}

		private void talk(String cmd) {
			String[] parts = cmd.split("\\s+");
			Client dest = server.clients.get(parts[1]);
			if(dest==null) {
				try {
					outStream.writeObject("Destination user not found!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				Message m = new Message(this.currentCli,dest,parts[2]);
				server.messages.addMessage(dest, m);
				try {
					outStream.writeObject("Message sent!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
			
		}
		

		private void classify(String cmd) {
			String[] parts = cmd.split("\\s+");
			Tintol tintol = this.server.wines.get(parts[1]);
			Double stars = Double.parseDouble(parts[2]);
			try {
				if(tintol != null) {
					tintol.classify(stars);
					this.outStream.writeObject(("The "+ tintol.getName() + 
							" has been rated with "+stars+" stars."));
				}
				else {
					this.outStream.writeObject(parts[1]+" not found!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}

		private void wallet() {
			try {
				this.outStream.writeObject(("Your current balance is: "
						+currentCli.getBalance()+" €"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
		private void buy(String cmd) {
			String[] parts = cmd.split("\\s+");
			Tintol tintol = server.wines.get(parts[1]);
			Client seller = server.clients.get(parts[2]);
			int quant = Integer.parseInt(parts[3]);
			server.sells.buy(tintol,seller,quant,this.currentCli);
			try {
				this.outStream.writeObject("U successfully bought: "+quant
						+" units of"+tintol.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		private void view(String cmd) {
			try {
				String[] parts = cmd.split("\\s+");
				String wineName = parts[1];
				Tintol tintol = server.wines.get(wineName);
				StringBuilder result=new StringBuilder();
				result.append(tintol.toString());
				for(Sell s : server.sells.getSellsByWine(tintol)) {
					result.append("\t"+s.getClient().getUser()+" has "+s.getQuant()+" units "
							+ "at "+s.getPrice()+" € per unit.\n");
				}
				
				
				outStream.writeObject(result.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			catch (NullPointerException eNull){
				try {
					outStream.writeObject("Wine: "+ cmd.split("\\s+")[1]+" not found!");
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
			
			
			
		}

		private void sell(String cmd) {
			//sell wine1 value quant
			String[] parts = cmd.split("\\s+");
			String wineName = parts[1];
			Double price = Double.parseDouble(parts[2]);
			int quant = Integer.parseInt(parts[3]);
			Tintol tintol=null;
			if((tintol = server.wines.get(wineName))!=null) {
				Sell sell;
				if((sell = server.sells.getSell(currentCli, tintol))==null) {
					sell = new Sell(this.currentCli,tintol,quant,price);
					server.sells.add(sell);
					try {
						outStream.writeObject((String)(quant+" units of "+tintol.getName()+" put up for sale, for "+price+"€ each!"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					sell.setQuant(sell.getQuant()+quant);
					sell.writeStats();
					try {
						outStream.writeObject((String)("ERROR - this sale already exists! \n\t"
								+ "Only sell quantity updated\n\t")+
								(quant+" units of "+tintol.getName()+" added in previous sale"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}
			else {
				try {
					outStream.writeObject((String)("Error - Wine not found!\n"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		private void addWine(String cmd) {
			try {
				String[] parts = cmd.split("\\s+");
				String fileName = parts[2];
				byte[] buff = new byte[1024];
				File img = new File(WINESPATH+parts[1]+"."+parts[2].split("\\.")[1]);
				img.delete();
				if(!img.createNewFile()) {
					System.out.println("server:\tFile: "+ fileName +" -> NOT CREATED");
				}
				else {
					FileOutputStream fout = new FileOutputStream(img,true);
					OutputStream output = new BufferedOutputStream(fout);
					

					int bytesRead =0;
					long size =0;
					try {
						size = (long) inStream.readObject();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int alreadyRead =0;
					while(alreadyRead + (bytesRead = inStream.read(buff, 0, 1024)) <size) {
						output.write(buff, 0, bytesRead);
						alreadyRead += bytesRead;
					}
					output.close();
					fout.close();
					
					Tintol tintol = new Tintol(parts[1], img);
					server.wines.put(tintol.getName(), tintol);
					
					outStream.writeObject((String)("Tintol - "+tintol.getName()+" added!"));
					System.out.println("server:\t(Tintol) - "+tintol.getName()+" added!");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
}
