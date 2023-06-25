package server;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 
 * Alexandre Müller - FC56343
 * Diogo Ramos - FC56308
 * Francisco Henriques - FC56348 
 *
 */

public class SellsCatalog {
	
	public ConcurrentHashMap<String,Sell> sells;

	public SellsCatalog() {
		this.sells = new ConcurrentHashMap<String,Sell>();
	}

	public ArrayList<Sell> getSellsByWine(Tintol wine) {
		ArrayList<Sell> sellsWine= new ArrayList<Sell>();
		for (Sell sell : this.sells.values()) {
			if(sell.getWine()==wine)
				sellsWine.add(sell);
		}
		return sellsWine;
	}
	
	public ArrayList<Sell> getSellsByClient(Client cli) {
		ArrayList<Sell> sellsCli= new ArrayList<Sell>();
		for (Sell sell : this.sells.values()) {
			if(sell.getClient()==cli)
				sellsCli.add(sell);
		}
		return sellsCli;
	}
	
	public void add(Sell s) {

		this.sells.put((s.getClient().getUser()+":"+s.getWine().getName()),s);
		s.writeStats();
	}

	public void load(Sell sell) {
		this.sells.put((sell.getClient().getUser()+":"+sell.getWine().getName()),sell);
	}
	
	public Sell getSell(Client cli,Tintol wine) {
		Sell sellGet= null;
		for (String key: this.sells.keySet()) {
			if(key.equals(cli.getUser()+":"+wine.getName())) {
				sellGet = this.sells.get(key);
				break;
			}
		}
		return sellGet;
	}
	
	public boolean buy(Tintol tintol, Client seller, int quant, Client buyer) {
		Sell sell = getSell(seller,tintol);
			Double amount = quant*(sell.getPrice());
			if(buyer.getBalance()>= amount) {
				buyer.setBalance(buyer.getBalance()-amount);
				buyer.loadStats();
				seller.setBalance(seller.getBalance()+amount);
				seller.loadStats();
				sell.setQuant(sell.getQuant()-quant);
				sell.writeStats();
				if(sell.getQuant()==0) {
					this.sells.remove(getKey(sell));
					seller.writeStats();
				}
				return true;
			}
			
			return false;
	}

	private String getKey(Sell s) {
		return s.getClient().getUser()+":"+s.getWine().getName();
	}
	

}
