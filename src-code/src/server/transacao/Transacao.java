package server.transacao;

public abstract class Transacao{

    protected String nomeVinho;
    protected double valorPerUnit;
    protected byte[] clientSign;

    public String getNomeVinho(){
        return this.nomeVinho;
    }
    
    public double getValorPerUnit(){
        return this.valorPerUnit;
    }
    
   
    
}