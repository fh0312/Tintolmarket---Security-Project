package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Sell {
	
	private Client user; 
	private Tintol wine;
	private int quant;
	private Double price;
	
	public Sell(Client user, Tintol wine, int quant, Double price) {
		this.user = user;
		this.wine = wine;
		this.quant = quant;
		this.price = price;
	}



	/**
	 * @return the wine
	 */
	public Tintol getWine() {
		return wine;
	}

	/**
	 * @return the quant
	 */
	public int getQuant() {
		return quant;
	}

	/**
	 * @return the price
	 */
	public Double getPrice() {
		return price;
	}

	/**
	 * @return the client
	 */
	public Client getClient() {
		return this.user;
	}

	@Override
	public String toString() {
		return wine.getName() + "=" + quant + ";" + price + "\n";
	}



	public void setQuant(int i) {
		this.quant=i;
	}



	public void writeStats() {
		File user_data = getClient().getDataFile();
		try {
			FileWriter fw = new FileWriter(user_data,true);
			synchronized (fw) {
				fw.append(toString());
				fw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}
