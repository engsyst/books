package ua.nure.order.server.dao.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);
    private final DataSource ds;

    public ConnectionManager(String resourceName) {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            ds = (DataSource) envContext.lookup(resourceName); // "jdbc/mysql"
        } catch (NamingException e) {
            log.error("Can not get DataSource for context: {}", resourceName);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create MSSQL pooled connection using application context
     *
     * @return connection
     * @throws SQLException ia an error occurs
     */
    Connection getConnection() throws SQLException {
        return getConnection(true);
    }

    Connection getConnection(boolean autocommit) throws SQLException {
        log.trace("Start");
        Connection con = ds.getConnection();
        con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        con.setAutoCommit(autocommit);
        log.trace("Finish");
        return con;
    }

    public static void rollback(Connection con) {
        if (con != null) {
            try {
                log.debug("Try rollback.");
                con.rollback();
            } catch (SQLException e) {
                log.error("Can not rollback transaction.", e);
            }
        }
    }

    protected static void close(Connection con) {
        try {
            log.debug("Try close connection.");
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            log.error("Can not close connection.", e);
        }
    }
}
