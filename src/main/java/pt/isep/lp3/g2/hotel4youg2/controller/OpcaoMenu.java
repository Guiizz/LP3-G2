package pt.isep.lp3.g2.hotel4youg2.controller;

/**
 * Opção do submenu de uma área (US44): o texto do botão, o ecrã que abre e se abre dentro
 * da área ou em pop-up.
 */
public class OpcaoMenu {
    private final String texto;
    private final String ficheiro;
    private final boolean formulario;
    private final String icone;

    public OpcaoMenu(String texto, String ficheiro, boolean formulario) {
        this(texto, ficheiro, formulario, null);
    }

    public OpcaoMenu(String texto, String ficheiro, boolean formulario, String icone) {
        this.texto = texto;
        this.ficheiro = ficheiro;
        this.formulario = formulario;
        this.icone = icone;
    }

    public String getIcone() {
        return icone;
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
