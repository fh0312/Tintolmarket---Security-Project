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
 *
 */
public class Tintolmarket {
	

	public static void main(String[] args) {
		String serverAddr = "" ;
		String userID = "";
		String pwd = "";
		
		if(args.length==3) {
			userID = args[1];
			serverAddr = args[0];
			pwd = args[2];
		}
		
		else if(args.length==2) {
			serverAddr = args[0];
			userID = args[1];
			
			Scanner sc = new Scanner(System.in);
			System.out.print("Please insert password: ");
			pwd=sc.nextLine();
			sc.close();
		}
		
		else {
			System.err.println("Modo de Uso: Tintolmarket <serverAddress> <userID> [password]");
            System.exit(-1);
		}
		
		
		try {
			Socket cliSocket = new Socket(serverAddr.split(":")[0],
					Integer.parseInt(serverAddr.split(":")[1]));
			ObjectInputStream inStream = new ObjectInputStream(cliSocket.getInputStream());
			ObjectOutputStream outStream = new ObjectOutputStream(cliSocket.getOutputStream());
			
			if(cliSocket.isConnected()) {
				outStream.writeObject(userID);
				outStream.writeObject(pwd);
				try {
					String answer = (String)inStream.readObject();
					if(answer.equals("true")) { //loged in successfully
						displayOptions();
						Scanner inputCli = new Scanner(System.in);
						String cmd = inputCli.nextLine();
						
						
						String op = cmd.split("\\s+")[0];
						outStream.writeObject(cmd); 						//sending command
						
						if(op.equals("a") || op.equals("add")) {
							String path = cmd.split("\\s+")[2];
							File img = new File(path);
							
							FileInputStream fin = new FileInputStream(img);
							InputStream input = new BufferedInputStream(fin);

							byte[] buff = new byte[1024];
							
							int bytesRead=0;
							//Envia o conteudo do ficheiro
							
							while((bytesRead = input.read(buff, 0, 1024)) >0) {
								outStream.write(buff, 0, bytesRead);
							}
							input.close();
						}
						
						inputCli.close();
					}
					else if(answer.equals("false")){
						System.out.println("Incorrect password! Please try again!");
						System.exit(-1);
					}
					else {
						System.out.println("ERRO"+answer);
					}
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
			cliSocket.close();

		} 
		
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	private static void displayOptions() {
		StringBuffer result = new StringBuffer();
		result.append("Choose one of the following commands:\n");
		result.append("\t-"+"add <wine> <image>"+"\n");
		result.append("\t-"+"sell <wine> <value> <quantity>"+"\n");
		result.append("\t-"+"view <wine>"+"\n");
		result.append("\t-"+"buy <wine> <seller> <quantity>"+"\n");
		result.append("\t-"+"wallet"+"\n");
		result.append("\t-"+"classify <wine> <stars>"+"\n");
		result.append("\t-"+"talk <user> <message>"+"\n");
		result.append("\t-"+"read"+"\n");
		result.append("\n\t"+"-> ");
		System.out.println(result);
	}

}
