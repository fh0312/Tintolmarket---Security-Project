package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
		File user_data = s.getClient().getDataFile();
		try {
			FileWriter fw = new FileWriter(user_data,true);
			synchronized (fw) {
				fw.append(s.toString());
				fw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.sells.add(s);
	}

	public void load(Sell sell) {
		this.sells.add(sell);
	}
	

}
