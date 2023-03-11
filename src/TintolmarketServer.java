import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;


public class TintolmarketServer {

	private static int PORT;
	
	private HashMap<String,Client> clients; 
	
	private File users;

	public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Modo de Uso: TintolmarketServer <port>");
            System.exit(-1);
        }
        PORT = (int) Integer.parseInt(args[0]);
        System.out.println("server:\tTintolmarketServer initiated!");
		TintolmarketServer server = new TintolmarketServer();
		server.startServer();
        
	}
	
	public TintolmarketServer() {
		this.clients = new HashMap<String,Client>();
		this.users = new File("users.txt");
		if(users.exists()) {
			Scanner scanner;
			try {
				scanner = new Scanner(users);
				String currentLine;
				while(scanner.hasNextLine()){
					currentLine = scanner.nextLine();
					
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		else{
			try {
				users.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void startServer (){
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(PORT);
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
         
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc,clients);
				System.out.println("server:\tConnection started!!");
				newServerThread.start();
				
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		
	}

}
