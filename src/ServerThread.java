import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {

		private static final String PATHNAME = "C:\\Users\\2002f\\OneDrive - Universidade de Lisboa\\"
							+ "Ambiente de Trabalho\\SC\\Praticas\\src\\tp01\\files\\server\\";

		private static final int BUFSIZE = 1024;
		
		private Socket socket = null;

		private File users;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			users = new File(PATHNAME+"users.txt");
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
			}
			catch (Exception e) {
				System.out.println("Error");
				e.printStackTrace();
			}
		}
}
