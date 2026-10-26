package pt.isep.lp3.g2.hotel4youg2.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Converte a linha atual de um {@link ResultSet} num objeto.
 * Usado por {@link ConexaoBD#select(String, RowMapper, Object...)}.
 *
 * @param <T> o tipo de objeto criado
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * @param rs o resultado, fica posicionado na linha a converter
     * @return o objeto criado a partir da linha
     * @throws SQLException se não for possível ler uma coluna
     */
    T map(ResultSet rs) throws SQLException;
}
