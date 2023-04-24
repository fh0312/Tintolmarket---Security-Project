package server;

public class TransacaoBuy extends Transacao{

    private int unidadesVendidas;
    private String idNovoDono;

    public TransacaoBuy(String nomeVinho, double valorPerUnit, int unidadesVendidas, String idNovoDono){
        this.nomeVinho = nomeVinho;
        this.valorPerUnit = valorPerUnit;
        this.unidadesVendidas = unidadesVendidas;
        this.idNovoDono = idNovoDono;
    }

    public String getNomeVinho(){
        return super.getNomeVinho();
    }

    public double getValorPerUnit(){
        return super.getValorPerUnit();
    }

    public int getUnidadesVendidas(){
        return this.unidadesVendidas;
    }

    public String getIdNovoDono(){
        return this.idNovoDono;
    }
}