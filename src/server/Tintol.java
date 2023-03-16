package server;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Tintol {
	
	private String name;
	private File data ; 
	private File img ;
	private Double rating;
	private int n_avals;
	
	private static final String SERVERPATH = "server_files//";
	private static final String WINESPATH = SERVERPATH +"wines//";
	
	
	
	public Tintol(String name, File img) {
		this.name = name;
		this.data = new File(WINESPATH+name+".txt");
		try {
			this.data.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.img = img;
		this.rating=0.0;
		this.n_avals=0;
		writeStats();
	}
	

	public Tintol(String tintolName, File img2, File wineData) {
		this.name = tintolName;
		this.data = wineData;
		this.img = img2;
		Scanner dataSc;
		try {
			dataSc = new Scanner(wineData);
			this.rating=Double.parseDouble(dataSc.nextLine().split("=")[1]);
			this.n_avals=Integer.parseInt(dataSc.nextLine().split("=")[1]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void writeStats() {
		try {
			synchronized(this.data){
				FileWriter fw = new FileWriter(this.data);
				StringBuffer sb = new StringBuffer();
				sb.append("rating="+this.rating);
				sb.append("\n");
				sb.append("numberOfEvaluations="+this.n_avals);
				sb.append("\n");
				fw.append(sb.toString());
				fw.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getName() {
		return name;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("tintol_name: "+this.name+"\n");
		sb.append("\timg_file: "+this.img+"\n");
		sb.append("\trating: "+this.rating+"\n");
		return sb.toString();
	}

	public void classify(Double stars) {
		this.rating = ((this.rating*this.n_avals)+stars)/(this.n_avals+1);
		this.n_avals ++;
		writeStats();
	}

	public Double getRating() {
		return this.rating;
	}
	
}







