package pt.isep.lp3.g2.hotel4youg2.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;

public class NavegadorTest {

    private BorderPane area;
    private VBox lista;
    private VBox detalhe;

    @BeforeEach
    void setUp() {
        Navegador.iniciar(new BorderPane());
        area = new BorderPane();
        lista = new VBox();
        detalhe = new VBox();
        area.setCenter(lista);
    }

    @Test
    void mostrarDetalheTrocaOCentroDaArea() {
        System.out.println("mostrarDetalhe (aparece dentro da área)");
        VBox expResult = detalhe;
        Navegador.mostrarDetalhe(lista, detalhe);
        Object result = area.getCenter();
        assertEquals(expResult, result);
    }

    @Test
    void voltarMostraOMesmoEcraDeAntes() {
        System.out.println("voltar (o mesmo ecrã, não um novo)");
        VBox expResult = lista;
        Navegador.mostrarDetalhe(lista, detalhe);
        Navegador.voltar();
        Object result = area.getCenter();
        assertSame(expResult, result);
    }

    @Test
    void voltarSemDetalheNaoFazNada() {
        System.out.println("voltar (sem detalhe aberto)");
        VBox expResult = lista;
        Navegador.voltar();
        Object result = area.getCenter();
        assertSame(expResult, result);
    }

    @Test
    void mostrarDetalheForaDeUmBorderPane() {
        System.out.println("mostrarDetalhe (ecrã fora de um BorderPane)");
        String expResult = "O ecrã atual não está dentro de um BorderPane";
        try {
            Navegador.mostrarDetalhe(new VBox(), detalhe);
            fail("Devia ter recusado um ecrã fora de um BorderPane");
        } catch (IllegalArgumentException e) {
            assertEquals(expResult, e.getMessage());
        }
    }

    @Test
    void ecraDeSaidaDeMateriaisExiste() {
        assertNotNull(Navegador.localizar("saida-stock-view.fxml"));
    }

    @Test
    void localizaUmEcraExiste() {
        assertNotNull(Navegador.localizar("login-view.fxml"));
    }

    @Test
    void ecraQueNaoExisteDaUmaMensagemClara() {
        try {
            Navegador.localizar("nao-existe-view.fxml");
            fail("Devia ter recusado um ecrã que não existe");
        }catch (IllegalArgumentException e) {
            assertEquals("Não existe o ecrã nao-existe-view.fxml", e.getMessage());
        }
    }

    @Test
    void todosOsEcrasDoMenuExistem() {
        String[] ecras = {"inicial-view.fxml", "area-view.fxml", "tarifas-view.fxml",
                "utilizadores-view.fxml", "criar-conta-view.fxml", "servicos-view.fxml",
                "entregas-view.fxml", "registar-cliente-view.fxml", "pesquisar-cliente-view.fxml",
                "sugestoes-view.fxml", "consultar-sugestoes-view.fxml", "gerir-reservas-view.fxml",
                "criar-reserva-view.fxml", "disponibilidade-view.fxml", "consultar-reservas-view.fxml",
                "quartos-view.fxml", "gestao-quartos-view.fxml", "criar-fornecedor-view.fxml",
                "fornecedor-view.fxml", "decidir-entregas-view.fxml", "horario-reserva-view.fxml",
                "acesso-cartao-view.fxml", "sugestao-cartao-view.fxml", "servicos-reserva-view.fxml",
                "consultar-acessos-view.fxml", "saida-stock-view.fxml", "detalhes-reserva-view.fxml"};
        for (String ecra : ecras) {
            assertNotNull(Navegador.localizar(ecra), ecra);
        }
    }
}
