package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConexaoBDTest {

    @Test
    void ligacaoAbreEFecha() throws Exception {
        try(Connection conn = ConexaoBD.abrir()) {
            assertTrue(conn.isValid(2));
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
    }
}
