package server.transacao;

public class TransacaoSell extends Transacao{

    private int unidadesCriadas;
    private String idDono;

    public TransacaoSell(String nomeVinho, double valorPerUnit, int unidadesCriadas, String idDono, byte[] bs){
        this.nomeVinho = nomeVinho;
        this.valorPerUnit = valorPerUnit;
        this.unidadesCriadas = unidadesCriadas;
        this.idDono = idDono;
        this.clientSign = bs;
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
				+ ",valorPerUnit=" + valorPerUnit + ",assinatura="+byteArrayToHexString(clientSign);
	}
	
	private String byteArrayToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}