import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class ServerThread extends Thread {
		
		private static final int BUFSIZE = 1024;
		
		/**
		 * Server´s clients catalog
		 */
		private HashMap<String,Client> clients; 
		
		/**
		 * Socket to connect with clients using TCP
		 */
		private Socket socket = null;
		
		/**
		 * File that contains user/password information from logged-in users.
		 */
		private File users;

		public ServerThread(Socket inSoc,HashMap<String,Client> clients) {
			socket = inSoc;
			users = new File("users.txt");
			this.clients = clients;
			System.out.println("server:\tServer_thread initiated!!");
		}
 
		public void run(){
			try {
				//users log file
				users.createNewFile();
				
				
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String passwd = null;
				
				try {
					user = (String)inStream.readObject();
					passwd = (String)inStream.readObject();
					Scanner scanner = new Scanner(users);
					String currentLine;
					String lineFound = "";
					while(scanner.hasNextLine()){
						currentLine = scanner.nextLine();
						if(currentLine.contains(user+":")){
					         lineFound= currentLine;
					         break;
					    }
					}
					scanner.close();
					
					/**
					 * TODO
					 * Operations:
					 * 1- New Client			: Registar Cliente, adicionar o cliente ao map, adicionar ao ficheiro de texto
					 * 2- Known Client			: 
					 * 3- User/Pswd incorret	: Terminar o programa com mensagem de erro
					 * 
					 */

					if(lineFound.equals("")) { 					// OP = 1 
						
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
