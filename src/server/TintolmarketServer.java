package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.DigestInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
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

import server.transacao.Transacao;
import server.transacao.TransacaoBuy;
import server.transacao.TransacaoSell;

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
	///private static String TRUSTSTORE_PATH = "server.truststore";
	private static String KEYSTORE_PASSWORD = "keystore_password";
	//private static String TRUSTSTORE_PASSWORD = "truststore_password";
	private static String CIPHER_PASSWORD = "pbe_password"; // cifra para cifrar o ficheiro de users - PBE - AES 128bits


	protected ConcurrentHashMap<String, Client> clients;

	protected ConcurrentHashMap<String, Tintol> wines;

	protected SellsCatalog sells;

	protected File usersFile;

	protected MessageCatalog messages;
	
	protected ArrayList<Block> blks;
	
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
		this.usersFile = new File(SERVERPATH + "users.cif");
		this.sells = new SellsCatalog();
		this.messages = new MessageCatalog();
		this.blks = new ArrayList<>();
		if (usersFile.exists()) {
			verifyAllFiles();
			loadWines();
			loadUsers();
			loadMessages();
			loadBlocks();
		} else {
			try {
				new File(SERVERPATH.substring(0, SERVERPATH.length() - 2)).mkdir();
				usersFile.createNewFile();
				encryptFile(usersFile, CIPHER_PASSWORD);
				usersFile = new File(usersFile.getPath().substring(0, usersFile.getPath().length()-4)+".cif");
				
				new File(WINESPATH.substring(0, WINESPATH.length() - 2)).mkdir();
				new File(MSGPATH.substring(0, MSGPATH.length() - 2)).mkdir();
				new File(CLIPATH.substring(0, CLIPATH.length() - 2)).mkdir();
				updateAllFiles();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadBlocks() {
		File blocksDir = new File(BLKPATH);
		for(File bFile : blocksDir.listFiles()) {
			try {
				Scanner sc = new Scanner(bFile);
				String str = sc.nextLine();
				byte[] hashBlkAnterior = str.substring(1, str.length() - 1)
                        				.replaceAll("\\s", "").getBytes();
				
				str= sc.nextLine();
				long id = Long.parseLong(str.split("=")[1].trim());
				
				
				
				str= sc.nextLine();
				int num =  Integer.parseInt(str.split("=")[1].trim());
				ArrayList<Transacao> trs = new ArrayList<>();
				while (sc.hasNextLine()) {
					str= sc.nextLine();
					if(str.contains("Transacao")) {
						String prop = str.split("->")[1];
							if(str.contains("Buy")){
								String[] parts = prop.split(",");
								
								int unidadesCriadas  = Integer.parseInt(parts[0].split("=")[1]);
								String idDono  = parts[1].split("=")[1];
								String nomeVinho  = parts[2].split("=")[1];
								double valorPerUnit  = Double.parseDouble(parts[3].split("=")[1]);
								byte[] assinatura  = new IntegrityVerifier().hexStringToByteArray(parts[4].split("=")[1]);
								TransacaoBuy t = new TransacaoBuy(nomeVinho, valorPerUnit, unidadesCriadas, idDono, assinatura);
								trs.add(t);
							}
							else if(str.contains("Sell")){
								String[] parts = prop.split(",");
								int unidadesCriadas  = Integer.parseInt(parts[0].split("=")[1]);
								String idDono  = parts[1].split("=")[1];
								String nomeVinho  = parts[2].split("=")[1];
								double valorPerUnit  = Double.parseDouble(parts[3].split("=")[1]);
								byte[] assinatura  = new IntegrityVerifier().hexStringToByteArray(parts[4].split("=")[1]);
								TransacaoSell t = new TransacaoSell(nomeVinho,valorPerUnit,unidadesCriadas,idDono,assinatura);
								trs.add(t);
							}
						}
					else {
						break;
					}
				}
				byte[] ass = new IntegrityVerifier().hexStringToByteArray(str);
				Block b = new Block(hashBlkAnterior,id,ass,trs);
				sc.close();
				this.blks.add(b);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		try {
			verificaBlockChain();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * Funcao que verifica a integridade da blockChain comparando o hash guardado no bloco 
	 * seguinte com o hash do ficheiro neste momento
	 * 
	 * @throws Exception - inidicando que a integridade foi violada
	 */
	private void verificaBlockChain() throws Exception {
		for(Block b : this.blks) {
			if(b.getBlk_id()<this.blks.size()) {
				File file = b.getFile();
				try {
					FileInputStream inputStream = new FileInputStream(file);
		            MessageDigest digest = MessageDigest.getInstance("SHA-256");
		            DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);
		            byte[] buff = new byte[4096];
		            while (digestInputStream.read(buff) > -1);
		            byte[] thisBlockHash = digest.digest();
		            Block nextBlock =  this.blks.get( (int) b.getBlk_id() );
		            String str = Arrays.toString(thisBlockHash);
		            byte[] bytes = str.substring(1, str.length() - 1).replaceAll("\\s", "").getBytes();
		            if(!MessageDigest.isEqual(bytes,nextBlock.getHash())) {
		            	throw new Exception("Blockchain integrity violated !");
		            }
				}
				catch(Exception e) {
					throw new Exception("Blockchain integrity violated !");
				}
			}
		}
		
	}
	
	/**
	 * Verifica a integridade de todos os ficheiros no servidor
	 */
	private void verifyAllFiles() {
		File dir = new File (SERVERPATH);
		for(File f : dir.listFiles()) {
			new IntegrityVerifier().verifyIntegrity(f);
		}
	}
	
	/**
	 * Faz o update(integridade) a todos os ficheiros no inicio do servidor (sem ficheiros)
	 * 
	 * @requires Ficheiros tenham acabado de ser criados pois anula os efeitos da verificacao de integridade.
	 */
	private void updateAllFiles() {
		File dir = new File (SERVERPATH);
		for(File f : dir.listFiles()) {
			new IntegrityVerifier().updateIntegrity(f);
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
			
			String content = decrypt(usersFile, CIPHER_PASSWORD);
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
			new IntegrityVerifier().updateFile(usersFile);
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
	
	
	/**
	 * Desencripta um ficheiro cifrado, tendo por base uma password
	 * 
	 * @param cif - ficheiro a decifrar
	 * @param pwd - password
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeySpecException
	 */
	private String decrypt(File cif, String pwd) throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, InvalidKeySpecException {
		try {
			new IntegrityVerifier().verifyFile(cif);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
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
        new IntegrityVerifier().updateIntegrity(file);

		AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
		p.init(params);
		Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		d.init(Cipher.DECRYPT_MODE, key, p);
		byte[] dec = d.doFinal(enc);
		fs.close();
		new IntegrityVerifier().updateFile(cif);
		return new String(dec);
	}

	/**
	 * Encripta um ficheiro tenod em conta uma password
	 * 
	 * @param aux - ficheiro a encriptar
	 * @param pwd - password
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
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
		new IntegrityVerifier().updateFile(aux);
		new IntegrityVerifier().updateFile(paramsFile);
	}

	public void writeNewClient(String string) {
		try {
			StringBuilder sb = new StringBuilder();
			String content = decrypt(usersFile,CIPHER_PASSWORD);
			sb.append(content);
			File temp = new File(usersFile.getPath().substring(0, usersFile.getPath().length()-4)+".txt");
			temp.createNewFile();
			sb.append(string);
			
			FileWriter writer = new FileWriter(temp);
			writer.write(sb.toString());
			writer.close();
			encryptFile(temp, CIPHER_PASSWORD);
			new IntegrityVerifier().updateFile(usersFile);
			temp.delete();
		}
		catch(Exception e) {
			
		}
	}
	
	/**
	 * Obtem o conteudo do ficheiro users.cif e retornando-o de modo a que o ficheiro continue cifrado.
	 * 
	 * @return - conteudo do ficheiro users.cif decifrado.
	 */
	public String getUsersContent() {
		try {
			new IntegrityVerifier().updateFile(usersFile);
			return decrypt(usersFile, CIPHER_PASSWORD);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Adiciona uma nova transacao ao bloco corrente
	 * @param t
	 */
	public void addTrToBlock(Transacao t){
		if(this.blks.size()==0) {
			Block b = new Block(null, 1);
			try {
				b.setPkey(getPkey());
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableEntryException
					| IOException e) {
				e.printStackTrace();
			}
			this.blks.add(b);
		}
		else {
			Block current = blks.get(blks.size()-1);
			if(current.getN_trx() <5) {
				current.addTr(t);
			}
			else {
				createNewBlock();
				current = blks.get(blks.size()-1);
				current.addTr(t);
			}
		}
	}
	
	/**
	 * Fecha o bloco corrente, cria um novo e adiciona-o à lista de blocos
	 */
	private void createNewBlock() {
		byte[] oldHash =  blks.get(blks.size()-1).close();
		Block b = new Block(oldHash, blks.get(blks.size()-1).getBlk_id()+1);
		try {
			b.setPkey(getPkey());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.blks.add(b);
	}
	
	
	private PrivateKey getPkey() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableEntryException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		FileInputStream fis = new FileInputStream(KEYSTORE_PATH);
		keyStore.load(fis,KEYSTORE_PASSWORD.toCharArray());
		fis.close();
		
		KeyStore.PrivateKeyEntry pKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("myServer",
				new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray()));
		
		return pKeyEntry.getPrivateKey();
		
	}
	
	
	
}
