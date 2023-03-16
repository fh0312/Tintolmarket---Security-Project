package server;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


public class TintolmarketServer {
	private static final String SERVERPATH = "server_files//";
	private static final String WINESPATH = SERVERPATH +"wines//";
	private static final String CLIPATH = SERVERPATH +"users//";
	private static final String MSGPATH = SERVERPATH +"messages//";

	private static int PORT;
	
	protected ConcurrentHashMap<String,Client> clients; 
	
	protected ConcurrentHashMap<String,Tintol> wines; 
	
	protected SellsCatalog sells;
	
	protected File users;
	

	public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Modo de Uso: TintolmarketServer <port>");
            System.exit(-1);
        }
        PORT = (int) Integer.parseInt(args[0]);
        System.out.println("server:\tTintolmarketServer initiated in port: "+PORT);
		TintolmarketServer server = new TintolmarketServer();
		server.startServer();
        
	}
	
	public TintolmarketServer() {
		this.clients = new ConcurrentHashMap<String,Client>();
		this.wines = new ConcurrentHashMap<String,Tintol>();
		this.users = new File(SERVERPATH+"users.txt");
		this.sells = new SellsCatalog();
		if(users.exists()) {
			loadWines();
			loadUsers();
			loadMessages();
		}
		else{
			try {
				new File(SERVERPATH.substring(0,SERVERPATH.length()-2)).mkdir();
				users.createNewFile();
				new File(WINESPATH.substring(0,WINESPATH.length()-2)).mkdir();
				new File(MSGPATH.substring(0,MSGPATH.length()-2)).mkdir();
				new File(CLIPATH.substring(0,CLIPATH.length()-2)).mkdir();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadMessages() {
		// TODO !!
	}

	private void loadWines() {
		File winesDir = new File(SERVERPATH+"wines");
		if(winesDir.listFiles()!=null) {
			for (File wineData : winesDir.listFiles()) {
				String tintolName = wineData.getName().split("\\.")[0];
				String ext = wineData.getName().split("\\.")[1];
				File img = null;
				if ( ! wines.containsKey(tintolName) ){
					if(ext.equals("txt")){
						for (File data : winesDir.listFiles()){
							if (data.getName().contains(tintolName)) {
								String extImg = data.getName().split("\\.")[1];
								if(!extImg.equals("txt")) {
									img = data;
								}
							}
						}
						
						this.wines.put(tintolName, new Tintol(tintolName,img));
					}
					
				}
			}
		}
	}

	private void loadUsers() {
		try {

			Scanner scanner = new Scanner(users);
			String currentLine;
			while(scanner.hasNextLine()){ //For each user in user.txt file :
				currentLine = scanner.nextLine();
				String user = currentLine.split(":")[0];
				File clientData = new File(CLIPATH+user+".txt");
				Client newClient = new Client(clientData);
				
				System.out.println("server:\tUser: "+user+" loaded");
				
				this.clients.put(user,newClient);
				Scanner scCli = new Scanner(clientData);
				scCli.nextLine();
				scCli.nextLine();
				while(scCli.hasNextLine()) {
					String s = scCli.nextLine();
					String tintol = s.split("=")[0];
					Tintol wine = this.wines.get(tintol);
					int quant = Integer.parseInt(s.split("=")[1].split(";")[0]);
					Double price = Double.parseDouble(s.split("=")[1].split(";")[1]);
					this.sells.load(new Sell(newClient,wine,quant,price));
				}
				scCli.close();
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void startServer (){
		ServerSocket sSoc = null;
		try {
			sSoc = new ServerSocket(PORT);
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc,this);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		
		
	}

}
