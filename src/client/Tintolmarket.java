package client;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author 
 * Alexandre Müller - FC56343
 * Diogo Ramos - FC56308
 * Francisco Henriques - FC56348 
 *
 */
public class Tintolmarket {
	/**
	 * Path do the client directory 
	 * Also where to put the image files when adding a wine
	 */
	private static final String CLIENTPATH = "client_files//";
	

	
	public static void main(String[] args) {
		new File(CLIENTPATH.substring(0,CLIENTPATH.length()-2)).mkdir();
		
		Scanner inputCli = new Scanner(System.in);
		
		String serverAddr = "" ;
		int port = 12345;
		String truststore = "";
		String keystore = "";
		String pswdKeystore = "";
		String userID="";
		
		
		if(args.length==5) {
			if(args[0].contains(":")) {
				serverAddr = args[0].split(":")[0];
				port = Integer.parseInt(args[0].split(":")[1]);
			}
			else {
				serverAddr = args[0];
			}
			truststore = args[1];
			keystore = args[2];
			pswdKeystore = args[3];
			userID = args[4];
		}
		
		else if(args.length==0) {
			//Caso teste
			serverAddr = "127.0.0.1" ;
			port = 12345;
			truststore = "";//TODO
			keystore = "";//TODO
			pswdKeystore = "";//TODO
			userID="test";//TODO
		}
		
		else {
			System.err.println("Modo de Uso: \tTintolmarket <serverAddress> <truststore> <keystore> "
					+ "<password-keystore> <userID>");
            System.exit(-1);
		}
		
		Socket cliSocket = null;
		try {
			cliSocket = new Socket(serverAddr,port);
			ObjectInputStream inStream = new ObjectInputStream(cliSocket.getInputStream());
			ObjectOutputStream outStream = new ObjectOutputStream(cliSocket.getOutputStream());
			
			if(cliSocket.isConnected()) {
				outStream.writeObject(userID);
				outStream.writeObject(pwd);
				try {
					String answer = (String)inStream.readObject();
					
					if(answer.equals("true")) { //loged in successfully
						System.out.println("\n\t\tWelcome "+userID+" !");
						while(true) {
							
							displayOptions();
							
							
							String cmd = inputCli.nextLine();

							
							String op = cmd.split("\\s+")[0];
							outStream.writeObject(cmd); 						//sending command
							
							if(op.equals("a") || op.equals("add")) {
								String path = cmd.split("\\s+")[2];
								File img = new File(CLIENTPATH+path);
								
								FileInputStream fin = new FileInputStream(img);
								InputStream input = new BufferedInputStream(fin);

								byte[] buff = new byte[1024];
								outStream.writeObject(img.length());
								int bytesRead=0;
								//Envia o conteudo do ficheiro
								
								while((bytesRead = input.read(buff, 0, 1024)) >0) {
									outStream.write(buff, 0, bytesRead);
								}
								outStream.flush();

							}
							
							String ret = (String) inStream.readObject();
							System.out.println("\n\t"+ret+"\n\n");
							System.out.print("\n\tTo continue press ENTER...");
							inputCli.nextLine();
							
							
							
						}
						
					}
					else if(answer.equals("false")){
						System.out.println("Incorrect password! Please try again!");
						System.exit(-1);
					}
					else {
						System.out.println("ERRO"+answer);
					}
					inputCli.close();
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
			}
			
			cliSocket.close();

		} 
		
		catch (IOException e) {
			e.printStackTrace();
			try {
				cliSocket.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
	}

	/**
	 * Method that displays to the standart output the possible commands (and their arguments)
	 */
	private static void displayOptions() {
		StringBuffer result = new StringBuffer();
		result.append("\n\nChoose one of the following commands:\n");
		result.append("\t-"+"add <wine> <image>"+"\n");
		result.append("\t-"+"sell <wine> <value> <quantity>"+"\n");
		result.append("\t-"+"view <wine>"+"\n");
		result.append("\t-"+"buy <wine> <seller> <quantity>"+"\n");
		result.append("\t-"+"wallet"+"\n");
		result.append("\t-"+"classify <wine> <stars>"+"\n");
		result.append("\t-"+"talk <user> <message>"+"\n");
		result.append("\t-"+"read"+"\n");
		result.append("\n\t"+"-> ");
		System.out.print(result);
	}

}
