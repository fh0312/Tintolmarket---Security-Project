package server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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
		this.users = s.usersFile;
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
				
				String usersCont = server.getUsersContent();
				Scanner scanner = new Scanner(usersCont);
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

					String nonceStr = new String(nonce, StandardCharsets.ISO_8859_1);
					outStream.writeObject(nonceStr + ":" + "true");

					String answer1 = (String) inStream.readObject();
					String answer2 = (String) inStream.readObject();
					

					byte[] nonceReceived = answer1.getBytes(StandardCharsets.ISO_8859_1);

					if (Arrays.equals(nonce, nonceReceived)) {
						System.out.println("server:\t Nonce received is correct !!\n\n");


						File certFile = receiveFile(SERVERPATH + user + ".crt");

						CertificateFactory cf = CertificateFactory.getInstance("X.509");
						FileInputStream fis = new FileInputStream(certFile.getPath());
						Certificate cert = cf.generateCertificate(fis);

				
						pubKey = cert.getPublicKey();

						Signature ver = Signature.getInstance("SHA256withRSA");
						ver.initVerify(pubKey);
						ver.update(nonce);


						byte[] signedNonce = answer2.getBytes(StandardCharsets.ISO_8859_1);
						new IntegrityVerifier().updateIntegrity(certFile);

						if (!ver.verify(signedNonce)) { // NON AUTHORIZED

							System.out.println("server:\tNon Authorized Login!");
							outStream.writeObject("server:\tNon Authorized Login! Please Try Again!");
							System.exit(-1);
						} else {

							Client newCli = new Client(user, certFile.getPath());

							server.clients.put(user, newCli);
							this.currentCli = newCli;
							
							server.writeNewClient(user + ":" + certFile.getPath() + "\n");

							outStream.writeObject("true");
						}
					}

				} else { // Current User

					// Send the nonce to the client
					String nonceStr = new String(nonce, StandardCharsets.ISO_8859_1);
					outStream.writeObject(nonceStr);

					String signedNonceStr = (String) inStream.readObject();
					byte[] signedNonce = signedNonceStr.getBytes(StandardCharsets.ISO_8859_1);

					this.currentCli = server.clients.get(user);

					if (this.currentCli != null) {
						pubKey = loadPublicKey(this.currentCli.getCert());
						Signature ver = Signature.getInstance("SHA256withRSA");
						ver.initVerify(pubKey);
						ver.update(nonce);

						if (!ver.verify(signedNonce)) { // NON AUTHORIZED
							System.out.println("server:\tNon Authorized Login!");
							outStream.writeObject("Non Authorized Login! Please Try Again!");
							System.exit(-1);
						} else {
							outStream.writeObject("true");
							System.out.println("server:\tLogin Authorized!");
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
						sell(cmd,(byte[])inStream.readObject());
					} else if (op.equals("v") || op.equals("view")) {
						view(cmd);
					} else if (op.equals("b") || op.equals("buy")) {
						buy(cmd,(byte[])inStream.readObject());
					} else if (op.equals("w") || op.equals("wallet")) {
						wallet();
					} else if (op.equals("c") || op.equals("classify")) {
						classify(cmd);
					} else if (op.equals("t") || op.equals("talk")) {
						talk(cmd, (byte[]) inStream.readObject());
					} else if (op.equals("r") || op.equals("read")) {
						read(cmd);
					} else if (op.equals("l") || op.equals("list")) {
						getTransactionsList();
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
			System.out.println("server:\\tError\n");
			e.printStackTrace();
		}
	}

	private void getTransactionsList() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("\tTransacoes efetuadas:\n");
		for(Block b:  server.blks) {
			for(Transacao t : b.getTrx() )
				sb.append(t.toString()+"\n");
		}
		outStream.writeObject(sb.toString());
	}

	private File receiveFile(String filepath)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {

		File file = new File(filepath);
		file.delete();
		file.createNewFile();

		FileOutputStream fout = new FileOutputStream(file, true);
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
		new IntegrityVerifier().updateFile(file);
		return file;
	}

	private void read(String cmd) throws IOException {
		ArrayList<Message> msgs = server.messages.getMessages(this.currentCli);

		if (msgs != null) {
			outStream.writeObject(msgs.size());// envio do numero de mensagens
			outStream.writeObject("Unread messages:\n");
			for (Message m : msgs) {
				outStream.writeObject(("\n\t  " + m.getSrc().getUser() + " sent: "));// envio string
				outStream.writeObject(m.getMessage()); // envio bytes
			}
			server.messages.delMessages(this.currentCli);

		} else {
			outStream.writeObject(0);
			outStream.writeObject("Your inbox is clear!");
		}

	}

	private void talk(String cmd, byte[] bs) throws IOException {
		String[] parts = cmd.split("\\s+");
		Client dest = server.clients.get(parts[1]);

		if (dest == null) {
			outStream.writeObject("Destination user not found!");

		} else {

			Message m = new Message(this.currentCli, dest, bs);
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

	private void buy(String cmd, byte[] bs) throws IOException {
		
		//bs é o byte[] signed que veio do cliente - TODO
		
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
					server.addTrToBlock(new TransacaoBuy(tintol.getName(), s.getPrice(), quant, this.currentCli.getUser(),bs));
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

	private void sell(String cmd, byte[] bs) throws IOException {
		
		
		//bs é o byte[] signed que veio do cliente - TODO
		
		
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
				outStream.writeObject((String) (quant + " units of " + tintol.getName() + " put up for sale, for "
						+ price + "€ each!"));
				server.addTrToBlock(new TransacaoSell(wineName,price,quant,this.currentCli.getUser(),bs));

			} else {
				sell.setQuant(sell.getQuant() + quant);
				sell.writeStats();
				outStream.writeObject(
						(String) ("ERROR - this sale already exists! \n\t" + "Only sell quantity updated\n\t")
								+ (quant + " units of " + tintol.getName() + " added in previous sale"));
				server.addTrToBlock(new TransacaoSell(wineName,price,quant,this.currentCli.getUser(),bs));
			}

		} else {
			outStream.writeObject((String) ("Error - Wine not found!\n"));
		}

	}

	private void addWine(String cmd) throws IOException {
		System.out.println("cmd is " + cmd);
		String[] parts = cmd.split("\\s+");

		// File img = new File(WINESPATH + parts[1] + "." + parts[2].split("\\.")[1]);
		File img = null;
		try {
			img = receiveFile(WINESPATH + parts[1] + "." + parts[2].split("\\.")[1]);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Tintol tintol = new Tintol(parts[1], img);
		if (server.wines.get(parts[1]) != null) {
			outStream.writeObject("Error - Wine already exists!");
			System.out.println("server:\tError - Wine already exists!");
		} else {
			server.wines.put(tintol.getName(), tintol);
			outStream.writeObject((String) ("Tintol - " + tintol.getName() + " added!"));
			System.out.println("server:\t(Tintol) - " + tintol.getName() + " added!");
			
		}
		new IntegrityVerifier().updateIntegrity(img);
		new IntegrityVerifier().updateIntegrity(users);

	}

	public PublicKey loadPublicKey(Certificate cert) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return cert.getPublicKey();
	}
}
