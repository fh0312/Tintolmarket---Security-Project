package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AuxClass {

	public static void main(String[] args) throws Exception {

		File aux = new File("aux.txt");
		aux.createNewFile();
		PrintWriter pw = new PrintWriter(aux);
		pw.append("aaa:ashjdbasdhbhjbad\n");
		pw.append("aaa:ashjdbasdhbhjbad\n");
		pw.append("aaa:ashjdbasdhbhjbad\n");
		pw.append("aaa:ashjdbasdhbhjbad\n");

		pw.close();

		String pwd = "adminadmin";

//		byte[] params = encryptFile(aux, pwd);
//
//		String newFileName = aux.getPath().split("\\.")[0] + ".cif";
//
//		String content = decrypt(new File(newFileName), params, "adminadmin");

		//System.out.println(content);
		
//		new IntegrityVerifier().updateFile(usersFile);
//		boolean truee = new IntegrityVerifier().verifyFile(aux);
//		
////		pw = new PrintWriter(aux);
////		pw.append("alterado\n");
////		pw.close();
//		
//		boolean falsee = new IntegrityVerifier().verifyFile(aux);
//		
//		System.out.println("true="+truee+" |  false="+falsee);
		
		
		
		
		
	}

	private static String decrypt(File cif, byte[] params, String pwd) throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, InvalidKeySpecException {

		FileInputStream fs = new FileInputStream(cif);
		byte[] enc = fs.readAllBytes();

		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea,
				(byte) 0xf2 };
		PBEKeySpec keySpec = new PBEKeySpec(pwd.toCharArray(), salt, 20);
		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		SecretKey key = kf.generateSecret(keySpec);

		AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
		p.init(params);
		Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		d.init(Cipher.DECRYPT_MODE, key, p);
		byte[] dec = d.doFinal(enc);
		fs.close();
		return new String(dec);
	}

	private static byte[] encryptFile(File aux, String pwd) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IOException, InvalidKeySpecException {
		// Generate the key based on the password
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
		
		
		return params;
	}

}
