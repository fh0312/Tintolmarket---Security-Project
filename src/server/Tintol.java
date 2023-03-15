package server;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Tintol {
	
	private File data ; 
	private File img ;
	private Double rating;
	private int eval;
	

	private String name;
	
	public Tintol(String name, File img) {
		this.name = name;
		this.data = new File(name+".txt");
		try {
			this.data.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.img = img;
		this.rating=0.0;
		this.eval=0;
	}

	public String getName() {
		return name;
	}
	
	
	

}
