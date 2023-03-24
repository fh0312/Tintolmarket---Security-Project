package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author 
 * Alexandre Müller - FC56343
 * Diogo Ramos - FC56308
 * Francisco Henriques - FC56348 
 *
 */

public class Sell {
	
	/**
	 * seller
	 */
	private Client user;
	
	/**
	 * wine to be sold
	 */
	private Tintol wine;
	
	/**
	 * sale stock
	 */
	private int quant;
	
	/**
	 * price per unit
	 */
	private Double price;
	
	/**
	 * Constructor of class Sell
	 * @param user  - seller
	 * @param wine  - wine to be sold
	 * @param quant - sale stock
	 * @param price - price per unit
	 */
	protected Sell(Client user, Tintol wine, int quant, Double price) {
		this.user = user;
		this.wine = wine;
		this.quant = quant;
		this.price = price;
	}

	/**
	 * @return the wine
	 */
	protected Tintol getWine() {
		return wine;
	}

	/**
	 * @return the quant
	 */
	protected int getQuant() {
		return quant;
	}

	/**
	 * @return the price
	 */
	protected Double getPrice() {
		return price;
	}

	/**
	 * @return the client
	 */
	protected Client getClient() {
		return this.user;
	}

	@Override
	public String toString() {
		return wine.getName() + "=" + quant + ";" + price + "\n";
	}


	/**
	 * Updates the sale stock
	 * @param i
	 */
	protected void setQuant(int i) {
		this.quant=i;
	}


	/**
	 * Writes the sell to the client data file.
	 */
	protected void writeStats() {
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
