package client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Scanner;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author Alexandre Müller - FC56343 Diogo Ramos - FC56308 Francisco Henriques
 *         - FC56348
 *
 */
public class Tintolmarket {
	/**
	 * Path do the client directory Also where to put the image files when adding a
	 * wine
	 */
	private static final String CLIENTPATH = "client_files//";

	public static void main(String[] args) throws ClassNotFoundException, NoSuchAlgorithmException {
		new File(CLIENTPATH.substring(0, CLIENTPATH.length() - 2)).mkdir();

		Scanner inputCli = new Scanner(System.in);

		String serverAddr = "";
		int port = 12345;
		String truststorePath = "";
		String keystorePath = "";
		String pswdKeystore = "";		
		String userID = "";

		if (args.length == 5) {
			if (args[0].contains(":")) {
				serverAddr = args[0].split(":")[0];
				port = Integer.parseInt(args[0].split(":")[1]);
			} else {
				serverAddr = args[0];
			}
			truststorePath = args[1];
			keystorePath = args[2];
			pswdKeystore = args[3];
			userID = args[4];
		}

		else if (args.length == 0) {
			// Caso teste
			serverAddr = "127.0.0.1";
			port = 12345;
			truststorePath = "truststore.client";
			keystorePath = "keystore.client1";// TODO
			pswdKeystore = "adminadmin";// TODO
			userID = "test";// TODO
		}

		else {
			System.err.println("Modo de Uso: \tTintolmarket <serverAddress> <truststore> <keystore> "
					+ "<password-keystore> <userID>");
			System.exit(-1);
		}
		
		
		KeyStore ks=null;
		PrivateKey privateKey = null;
		
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
		} catch (KeyStoreException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(keystorePath);
		} catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		try {
			ks.load(fis, "adminadmin".toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try {
			privateKey = (PrivateKey) ks.getKey(ks.aliases().nextElement(), pswdKeystore.toCharArray());
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		

		SSLSocket cliSocket = null;
		try {
			System.setProperty("javax.net.ssl.trustStore", truststorePath);
			System.setProperty("javax.net.ssl.trustStorePassword", "adminadmin" );
			SocketFactory sf = SSLSocketFactory.getDefault( );
			cliSocket = (SSLSocket) sf.createSocket(serverAddr, port);

			// Start handshake
			cliSocket.startHandshake();

			// Connection successful
			System.out.println("Connected to server.");

			// cliSocket = new Socket(serverAddr,port);
			ObjectInputStream inStream = new ObjectInputStream(cliSocket.getInputStream());
			//PrintWriter outStream = new PrintWriter(cliSocket.getOutputStream(), true);
			ObjectOutputStream outStream = new ObjectOutputStream(cliSocket.getOutputStream());
			if (cliSocket.isConnected()) {
				outStream.writeObject(userID);
				System.out.println("User enviado");
				
				String nonceStrReceived = (String) inStream.readObject();
				String nonceStr = nonceStrReceived.split(":")[0];
				byte[] nonce = nonceStr.getBytes(StandardCharsets.ISO_8859_1);
				
				
				
				if(nonceStrReceived.contains(":")) { // Nao esta registado
					
					StringBuilder ret = new StringBuilder();
					String parte1 = new String(nonce,StandardCharsets.ISO_8859_1);
					
					byte[] nonceSigned = signNonce(privateKey,new String(nonce,StandardCharsets.ISO_8859_1));
					String parte2 = new String(nonceSigned,StandardCharsets.ISO_8859_1);
					
					int sizee = nonceSigned.length;
					
					System.out.println("Resposta ao server com (nonce:signNonce) -> " + parte1+"\n\n"+parte2);
					
					
					outStream.writeObject(parte1);
					outStream.writeObject(parte2);
					
			        try {
						ks.load(new FileInputStream(keystorePath), pswdKeystore.toCharArray());
					} catch (NoSuchAlgorithmException | CertificateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        
			        Certificate cert= createCert(ks);
			        File certFile = null;
			        try {
						certFile = getCertFile(cert,ks.aliases().nextElement());
					} catch (KeyStoreException e) {
						e.printStackTrace();
					}
					
					sendFile(cliSocket,inStream, certFile,outStream);
					

					
					//enviar  assinatura deste gerada com a sua
					//chave privada, e o certificado com a chave pública correspondente.
					
					
				}
				else { //esta registado
					
					
					byte[] nonceSigned = signNonce(privateKey,new String(nonce,StandardCharsets.ISO_8859_1));;
					
					outStream.writeObject(nonceSigned);
					
				}
				
				String answer = (String) inStream.readObject(); //indicacao que foi aprovado o login //TODO
				

				
				if (answer.equals("true")) { // loged in successfully
					System.out.println("\n\t\tWelcome " + userID + " !");
					while (true) {

						displayOptions();

						String cmd = inputCli.nextLine();

						String op = cmd.split("\\s+")[0];
						outStream.writeObject(cmd); // sending command

						if (op.equals("a") || op.equals("add")) {
							String path = cmd.split("\\s+")[2];
							File img = new File(CLIENTPATH + path);

							FileInputStream fin = new FileInputStream(img);
							InputStream input = new BufferedInputStream(fin);

							
							outStream.writeObject((int)img.length());
							
							
							byte[] buffer = new byte[1024];
							int bytesRead;

							// Send the file in 1024 byte chunks
							while ((bytesRead = input.read(buffer)) != -1) {
							    outStream.writeObject(new String(buffer, 0, bytesRead));
							}

							// Flush the output stream
							outStream.flush();

						}

						String ret = (String) inStream.readLine();
						System.out.println("\n\t" + ret + "\n\n");
						System.out.print("\n\tTo continue press ENTER...");
						inputCli.nextLine();

					}

				} else if (answer.equals("false")) {
					System.out.println("Incorrect password! Please try again!");
					System.exit(-1);
				} else {
					System.out.println("ERRO" + answer);
				}
				inputCli.close();
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

	private static void sendFile(SSLSocket cliSocket,ObjectInputStream inStream, File certFile,ObjectOutputStream outStream) throws ClassNotFoundException {
		
		        
		        try {
					outStream.writeObject(certFile.length()+"");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		        System.out.println("size do ficheiro enviado: "+certFile.length());
		        
		        //System.out.println("enviado e recebido este nº de bytes:"+((Integer)inStream.readObject()).toString());
		        
		        try {
		            FileInputStream fileInputStream = new FileInputStream(certFile);
		            BufferedInputStream fin = new BufferedInputStream(fileInputStream);

		            byte[] buffer = new byte[1024];
		            
		            int bytesRead=0;
		            
		            long totalsize =  certFile.length();
		            
		            while (totalsize>0) {
		            	fin.read(buffer);
		                outStream.write(buffer);
		                // Clear the buffer after writing
		                buffer = new byte[1024];
		                totalsize-= buffer.length;
		            }

		            fin.close();
		            fileInputStream.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        
	}

	private static File getCertFile(Certificate cert,String elias) throws IOException {
		
		File certFile = new File(CLIENTPATH+elias+".crt");
		certFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(certFile);
		try {
			try {
				fos.write(cert.getEncoded());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (CertificateEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    fos.close();
		return certFile;
	}

	private static Certificate createCert(KeyStore ks) {
		String elias=null;
		try {
			elias = ks.aliases().nextElement();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
        Certificate cert=null;
		try {
			cert = ks.getCertificate(elias);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return cert;
	}

	private static byte[] signNonce(PrivateKey privateKey, String nonce) {
		Signature signature = null;
		try {
			signature = Signature.getInstance("SHA256withRSA");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			signature.initSign(privateKey);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] nonceBytes = nonce.getBytes(StandardCharsets.ISO_8859_1);
		try {
			signature.update(nonceBytes);
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] nonceSigned = null;
		try {
			nonceSigned = signature.sign();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nonceSigned;
	}

	/**
	 * Method that displays to the standart output the possible commands (and their
	 * arguments)
	 */
	private static void displayOptions() {
		StringBuffer result = new StringBuffer();
		result.append("\n\nChoose one of the following commands:\n");
		result.append("\t-" + "add <wine> <image>" + "\n");
		result.append("\t-" + "sell <wine> <value> <quantity>" + "\n");
		result.append("\t-" + "view <wine>" + "\n");
		result.append("\t-" + "buy <wine> <seller> <quantity>" + "\n");
		result.append("\t-" + "wallet" + "\n");
		result.append("\t-" + "classify <wine> <stars>" + "\n");
		result.append("\t-" + "talk <user> <message>" + "\n");
		result.append("\t-" + "read" + "\n");
		result.append("\n\t" + "-> ");
		System.out.print(result);
	}

}
