package pt.isep.lp3.g2.hotel4youg2.dal;

import java.sql.ResultSet;
import java.sql.SQLException;



@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
