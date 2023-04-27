package server;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class IntegrityVerifier {
	
	private final String HASH_ALGORITHM = "HmacSHA256";
   
	
    private SecretKey secretKey;
    private File hashLog;
    private HashMap<String, byte[]> fileHashes;
    private final String SERVER_PATH = "server_files//";
    private final String pwd = "adminadmin";
    
    private final String HASH_LOG_FILENAME = SERVER_PATH+"hashes.txt";

    public IntegrityVerifier() {
    	byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea,
				(byte) 0xf2 };
		PBEKeySpec keySpec = new PBEKeySpec(pwd.toCharArray(), salt, 20);
		SecretKeyFactory kf;
		SecretKey key=null;
		try {
			kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
			key = kf.generateSecret(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
			e1.printStackTrace();
		}
		this.secretKey=key;
        this.fileHashes = new HashMap<>();
        this.hashLog = new File(SERVER_PATH + "hashes.txt");
        try {
            this.hashLog.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        load();
        writeAll();
    }

    public boolean verifyFile(File file) throws Exception {
        load();
    	byte[] storedHash = this.fileHashes.get(file.getPath());
        if (storedHash == null) {
            return false;
        }
        byte[] currentHash = getHmac(file);
        
//        System.out.println("Novo   HMAC: "+byteArrayToHexString(currentHash));
//        System.out.println("Antigo HMAC: "+byteArrayToHexString(storedHash));
        
        boolean ret = MessageDigest.isEqual(storedHash, currentHash);
        
        if(!ret) {
        	throw new Exception(file.getPath()+" integrity violated. Aborting...");
        }
        
        return ret;
    }
	
    public void updateFile(File file) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
    	load();
    	byte[] hmac = getHmac(file);
    	if(this.fileHashes.containsKey(file.getPath())) {
//    		System.out.println(file.getPath()+" -> Antes do update: "+byteArrayToHexString(this.fileHashes.get(file.getPath())));
//        	
//        	System.out.println(file.getPath()+" -> Depois do update: "+byteArrayToHexString(hmac));
    	}
    	else {
//    		System.out.println(file.getPath()+" -> Criado com hmac: "+byteArrayToHexString(hmac));
    	}
    	
    	
    	//System.out.flush();
    	
        this.fileHashes.remove(file.getPath());
        writeAll();
        this.fileHashes.put(file.getPath(), hmac);
        write(file.getPath(), hmac);
        
    }
    
//    private HashMap<String, byte[]> loadFile() throws IOException {
//        HashMap<String, byte[]> fileHashes = new HashMap<>();
//        BufferedReader reader = new BufferedReader(new FileReader(this.hashLog));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            String[] parts = line.split(" ");
//            String path = parts[0];
//            byte[] hash = hexStringToByteArray(parts[1]);
//            fileHashes.put(path, hash);
//        }
//        reader.close();
//        return fileHashes;
//    }
    
    private void writeAll() {
    	try {
			new FileWriter(this.hashLog.getPath(), false).close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
    	try {
			this.hashLog.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(String key : this.fileHashes.keySet()) {
			try {
				write(key, this.fileHashes.get(key));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void write(String path, byte[] data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(HASH_LOG_FILENAME, true));
        writer.write(path + "==>" + byteArrayToHexString(data)+";-;");
        writer.newLine();
        writer.close();
    }
    
    private void load() {
    	HashMap<String,byte[]> map = new HashMap<>();
    	try {
			Scanner sc = new Scanner(hashLog);
			while(sc.hasNextLine()) {
				String s = sc.nextLine();
				if(s.contains(";-;")) {
					String[] parts = s.split("==>");
					if(parts.length==2) {
						map.put(parts[0], hexStringToByteArray(parts[1].substring(0, parts[1].length()-3)));
					}
					else {
						System.out.println("\n\nERRO - loadError\n\n");
					}
				}
			}
			sc.close();
			this.fileHashes = map;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    }
    
    private byte[] getHmac(File file) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Mac hmac = Mac.getInstance(HASH_ALGORITHM);
        hmac.init(this.secretKey);
        byte[] fileData = Files.readAllBytes(file.toPath());
        return hmac.doFinal(fileData);
    }
    
//    private static void removeLine(String fileName, String stringToRemove) throws IOException {
//        File inputFile = new File(fileName);
//        File tempFile = new File("temp.txt");
//        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
//        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            // If the line contains the string to remove, skip it
//            if (line.contains(stringToRemove)) {
//                continue;
//            }
//            // Otherwise, write the line to the temp file
//            writer.write(line);
//            writer.newLine();
//        }
//        // Close the input and output files
//        reader.close();
//        writer.close();
//        // Delete the original file and rename the temp file to the original filename
//        if (!inputFile.delete()) {
//            throw new IOException("Failed to delete file: " + inputFile);
//        }
//        if (!tempFile.renameTo(inputFile)) {
//            throw new IOException("Failed to rename file: " + tempFile + " -> " + inputFile);
//        }
//    }
    
    private String byteArrayToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
	public void updateIntegrity(File file) {
		try {
			new IntegrityVerifier().updateFile(file);
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void verifyIntegrity(File file) {
		try {
			new IntegrityVerifier().verifyFile(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    
}
