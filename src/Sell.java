
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
	 * @return the user
	 */
	public Client getUser() {
		return user;
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
	
	

}
