package server;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
	
	private String user;
	private String pswd;
	private Double balance;
	private File data ;
	private static final String SERVERPATH = "server_files//";
	private static final String CLIPATH = SERVERPATH +"users//";
	
	private ArrayList<Sell> sells;
	
	
	//new client
	public Client(String u,String p) {
		this.user = u;
		this.pswd = p;
		this.balance = 200.0;
		
		this.data = new File (CLIPATH+this.user+".txt");
		try {
			data.createNewFile();
		} catch (IOException e) {
			System.out.println("ERROR while creating "+data.getName()+" file");
			e.printStackTrace();
		}
		
		this.sells = new ArrayList<Sell>();
		writeStats();
	}
	
	private void writeStats() {
		try {
			FileWriter fw = new FileWriter(this.data);
			StringBuffer sb = new StringBuffer();
			sb.append("balance="+this.balance);
			sb.append("\n");
			sb.append("password="+this.pswd);
			sb.append("\n");
			fw.append(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Loads and creates a client by his data file and password.
	 *(This constructor only creates clients that were created somewhere in time by this server)
	 * 
	 * @param data - file created to store the client's data if the server goes down
	 * @param password - client´s password 
	 */
	public Client(File data) {
		this.user = data.getName().split("\\.")[0];
		this.data=data;
		this.sells = new ArrayList<Sell>();
		
		//Load balance from the data file
		Scanner sc;
		try {
			sc = new Scanner(data);
			this.balance = Double.parseDouble((sc.nextLine().split("="))[1]);
			this.pswd = sc.nextLine().split("=")[1];
			sc.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Function that returns true if the given password is the user's password
	 * 
	 * @param p - possible password attempt
	 * @return true if p is the user's password
	 */
	public boolean validate(String p) {
		return p.equals(pswd);
	}
	
	/**
	 * Add a sell to the user's sells array
	 * @param s -  sell to be added
	 */
	public void sellWine(Sell s) {
		this.sells.add(s);
	}
	
	/**
	 * Get client's username
	 * 
	 * @return Client's username
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Get client's balance
	 * 
	 * @return client's balance
	 */
	public double getBalance() {
		return balance;
	}


	
	
	
	
	
}
