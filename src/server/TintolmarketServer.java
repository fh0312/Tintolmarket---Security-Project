package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * @author Alexandre Müller - FC56343 Diogo Ramos - FC56308 Francisco Henriques
 *         - FC56348
 *
 */

public class TintolmarketServer {

	// ANETS DE USAR CADA UM DESTES FICHEIROS VERIFICAR A INTEGRIDADE dos mesmos
	private static final String SERVERPATH = "server_files//";
	private static final String WINESPATH = SERVERPATH + "wines//";
	private static final String CLIPATH = SERVERPATH + "users//";
	private static final String MSGPATH = SERVERPATH + "messages//";
	private static final String BLKPATH = SERVERPATH + "blks//";

	private static int PORT;

	private static String KEYSTORE_PATH = "server.keystore";
	private static String TRUSTSTORE_PATH = "server.truststore";
	private static String KEYSTORE_PASSWORD = "keystore_password";
	private static String TRUSTSTORE_PASSWORD = "truststore_password";
	private static String CIPHER_PASSWORD = "pbe_password"; // cifra para cifrar o ficheiro de users - PBE - AES 128bits

	private static final String ALGORITHM = "AES";
	private static final int KEY_SIZE = 128;
	private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256";

	protected ConcurrentHashMap<String, Client> clients;

	protected ConcurrentHashMap<String, Tintol> wines;

	protected SellsCatalog sells;

	protected File users;

	protected MessageCatalog messages;
	
	protected List<Block> blks;
	
	protected int trsCounter;

	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		if (args.length == 0) {
			PORT = 12345;
			KEYSTORE_PATH = "keystore.server";
			KEYSTORE_PASSWORD = "adminadmin";
			CIPHER_PASSWORD = "adminadmin";

			// TODO - remover !!
		} else if (args.length != 1) {
			System.err.println(
					"Modo de Uso: TintolmarketServer <port>" + "<password-cifra> <keystore> <password-keystore>");
			System.exit(-1);
		} else {
			PORT = (int) Integer.parseInt(args[0]);
			CIPHER_PASSWORD = args[1];
			KEYSTORE_PATH = args[2];
			KEYSTORE_PASSWORD = args[3];
		}

