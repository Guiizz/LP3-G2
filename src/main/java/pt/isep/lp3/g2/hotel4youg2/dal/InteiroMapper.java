package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Lê a primeira coluna de cada linha como um número inteiro, por exemplo um {@code COUNT(*)}
 * ou o id devolvido por um {@code OUTPUT INSERTED.id}.
 */
public class InteiroMapper implements RowMapper<Integer> {

    @Override
    public Integer map(ResultSet rs) throws SQLException {
        return rs.getInt(1);
    }
}
