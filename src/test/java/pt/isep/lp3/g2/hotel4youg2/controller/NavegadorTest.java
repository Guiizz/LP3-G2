package pt.isep.lp3.g2.hotel4youg2.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NavegadorTest {
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
        String[] ecras = {"inicial-view.fxml", "quartos-menu-view.fxml", "tarifas-view.fxml",
                "utilizadores-view.fxml", "criar-conta-view.fxml", "servicos-view.fxml", "reservas-view.fxml",
                "entregas-view.fxml", "registar-cliente-view.fxml", "pesquisar-cliente-view.fxml",
                "sugestoes-view.fxml", "consultar-sugestoes-view.fxml", "criar-fornecedor-view.fxml",
                "fornecedor-view.fxml", "decidir-entregas-view.fxml"};
        for (String ecra : ecras) {
            assertNotNull(Navegador.localizar(ecra), ecra);
        }
    }
}
