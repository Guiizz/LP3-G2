package pt.isep.lp3.g2.hotel4youg2.controller;

/**
 * Opção do submenu de uma área (US44): o texto do botão, o ecrã que abre e se abre dentro
 * da área ou em pop-up.
 */
public class OpcaoMenu {
    private final String texto;
    private final String ficheiro;
    private final boolean formulario;

    /**
     * Cria uma opção do submenu.
     *
     * @param texto      o texto do botão
     * @param ficheiro   o nome do ficheiro FXML do ecrã
     * @param formulario {@code true} se o ecrã abre em pop-up; {@code false} se abre dentro da área
     */
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
