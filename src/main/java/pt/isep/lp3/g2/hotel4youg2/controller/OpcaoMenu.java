package pt.isep.lp3.g2.hotel4youg2.controller;

public class OpcaoMenu {
    private final String texto;
    private final String ficheiro;
    private final boolean formulario;

    public OpcaoMenu(String texto, String ficheiro, boolean formulario) {
        this.texto = texto;
        this.ficheiro = ficheiro;
        this.formulario = formulario;
    }

    public String getTexto() {
        return texto;
    }

    public String getFicheiro(){
        return ficheiro;
    }

    public boolean isFormulario() {
        return formulario;
    }
}
