package server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
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
	private PrintWriter outStream;

	/**
	 * Channel to receive messages from the client
	 */
	private BufferedReader inStream;

	public ServerThread(Socket inSoc, TintolmarketServer s) {
		this.socket = inSoc;
		this.server = s;
		this.users = s.users;
		System.out.println("server:\tServer_thread initiated!!");
	}

	public void run() {
		try {

			// Create input/output streams
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

			this.outStream = output;
			this.inStream = input;

			String user = null;
			PublicKey pubKey = null;
			
			
			try {
				user = (String) inStream.readLine();

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
				
				//ja temos o userID
				
				//nonce:
				byte[] nonce = new byte[8];
				new SecureRandom().nextBytes(nonce);
				
				if (userFound.equals("")) { 											// New User
					// Send the nonce to the client
					outStream.println(nonce+":"+true);
					outStream.flush();
					
					//TODO - Parte do novo cliente 
					
					
//					Client newCli = new Client(user, passwd);
//					server.clients.put(user, newCli);
//					this.currentCli = newCli;
//					FileWriter writer = new FileWriter(users, true);

//					synchronized (writer) {
//						writer.write(user + ":" + passwd + "\n");
//						writer.close();
//					}
//					outStream.println("true");

				} else { 																// Current User
					
					outStream.println(nonce);
					outStream.flush();
					
					String signedNonce = inStream.readLine();
					
					this.currentCli = server.clients.get(user);
					
					if(this.currentCli!=null) {
						pubKey = loadPublicKey(this.currentCli.getPubKey());
						Signature ver = Signature.getInstance("SHA256withRSA");
						ver.initVerify(pubKey);
						ver.update(nonce);
						
						if(!ver.verify(signedNonce.getBytes())) { // NON AUTHORIZED
							System.out.println("server:\tNon Authorized Login!");
							outStream.println("Non Authorized Login! Please Try Again!");
							System.exit(-1);
						}
					}
					else {
						System.out.println("server:\tUser: |"+user+"| not found !");
						outStream.println("User not found !");
						System.exit(-1);
					}
					

				}
				String cmd = "";

				while ((cmd = (String) inStream.readLine()) != "-1") {

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
						outStream.println("\n\tCommand not accepted");
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

	private void read(String cmd) {
		StringBuilder sb = new StringBuilder();
		ArrayList<Message> msgs = server.messages.getMessages(this.currentCli);
		sb.append("Unread messages:\n");
		if (msgs != null) {
			for (Message m : msgs) {
				sb.append("\t  " + m.getSrc().getUser() + " sent: " + m.getMessage() + "\n");
			}
			server.messages.delMessages(this.currentCli);

			outStream.println(sb.toString());

		} else {

			outStream.println("Your Inbox is Clear!");

		}

	}

	private void talk(String cmd) {
		String[] parts = cmd.split("\\s+");
		Client dest = server.clients.get(parts[1]);
		if (dest == null) {
			outStream.println("Destination user not found!");

		} else {
			Message m = new Message(this.currentCli, dest, parts[2]);
			server.messages.addMessage(dest, m, false);
			outStream.println("Message sent!");

		}

	}

	private void classify(String cmd) {
		String[] parts = cmd.split("\\s+");
		Tintol tintol = this.server.wines.get(parts[1]);
		Double stars = Double.parseDouble(parts[2]);
		if (tintol != null) {
			tintol.classify(stars);
			this.outStream.println(("The " + tintol.getName() + " has been rated with " + stars + " stars."));
		} else {
			this.outStream.println(parts[1] + " not found!");
		}

	}

	private void wallet() {

		this.outStream.println(("Your current balance is: " + currentCli.getBalance() + " €"));

	}

	private void buy(String cmd) {
		String[] parts = cmd.split("\\s+");
		Tintol tintol = server.wines.get(parts[1]);
		Client seller = server.clients.get(parts[2]);
		int quant = Integer.parseInt(parts[3]);

		if (tintol == null) {

			this.outStream.println("Wine does not exist!");

		} else if (seller == null) {

			this.outStream.println("Seller does not exist!");

		} else {
			Sell s = server.sells.getSell(seller, tintol);
			if (quant > s.getQuant()) {
				this.outStream.println("Not enough units available");
			} else {
				if (server.sells.buy(tintol, seller, quant, this.currentCli)) {
					this.outStream.println("U successfully bought: " + quant + " units of" + tintol.getName());
				} else {
					this.outStream.println("Not enough balance available");
				}

			}
		}

	}

	private void view(String cmd) {
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

			outStream.println(result.toString());
		} catch (NullPointerException eNull) {
			outStream.println("Wine: " + cmd.split("\\s+")[1] + " not found!");
		}

	}

	private void sell(String cmd) {
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
				outStream.println((String) (quant + " units of " + tintol.getName() + " put up for sale, for " + price
						+ "€ each!"));

			} else {
				sell.setQuant(sell.getQuant() + quant);
				sell.writeStats();
				outStream.println((String) ("ERROR - this sale already exists! \n\t" + "Only sell quantity updated\n\t")
						+ (quant + " units of " + tintol.getName() + " added in previous sale"));

			}

		} else {
			outStream.println((String) ("Error - Wine not found!\n"));
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

				size = Long.parseLong(inStream.readLine());

				int alreadyRead = 0;
				while (alreadyRead < size) {
					String line = inStream.readLine();
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
					outStream.println("Error - Wine already exists!");
					System.out.println("server:\tError - Wine already exists!");
				} else {
					server.wines.put(tintol.getName(), tintol);
					outStream.println((String) ("Tintol - " + tintol.getName() + " added!"));
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
