package server;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author 
 * Alexandre Müller - FC56343
 * Diogo Ramos - FC56308
 * Francisco Henriques - FC56348 
 *
 */

/**
 * Class that represents a client in the tintolMarket system 
 */
public class Client {
	
	private String user;
	private String pswd;
	private Double balance;
	private File data ;
	private static final String SERVERPATH = "server_files//";
	private static final String CLIPATH = SERVERPATH +"users//";
	
	private ArrayList<Sell> sells;
	
	
	/**
	 * Client constructor used to regist a new user in the system.
//	 * To load an old client please use the constructor: Client(File data)
	 * @param u
	 * @param p
	 */
	protected Client(String u,String p) {
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
		loadStats();
	}
	
	
	/**
	 * Load the stats from the existing files in the servers/users directory
	 * Use it to update the data in memory through the files, while running the server.
	 */
	protected void loadStats() {
		try {
			synchronized (this.data) {
				
				FileWriter fw = new FileWriter(this.data);
				StringBuffer sb = new StringBuffer();
				sb.append("balance="+this.balance+"\n");
				sb.append("password="+this.pswd+"\n");
				
				Scanner sc = new Scanner(this.data);
				if(sc.hasNextLine()) {
					sc.nextLine();
					sc.nextLine();
				}
				while(sc.hasNextLine()) {
					sb.append(sc.nextLine()+"\n");
				}
				fw.append(sb.toString());
				fw.close();
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * Writes/updates a client's stats into the file data previous created and declared 
	 * in the field private File data;
	 */
	protected void writeStats() {
		try {
			synchronized (this.data) {
				
				FileWriter fw = new FileWriter(this.data);
				StringBuffer sb = new StringBuffer();
				sb.append("balance="+this.balance+"\n");
				sb.append("password="+this.pswd+"\n");
				for(Sell s: this.sells) {
					sb.append(s.toString());
				}
				fw.append(sb.toString());
				fw.close();
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	/**
	 * Loads and creates a client by his data file.
	 *(This constructor only creates clients that were created somewhere in time by this server)
	 * 
	 * @param data - file created by the system, to store the client's data if the server goes down
	 * 			   - This file requires at least 2 lines with this structure : 
	 * 							( balance=xx.x\npassword=xxx\n )
	 */
	protected Client(File data) {
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
	protected boolean validate(String p) {
		return p.equals(pswd);
	}
	
	/**
	 * Add a sell to the user's sells array
	 * @param s -  sell to be added
	 */
	protected void sellWine(Sell s) {
		this.sells.add(s);
	}
	
	/**
	 * Get client's username
	 * 
	 * @return Client's username
	 */
	protected String getUser() {
		return user;
	}

	/**
	 * Get client's balance
	 * @return client's balance
	 */
	protected double getBalance() {
		return balance;
	}
	
	/**
	 * Get client's data file
	 * @return client's balance
	 */
	protected File getDataFile() {
		return this.data;
	}
	
	/**
	 * Set's the balance to a specific value
	 * @param d - new balance to be set
	 */
	protected void setBalance(double d) {
		this.balance=d;
		loadStats();
	}


	
	
	
	
	
}
