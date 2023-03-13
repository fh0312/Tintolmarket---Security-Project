import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
	
	private String user;
	private String pswd;
	private Double balance;
	private File data ;
	private String PATH = "users//";
	
	private ArrayList<Sell> sells;
	
	
	//new client
	public Client(String u,String p) {
		this.user = u;
		this.pswd = p;
		this.balance = 200.0;
		
		this.data = new File ("users//"+this.user+".txt");
		try {
			data.createNewFile();
		} catch (IOException e) {
			System.out.println("ERROR while creating "+data.getName()+" file");
			e.printStackTrace();
		}
		
		this.sells = new ArrayList<Sell>();
	}
	
	/**
	 * Loads and creates a client by his data file and password.
	 *(This constructor only creates clients that were created somewhere in time by this server)
	 * 
	 * @param data - file created to store the client's data if the server goes down
	 * @param password - client´s password 
	 */
	public Client(File data) {
		System.out.println(data.getName());
		this.user = data.getName().split("\\.")[0];
		System.out.println("User: "+user+" created!");
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
		return p == this.pswd;
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
