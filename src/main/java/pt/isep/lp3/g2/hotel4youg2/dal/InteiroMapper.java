package pt.isep.lp3.g2.hotel4youg2.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteiroMapper implements RowMapper<Integer> {

    @Override
    public Integer map(ResultSet rs) throws SQLException {
        return rs.getInt(1);
    }
}
