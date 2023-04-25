package server;

public abstract class Transacao{

    protected String nomeVinho;
    protected double valorPerUnit;

    public String getNomeVinho(){
        return this.nomeVinho;
    }
    
    public double getValorPerUnit(){
        return this.valorPerUnit;
    }
    
   
    
}