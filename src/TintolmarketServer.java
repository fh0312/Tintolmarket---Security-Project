import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class TintolmarketServer {

	private static int PORT;

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
				ServerThread newServerThread = new ServerThread(inSoc);
				System.out.println("server:\tConnection started!!");
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		
	}

}
