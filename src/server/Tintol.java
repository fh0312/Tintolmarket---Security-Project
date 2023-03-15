package server;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Tintol {
	
	private String name;
	private File data ; 
	private File img ;
	private Double rating;
	private int eval;
	
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
		this.eval=0;
		writeStats();
	}
	
	public Tintol(String name, File img,Double rating,int eval) { //load tintol
		this.name = name;
		this.data = new File(WINESPATH+name+".txt");
		this.img = img;
		this.rating=rating;
		this.eval=eval;
	}

	private void writeStats() {
		try {
			FileWriter fw = new FileWriter(this.data);
			StringBuffer sb = new StringBuffer();
			sb.append("rating="+this.rating);
			sb.append("\n");
			sb.append("numberOfEvaluations="+this.eval);
			sb.append("\n");
			fw.append(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getName() {
		return name;
	}

	public void setRating(double d) {
		// TODO Auto-generated method stub
		
	}

	public void setEval(int i) {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
