package server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

/**
 * @author Alexandre Müller - FC56343 Diogo Ramos - FC56308 Francisco Henriques
 *         - FC56348
 *
 */

public class ServerThread extends Thread {

	private static final String SERVERPATH = "server_files//";
	private static final String WINESPATH = SERVERPATH + "wines//";

	/**
	 * Socket to connect with clients using TCP
	 */
	private Socket socket = null;

	private TintolmarketServer server;

	/**
	 * File that contains user/password information from logged-in users.
	 */
	private File users;

	/**
	 * Current client in this thread
	 */
	private Client currentCli;

	/**
	 * Channel to send messages to the client
	 */
	private ObjectOutputStream outStream;

	/**
	 * Channel to receive messages from the client
	 */
	private ObjectInputStream inStream;

	public ServerThread(Socket inSoc, TintolmarketServer s) {
		this.socket = inSoc;
		this.server = s;
		this.users = s.users;
		System.out.println("server:\tServer_thread initiated!!");
	}

	public void run() {
		try {

			this.outStream = new ObjectOutputStream(socket.getOutputStream());
			this.inStream = new ObjectInputStream(socket.getInputStream());

			String user = null;
			PublicKey pubKey = null;

			try {
				user = (String) inStream.readObject();

				String userFound = "";

				Scanner scanner = new Scanner(users);
				String currentLine;

				while (scanner.hasNextLine()) {
					currentLine = scanner.nextLine();
					if (currentLine.contains(user + ":")) {
						userFound = currentLine;
						break;
					}
				}
				scanner.close();

				// ja temos o userID

				// nonce:
				byte[] nonce = new byte[8];
				new SecureRandom().nextBytes(nonce);
				
				
				if (userFound.equals("")) { // New User
					// Send the nonce to the client
					String nonceStr = new String(nonce,StandardCharsets.ISO_8859_1);
					byte[] nonce2 = nonceStr.getBytes(StandardCharsets.ISO_8859_1);
					outStream.writeObject(nonceStr+":"+"true");
					
					//byte[] buff = new byte[265];
					//buff = (byte[]) inStream.readObject();
					
					//String answer = new String(buff, StandardCharsets.ISO_8859_1);
					String answer = (String) inStream.readObject();
					System.out.println("answer: "+answer);
					
					

					
					
					byte[] nonceReceived = answer.split(":")[0].getBytes(StandardCharsets.ISO_8859_1);
					
					
					if (Arrays.equals(nonce,nonceReceived)) {
						System.out.println("Nonce recebido está correto !!");
						// ir buscar certificado

						File certFile = receiveFile(SERVERPATH + user + ".crt");

						CertificateFactory cf = CertificateFactory.getInstance("X.509");
						FileInputStream fis = new FileInputStream(certFile.getPath());
						Certificate cert = cf.generateCertificate(fis);

						// Get the public key from the certificate
						pubKey = cert.getPublicKey();

						Signature ver = Signature.getInstance("SHA256withRSA");
						ver.initVerify(pubKey);
						ver.update(nonce);
						
						System.out.println("signedNonce :"+answer.split(":")[1]);
						
						
						
						byte[] signedNonce = answer.split(":")[1].getBytes();
						
						if (!ver.verify(answer.split(":")[1].getBytes())) { // NON AUTHORIZED
							
							System.out.println("server:\tNon Authorized Login!");
							outStream.writeObject("Non Authorized Login! Please Try Again!");
							System.exit(-1);
						} else {
							
							Client newCli = new Client(user, certFile.getPath());
							server.clients.put(user, newCli);
							this.currentCli = newCli;
							FileWriter writer = new FileWriter(users, true);

							synchronized (writer) {
								writer.write(user + ":" + certFile.getPath() + "\n");
								writer.close();
							}
							outStream.writeObject("true");
						}
					}

				} else { // Current User
					outStream.writeObject(nonce);
					outStream.flush();

					String signedNonce = (String) inStream.readObject();

					this.currentCli = server.clients.get(user);

					if (this.currentCli != null) {
						pubKey = loadPublicKey(this.currentCli.getPubKey());
						Signature ver = Signature.getInstance("SHA256withRSA");
						ver.initVerify(pubKey);
						ver.update(nonce);

						if (!ver.verify(signedNonce.getBytes())) { // NON AUTHORIZED
							System.out.println("server:\tNon Authorized Login!");
							outStream.writeObject("Non Authorized Login! Please Try Again!");
							System.exit(-1);
						}
					} else {
						System.out.println("server:\tUser: |" + user + "| not found !");
						outStream.writeObject("User not found !");
						System.exit(-1);
					}

				}

				String cmd = "";
	

				while (!(cmd = (String) inStream.readObject()).equals("-1")) {

					System.out.println("server:\tCommand received: " + cmd);
					String op = cmd.split("\\s+")[0];
					if (op.equals("a") || op.equals("add")) {
						addWine(cmd);
					} else if (op.equals("s") || op.equals("sell")) {
						sell(cmd);
					} else if (op.equals("v") || op.equals("view")) {
						view(cmd);
					} else if (op.equals("b") || op.equals("buy")) {
						buy(cmd);
					} else if (op.equals("w") || op.equals("wallet")) {
						wallet();
					} else if (op.equals("c") || op.equals("classify")) {
						classify(cmd);
					} else if (op.equals("t") || op.equals("talk")) {
						talk(cmd);
					} else if (op.equals("r") || op.equals("read")) {
						read(cmd);
					} else {
						outStream.writeObject("\n\tCommand not accepted");
					}
				}

			} catch (SocketException socket) {
				System.out.println("server:\t" + user + " logged out! - Connection ended");
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}
	}

	private File receiveFile(String filepath) throws IOException {
		
		File file = new File(filepath);
		file.createNewFile();
		FileOutputStream fout = new FileOutputStream(file,true);
        OutputStream output = new BufferedOutputStream(fout);
	    int totalsize = 0;
		try {
			totalsize = Integer.parseInt((String) (inStream.readObject()));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		FileOutputStream fos = new FileOutputStream(file);
	    byte[] buffer = new byte[1024];
	    int bytesRead;
	    while (totalsize > 0) {
	    	
	    	bytesRead = inStream.read(buffer);
	    	
	        output.write(buffer, 0, bytesRead);
	        
	        totalsize -= bytesRead;
	    }
	    fos.close();
	    output.close();
	    return file;
	}

	private void read(String cmd) throws IOException {
		StringBuilder sb = new StringBuilder();
		ArrayList<Message> msgs = server.messages.getMessages(this.currentCli);
		sb.append("Unread messages:\n");
		if (msgs != null) {
			for (Message m : msgs) {
				sb.append("\t  " + m.getSrc().getUser() + " sent: " + m.getMessage() + "\n");
			}
			server.messages.delMessages(this.currentCli);

			outStream.writeObject(sb.toString());

		} else {

			outStream.writeObject("Your Inbox is Clear!");

		}

	}

	private void talk(String cmd) throws IOException {
		String[] parts = cmd.split("\\s+");
		Client dest = server.clients.get(parts[1]);
		if (dest == null) {
			outStream.writeObject("Destination user not found!");

		} else {
			Message m = new Message(this.currentCli, dest, parts[2]);
			server.messages.addMessage(dest, m, false);
			outStream.writeObject("Message sent!");

		}

	}

	private void classify(String cmd) throws IOException {
		String[] parts = cmd.split("\\s+");
		Tintol tintol = this.server.wines.get(parts[1]);
		Double stars = Double.parseDouble(parts[2]);
		if (tintol != null) {
			tintol.classify(stars);
			this.outStream.writeObject(("The " + tintol.getName() + " has been rated with " + stars + " stars."));
		} else {
			this.outStream.writeObject(parts[1] + " not found!");
		}

	}

	private void wallet() throws IOException {

		this.outStream.writeObject(("Your current balance is: " + currentCli.getBalance() + " €"));

	}

	private void buy(String cmd) throws IOException {
		String[] parts = cmd.split("\\s+");
		Tintol tintol = server.wines.get(parts[1]);
		Client seller = server.clients.get(parts[2]);
		int quant = Integer.parseInt(parts[3]);

		if (tintol == null) {

			this.outStream.writeObject("Wine does not exist!");

		} else if (seller == null) {

			this.outStream.writeObject("Seller does not exist!");

		} else {
			Sell s = server.sells.getSell(seller, tintol);
			if (quant > s.getQuant()) {
				this.outStream.writeObject("Not enough units available");
			} else {
				if (server.sells.buy(tintol, seller, quant, this.currentCli)) {
					this.outStream.writeObject("U successfully bought: " + quant + " units of" + tintol.getName());
				} else {
					this.outStream.writeObject("Not enough balance available");
				}

			}
		}

	}

	private void view(String cmd) throws IOException {
		try {
			String[] parts = cmd.split("\\s+");
			String wineName = parts[1];
			Tintol tintol = server.wines.get(wineName);
			StringBuilder result = new StringBuilder();
			result.append(tintol.toString());
			for (Sell s : server.sells.getSellsByWine(tintol)) {
				result.append("\t" + s.getClient().getUser() + " has " + s.getQuant() + " units " + "at " + s.getPrice()
						+ " € per unit.\n");
			}

			outStream.writeObject(result.toString());
		} catch (NullPointerException eNull) {
			outStream.writeObject("Wine: " + cmd.split("\\s+")[1] + " not found!");
		}

	}

	private void sell(String cmd) throws IOException {
		// sell wine1 value quant
		String[] parts = cmd.split("\\s+");
		String wineName = parts[1];
		Double price = Double.parseDouble(parts[2]);
		int quant = Integer.parseInt(parts[3]);
		Tintol tintol = null;
		if ((tintol = server.wines.get(wineName)) != null) {
			Sell sell;
			if ((sell = server.sells.getSell(currentCli, tintol)) == null) {
				sell = new Sell(this.currentCli, tintol, quant, price);
				server.sells.add(sell);
				outStream.writeObject((String) (quant + " units of " + tintol.getName() + " put up for sale, for " + price
						+ "€ each!"));

			} else {
				sell.setQuant(sell.getQuant() + quant);
				sell.writeStats();
				outStream.writeObject((String) ("ERROR - this sale already exists! \n\t" + "Only sell quantity updated\n\t")
						+ (quant + " units of " + tintol.getName() + " added in previous sale"));

			}

		} else {
			outStream.writeObject((String) ("Error - Wine not found!\n"));
		}

	}

	private void addWine(String cmd) {

		String[] parts = cmd.split("\\s+");
		String fileName = parts[2];
		byte[] buff = new byte[1024];

		File img = new File(WINESPATH + parts[1] + "." + parts[2].split("\\.")[1]);
		img.delete();
		try {
			
			if (!img.createNewFile()) {
				System.out.println("server:\tFile: " + fileName + " -> NOT CREATED");
			} else {
				FileOutputStream fout = new FileOutputStream(img, true);
				OutputStream output = new BufferedOutputStream(fout);

				long size = 0;

				try {
					size = (long) (inStream.readObject());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				int alreadyRead = 0;
				while (alreadyRead < size) {
					String line = null;
					try {
						line = (String) inStream.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (line == null) {
						break;
					}
					output.write(line.getBytes());
					alreadyRead += line.length();
				}
				output.close();
				fout.close();

				Tintol tintol = new Tintol(parts[1], img);
				if (server.wines.get(parts[1]) != null) {
					outStream.writeObject("Error - Wine already exists!");
					System.out.println("server:\tError - Wine already exists!");
				} else {
					server.wines.put(tintol.getName(), tintol);
					outStream.writeObject((String) ("Tintol - " + tintol.getName() + " added!"));
					System.out.println("server:\t(Tintol) - " + tintol.getName() + " added!");
				}

			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public PublicKey loadPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(spec);
		return publicKey;
	}
}
