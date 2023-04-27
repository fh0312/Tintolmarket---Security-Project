package server.transacao;

public class TransacaoBuy extends Transacao{

    private int unidadesVendidas;
    private String idNovoDono;
	

    public TransacaoBuy(String nomeVinho, double valorPerUnit, int unidadesVendidas, String idNovoDono, byte[] bs){
        this.nomeVinho = nomeVinho;
        this.valorPerUnit = valorPerUnit;
        this.unidadesVendidas = unidadesVendidas;
        this.idNovoDono = idNovoDono;
        this.clientSign = bs;
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

	@Override
	public String toString() {
		return "TransacaoBuy->unidadesVendidas=" + unidadesVendidas + ",idNovoDono=" + idNovoDono + ",nomeVinho="
				+ nomeVinho + ",valorPerUnit=" + valorPerUnit+ ",assinatura="+ byteArrayToHexString(clientSign);
	}

	private String byteArrayToHexString(byte[] data) {
	    StringBuilder sb = new StringBuilder();
	    for (byte b : data) {
	        sb.append(String.format("%02X", b));
	    }
	    return sb.toString();
}
    
    
}