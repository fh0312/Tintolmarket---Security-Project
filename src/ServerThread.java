import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ServerThread extends Thread {
		
		private static final int BUFSIZE = 1024;
		
		/**
		 * Server´s clients catalog
		 */
		private HashMap<String,Client> clients;
		
		/**
		 * Server´s sells
		 */
		public ArrayList<Sell> sells;
		
		/**
		 * Socket to connect with clients using TCP
		 */
		private Socket socket = null;
		
		/**
		 * File that contains user/password information from logged-in users.
		 */
		private File users;

		private HashMap<String, Tintol> wines;

		public ServerThread(Socket inSoc,HashMap<String,Client> clients,ArrayList<Sell> sells,
				HashMap<String,Tintol> wines) {
			this.socket = inSoc;
			this.sells = sells;
			this.users = new File("users.txt");
			this.clients = clients;
			this.sells = sells;
			this.wines = wines;
			System.out.println("server:\tServer_thread initiated!!");
		}
		
		public void run(){
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

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
						outStream.writeBoolean(true);
						
					}
					else {										// Current User
						Client cli = clients.get(user);
						if(cli.validate(passwd)) {				// login bem sucedido
							outStream.writeBoolean(true);
						}
						else {
							outStream.writeBoolean(false);
							//FECHAR E MANDAR TENTAR NOVAMENTE
							//System.exit(-1);
						}
					}
					while(!this.socket.isClosed()) {
						String command = (String) inStream.readObject();
						
					}
					
					
					
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
}
