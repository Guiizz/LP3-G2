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
        String[] ecras = {"inicial-view.fxml", "area-view.fxml", "tarifas-view.fxml",
                "utilizadores-view.fxml", "criar-conta-view.fxml", "servicos-view.fxml",
                "entregas-view.fxml", "registar-cliente-view.fxml", "pesquisar-cliente-view.fxml",
                "sugestoes-view.fxml", "consultar-sugestoes-view.fxml", "gerir-reservas-view.fxml",
                "criar-reserva-view.fxml", "disponibilidade-view.fxml", "consultar-reservas-view.fxml",
                "quartos-view.fxml", "gestao-quartos-view.fxml", "criar-fornecedor-view.fxml",
                "fornecedor-view.fxml", "decidir-entregas-view.fxml", "horario-reserva-view.fxml",
                "acesso-cartao-view.fxml", "sugestao-cartao-view.fxml", "servicos-reserva-view.fxml",
                "consultar-acessos-view.fxml", "saida-stock-view.fxml"};
        for (String ecra : ecras) {
            assertNotNull(Navegador.localizar(ecra), ecra);
        }
    }
}