		System.out.println("server:\tTintolmarketServer initiated in port: " + PORT);
		TintolmarketServer server = new TintolmarketServer();
		try {
			server.startServer();
		} catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException | CertificateException
				| KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public TintolmarketServer() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		this.clients = new ConcurrentHashMap<String, Client>();
		this.wines = new ConcurrentHashMap<String, Tintol>();
		this.users = new File(SERVERPATH + "users.cif");
		this.sells = new SellsCatalog();
		this.messages = new MessageCatalog();
		this.blks = new ArrayList<>();
		if (users.exists()) {
			new IntegrityVerifier().verifyFile(users);
			loadWines();
			loadUsers();
			loadMessages();
		} else {
			try {
				new File(SERVERPATH.substring(0, SERVERPATH.length() - 2)).mkdir();
				users.createNewFile();
				encryptFile(users, CIPHER_PASSWORD);
				users = new File(users.getPath().substring(0, users.getPath().length()-4)+".cif");
				
				new File(WINESPATH.substring(0, WINESPATH.length() - 2)).mkdir();
				new File(MSGPATH.substring(0, MSGPATH.length() - 2)).mkdir();
				new File(CLIPATH.substring(0, CLIPATH.length() - 2)).mkdir();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadMessages() {
		this.messages.load_msgs(clients);
	}

	private void loadWines() {
		File winesDir = new File(SERVERPATH + "wines");
		if (winesDir.listFiles() != null) {
			for (File wineData : winesDir.listFiles()) {
				String tintolName = wineData.getName().split("\\.")[0];
				String ext = wineData.getName().split("\\.")[1];
				File img = null;
				if (!wines.containsKey(tintolName)) {
					if (ext.equals("txt")) {
						for (File data : winesDir.listFiles()) {
							if (data.getName().contains(tintolName)) {
								String extImg = data.getName().split("\\.")[1];
								if (!extImg.equals("txt")) {
									img = data;
								}
							}
						}

						this.wines.put(tintolName, new Tintol(tintolName, img, wineData));
					}

				}
			}
		}
	}
	
	private void loadUsers() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {
		try {
			
			String content = decrypt(users, CIPHER_PASSWORD);
			Scanner scanner = new Scanner(content);
			String currentLine;
			while (scanner.hasNextLine()) { // For each user in user.txt file :
				currentLine = scanner.nextLine();
				String user = currentLine.split(":")[0];
				File clientData = new File(CLIPATH + user + ".txt");
				Client newClient = new Client(clientData);

				System.out.println("server:\tUser: " + user + " loaded");

				this.clients.put(user, newClient);
				Scanner scCli = new Scanner(clientData);
				scCli.nextLine();
				scCli.nextLine();
				while (scCli.hasNextLine()) {
					String s = scCli.nextLine();
					String tintol = s.split("=")[0];
					Tintol wine = this.wines.get(tintol);
					int quant = Integer.parseInt(s.split("=")[1].split(";")[0]);
					Double price = Double.parseDouble(s.split("=")[1].split(";")[1]);
					this.sells.load(new Sell(newClient, wine, quant, price));
				}
				scCli.close();
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void startServer() throws NoSuchAlgorithmException, CertificateException, KeyStoreException,
			UnrecoverableKeyException, KeyManagementException {
		SSLServerSocket sSoc = null;
		try {

			System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
			System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);

			ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
			sSoc = (SSLServerSocket) ssf.createServerSocket(PORT);

			// Wait for client connection
			System.out.println("Waiting for client connection...");

		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		while (true) {
			try {
				new ServerThread(sSoc.accept(), this).start();
				System.out.println("New client connected");
			} catch (Exception e) {
				try {
					sSoc.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}

		}

	}

	private String decrypt(File cif, String pwd) throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, InvalidKeySpecException {
		boolean b = new IntegrityVerifier().verifyFile(cif);
//		if(!b) {
//			System.out.println("\n\nIntegridade VIOLADA\n\n");
//		}
		FileInputStream fs = new FileInputStream(cif);
		byte[] enc = fs.readAllBytes();

		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea,
				(byte) 0xf2 };
		PBEKeySpec keySpec = new PBEKeySpec(pwd.toCharArray(), salt, 20);
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		SecretKey key = kf.generateSecret(keySpec);
		
		
		File file = new File("params.params");
        byte[] params = new byte[(int) file.length()];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(params);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

		AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
		p.init(params);
		Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		d.init(Cipher.DECRYPT_MODE, key, p);
		byte[] dec = d.doFinal(enc);
		fs.close();
		return new String(dec);
	}

	private static void encryptFile(File aux, String pwd) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IOException, InvalidKeySpecException {
		new IntegrityVerifier().updateFile(aux);
		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea,
				(byte) 0xf2 };
		PBEKeySpec keySpec = new PBEKeySpec(pwd.toCharArray(), salt, 20);
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		SecretKey key = kf.generateSecret(keySpec);

		Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		c.init(Cipher.ENCRYPT_MODE, key);

		FileInputStream fis;
		FileOutputStream fos;
		CipherOutputStream cos;
		fis = new FileInputStream(aux);
		String newFileName = aux.getPath().split("\\.")[0] + ".cif";
		fos = new FileOutputStream(newFileName);

		cos = new CipherOutputStream(fos, c);
		byte[] b = new byte[16];
		int i = fis.read(b);
		while (i != -1) {
			cos.write(b, 0, i);
			i = fis.read(b);
		}
		cos.close();
		fis.close();
		fos.close();

		byte[] params = c.getParameters().getEncoded();
		
		File paramsFile = new File("params.params");
		paramsFile.createNewFile();
		try {
            FileOutputStream foss = new FileOutputStream(paramsFile.getPath());
            foss.write(params);
            foss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void writeNewClient(String string) {
		try {
			StringBuilder sb = new StringBuilder();
			String content = decrypt(users,CIPHER_PASSWORD);
			sb.append(content);
			File temp = new File(users.getPath().substring(0, users.getPath().length()-4)+".txt");
			temp.createNewFile();
			sb.append(string);
			
			FileWriter writer = new FileWriter(temp);
			writer.write(sb.toString());
			writer.close();
			encryptFile(temp, CIPHER_PASSWORD);
			temp.delete();
		}
		catch(Exception e) {
			
		}
	}

	public String getUsersContent() {
		try {
			return decrypt(users, CIPHER_PASSWORD);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void addTrToBlock(Transacao t){
		if(this.blks.size()==0)
			this.blks.add(new Block(null, 1));
		else {
			Block current = blks.get(blks.size()-1);
			if(current.getN_trx() <5) {
				current.addTr(t);
			}
			else {
				createNewBlock();
			}
		}
	}
	
	private void createNewBlock() {
		byte[] oldHash =  blks.get(blks.size()-1).close();
		this.blks.add(new Block(oldHash, blks.get(blks.size()-1).getBlk_id()+1));
	}
	
	
	
}
