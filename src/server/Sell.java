package server;

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
	
	

}
