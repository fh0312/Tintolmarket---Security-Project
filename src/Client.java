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
		
		this.data = new File (this.user+"_data"+".txt");
		try {
			data.createNewFile();
		} catch (IOException e) {
			System.out.println("ERROR while creating "+data.getName()+" file");
			e.printStackTrace();
		}
		
		this.sells = new ArrayList<Sell>();
	}
	
	//Client pré carregado - users.txt
	public Client(File data) {
		Scanner sc;
		this.sells = new ArrayList<Sell>();
		try {
			sc = new Scanner(data);
			this.balance = Double.parseDouble((sc.nextLine().split("="))[1]);
			sc.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean validate(String p) {
		return p == this.pswd;
	}
	
	public void sellWine(Sell s) {
		this.sells.add(s);
	}

	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public double getBalance() {
		return balance;
	}
	
	
	
	
	
}
