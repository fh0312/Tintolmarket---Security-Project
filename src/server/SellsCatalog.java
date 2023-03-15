package server;

import java.util.ArrayList;

public class SellsCatalog {
	
	public ArrayList<Sell> sells;

	public SellsCatalog() {
		this.sells = new ArrayList<Sell>();
	}

	public ArrayList<Sell> getSellsByWine(Tintol wine) {
		ArrayList<Sell> sellsWine= new ArrayList<Sell>();
		for (Sell sell : this.sells) {
			if(sell.getWine()==wine)
				sellsWine.add(sell);
		}
		return sellsWine;
	}
	
	public ArrayList<Sell> getSellsByClient(Client cli) {
		ArrayList<Sell> sellsCli= new ArrayList<Sell>();
		for (Sell sell : this.sells) {
			if(sell.getClient()==cli)
				sellsCli.add(sell);
		}
		return sellsCli;
	}
	
	public void add(Sell s) {
		this.sells.add(s);
	}
	

}
