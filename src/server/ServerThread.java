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
import java.util.HashMap;
import java.util.Scanner;

public class ServerThread extends Thread {
		
		private static final String SERVERPATH = "server_files//";
		private static final String WINESPATH = SERVERPATH +"wines//";
		private static final String CLIPATH = SERVERPATH +"users//";
		private static final String MSGPATH = SERVERPATH +"messages//";
		

		/**
		 * Socket to connect with clients using TCP
		 */
		private Socket socket = null;
		
		/**
		 * Server´s clients catalog
		 */
		private HashMap<String,Client> clients;
		
		/**
		 * Server´s wines catalog
		 */
		private HashMap<String, Tintol> wines;
		
		/**
		 * Server´s sells
		 */
		public SellsCatalog sells;
		
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
		
		
		

		public ServerThread(Socket inSoc,HashMap<String,Client> clients,SellsCatalog sells,
				HashMap<String,Tintol> wines) {
			this.socket = inSoc;
			this.sells = sells;
			this.users = new File(SERVERPATH+"users.txt");
			this.clients = clients;
			this.sells = sells;
			this.wines = wines;
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
						this.clients.put(user, newCli);
						FileWriter writer = new FileWriter(users);
						writer.write(user+":"+passwd+"\n");
						writer.close();
						outStream.writeObject("true");
						
					}
					else {										// Current User
						this.currentCli = clients.get(user);
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
					System.out.println("server:\t "+user+" logged out! - Connection ended");
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
			// TODO 
			
		}

		private void talk(String cmd) {
			// TODO 
			
		}

		private void classify(String cmd) {
			// TODO 
			
		}

		private void wallet() {
			// TODO 
			
		}

		private void buy(String cmd) {
			// TODO 
			
		}

		private void view(String cmd) {
			// TODO 
			
		}

		private void sell(String cmd) {
			// TODO 
			
		}

		private void addWine(String cmd) {
			try {
				String[] parts = cmd.split("\\s+");
				String fileName = parts[2];
				byte[] buff = new byte[1024];
				File img = new File(WINESPATH+parts[2]);
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
					this.wines.put(tintol.getName(), tintol);
					
					outStream.writeObject((String)("Tintol - "+tintol.getName()+" added!"));
					System.out.println("server:\t(Tintol) - "+tintol.getName()+" added!");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
}
