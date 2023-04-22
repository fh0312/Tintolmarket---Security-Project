package server;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * @author 
 * Alexandre Müller - FC56343
 * Diogo Ramos - FC56308
 * Francisco Henriques - FC56348 
 *
 */

public class TintolmarketServer { 
	
	
	//ANETS DE USAR CADA UM DESTES FICHEIROS VERIFICAR A INTEGRIDADE dos mesmos
	private static final String SERVERPATH = "server_files//";
	private static final String WINESPATH = SERVERPATH +"wines//";
	private static final String CLIPATH = SERVERPATH +"users//";
	private static final String MSGPATH = SERVERPATH +"messages//";

	private static int PORT;
	
	private static String KEYSTORE_PATH = "server.keystore";
    private static String TRUSTSTORE_PATH = "server.truststore";
    private static String KEYSTORE_PASSWORD = "keystore_password";
    private static String TRUSTSTORE_PASSWORD = "truststore_password";
    private static String CIPHER_PASSWORD = "pbe_password"; // cifra para cifrar o ficheiro de users - PBE - AES 128bits
    
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128;
    private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256";
	
	
	protected ConcurrentHashMap<String,Client> clients; 
	
	protected ConcurrentHashMap<String,Tintol> wines; 
	
	protected SellsCatalog sells;
	
	protected File users;
	
	protected MessageCatalog messages;
	

	public static void main(String[] args) {
		if(args.length==0) {
			PORT = 12345;
			KEYSTORE_PATH = "keystore.server";
			KEYSTORE_PASSWORD = "adminadmin";
			CIPHER_PASSWORD = "adminadmin";
			
			//TODO - remover !!
		}
		else if (args.length != 1) {
            System.err.println("Modo de Uso: TintolmarketServer <port>"
            		+ "<password-cifra> <keystore> <password-keystore>");
            System.exit(-1);
        }
		else {
			PORT = (int) Integer.parseInt(args[0]);
			CIPHER_PASSWORD = args[1];
			KEYSTORE_PATH = args[2];
			KEYSTORE_PASSWORD = args[3];
		}
        
        System.out.println("server:\tTintolmarketServer initiated in port: "+PORT);
		TintolmarketServer server = new TintolmarketServer();
		try {
			server.startServer();
		} catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException | CertificateException
				| KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	public TintolmarketServer() {
		this.clients = new ConcurrentHashMap<String,Client>();
		this.wines = new ConcurrentHashMap<String,Tintol>();
		this.users = new File(SERVERPATH+"users.txt");
		this.sells = new SellsCatalog();
		this.messages = new MessageCatalog();
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
		this.messages.load_msgs(clients);
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
						
						this.wines.put(tintolName, new Tintol(tintolName,img,wineData));
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

	public void startServer () throws NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException, KeyManagementException{
		SSLServerSocket sSoc = null;
		try {

			System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
			System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
			

			ServerSocketFactory ssf = SSLServerSocketFactory.getDefault( );
			sSoc = (SSLServerSocket) ssf.createServerSocket(PORT);

            // Wait for client connection
            System.out.println("Waiting for client connection...");

			
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		while(true) {
			try {
				new ServerThread(sSoc.accept(),this).start( ); 
				System.out.println("New client connected");
		    }
		    catch (Exception e) {
		    	try {
					sSoc.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		        e.printStackTrace();
		    }
		    
		}
		
		
	}

}
