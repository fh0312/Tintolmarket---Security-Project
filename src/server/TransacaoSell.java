package server;

public class TransacaoSell extends Transacao{

    private int unidadesCriadas;
    private String idDono;

    public TransacaoSell(String nomeVinho, double valorPerUnit, int unidadesCriadas, String idDono){
        this.nomeVinho = nomeVinho;
        this.valorPerUnit = valorPerUnit;
        this.unidadesCriadas = unidadesCriadas;
        this.idDono = idDono;
    }

    public String getNomeVinho(){
        return super.getNomeVinho();
    }

    public double getValorPerUnit(){
        return super.getValorPerUnit();
    }

    public int getUnidadesCriadas(){
        return this.unidadesCriadas;
    }

    public String getIdDono(){
        return this.idDono;
    }

	@Override
	public String toString() {
		return "TransacaoSell->unidadesCriadas=" + unidadesCriadas + ",idDono=" + idDono + ",nomeVinho=" + nomeVinho
				+ ",valorPerUnit=" + valorPerUnit;
	}
}