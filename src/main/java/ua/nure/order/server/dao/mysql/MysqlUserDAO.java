package ua.nure.order.server.dao.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.entity.user.User;
import ua.nure.order.server.dao.DAOException;
import ua.nure.order.server.dao.UpdateException;
import ua.nure.order.server.dao.UserDAO;

import java.sql.*;

/**
 * <p>
 * Concrete DAO for User domain. Singleton pattern. Can not be created from
 * application context.
 * </p>
 * <p>
 * Each interface method represent as 2 methods. 1-st public method - manage
 * connection, 2-nd use existed connection to obtain data.
 * </p>
 * <p>
 * Pattern for read operation can be represented as flowing
 * </p>
 *
 * <pre>
 * {@code
 *  TransferObject obj;
 *  try (Connection con = MysqlDAOFactory.getConnection()) {
 *      obj = getTransferObject(con, dataToFind);
 *  } catch (SQLException e) {
 *      log.error("Log error if Exception occurs", e);
 *      throw new DAOException("Can not get TransferObject. " + e.getMessage(), e);
 *  }
 *  return obj;
 * }
 * </pre>
 * <p>
 * Pattern for <b>insert, update, delete</b> operations can be represented as flowing
 * </p>
 *
 * <pre>
 * {@code
 *  Connection con = null;
 *  try {
 *      con = MysqlDAOFactory.getConnection();
 *      obj = updateTransferObject(con, dataToUpdate);
 *
 *      // Commit changes if no errors occurs
 *      con.commit();
 *  } catch (SQLException e) {
 *      log.error("Log error if Exception occurs", e);
 *
 *      // Rollback changes if Exception occurs
 *      MysqlDAOFactory.rollback(con);
 *      throw new DAOException("Can not update TransferObject. " + e.getMessage(), e);
 *  } finally {
 *      MysqlDAOFactory.close(con);
 *  }
 *  return obj;
 * }
 * </pre>
 *
 * @author engsyst
 */
class MysqlUserDAO implements UserDAO {
    private static final Logger log = LoggerFactory.getLogger(MysqlUserDAO.class);
    public static final String LOG_MSG_QUERY = "Query --> {}";
    public static final String LOG_MSG_START = "Start";
    public static final String LOG_MSG_FINISH = "Finish";
    public static final String LOG_MSG_RESULT = "Result --> {}";
    final ConnectionManager cm;
    public MysqlUserDAO(ConnectionManager cm) {
        this.cm = cm;
    }

    /**
     * Get {@link User} from database by login
     */
    @Override
    public User getUser(String login) throws DAOException {
        User user;
        try (Connection con = cm.getConnection()) {
            user = getUser(con, login);
        } catch (SQLException e) {
            throw new DAOException("Can to get user: '" + login + "'. " + e.getMessage(), e);
        }
        return user;
    }

    /**
     * Get {@link User} from database by login.
     *
     * @param con   Opened connection
     * @param login User login
     * @return {@link User}
     * @throws SQLException if an error occurs
     * @see ConnectionManager#getConnection()
     */
    User getUser(Connection con, String login) throws SQLException {
        User user = null;
        try (PreparedStatement st = con.prepareStatement(Querys.SQL_GET_USER)) {
            st.setString(1, login);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                user = unmapUser(rs);
            }
        }
        return user;
    }

    /**
     * Create User object from ResultSet
     *
     * @param rs ResultSet
     * @return User
     * @throws SQLException if an error occurs
     */
    User unmapUser(ResultSet rs) throws SQLException {
        User user = new User(rs.getInt("id"), rs.getString("login"), rs.getString("password"), rs.getString("role"));
        user.setAddress(rs.getString("address"));
        user.setAvatar(rs.getString("avatar"));
        user.setDescription(rs.getString("description"));
        user.setEmail(rs.getString("e-mail"));
        user.setPhone(rs.getString("phone"));
        user.setName(rs.getString("name"));
        return user;
    }

    @Override
    public void updateUser(User user) throws DAOException {
        Connection con = null;
        try {
            con = cm.getConnection(false);
            updateUser(con, user);
            con.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(con);
            throw new UpdateException("Can to update user: " + user, e);
        } finally {
            ConnectionManager.close(con);
        }
    }


    public int updateUser(Connection con, User user) throws SQLException {
        log.trace(LOG_MSG_START);
        int id = 0;
        /* `login`,`password`,`role`,`e-mail`,`phone`,
         * `name`,`address`,`avatar`,`description` WHERE `id` = ?
         */
        try (PreparedStatement st = con.prepareStatement(Querys.SQL_UPDATE_USER)) {
            int k = 0;
            st.setString(++k, user.getLogin());
            st.setString(++k, user.getPass());
            st.setString(++k, user.getRole().toString());
            st.setString(++k, user.getEmail());
            st.setString(++k, user.getPhone());
            st.setString(++k, user.getName());
            st.setString(++k, user.getAddress());
            st.setString(++k, user.getAvatar());
            st.setString(++k, user.getDescription());
            st.setInt(++k, user.getId());
            log.debug(LOG_MSG_QUERY, st);
            st.executeUpdate();
        }
        log.trace(LOG_MSG_FINISH);
        return id;
    }

    @Override
    public int addUser(User user) throws DAOException {
        Connection con = null;
        int id;
        try {
            con = cm.getConnection(false);
            id = addUser(con, user);
            con.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(con);
            throw new DAOException("Can to add user: " + user, e);
        } finally {
            ConnectionManager.close(con);
        }
        return id;

    }

    public int addUser(Connection con, User user) throws SQLException {
        log.trace(LOG_MSG_START);
        int id;
        try (PreparedStatement st = con.prepareStatement(Querys.SQL_ADD_USER, Statement.RETURN_GENERATED_KEYS)) {
            // (`login`,`password`,`role`,`e-mail`,`phone`,`name`,`address`,`avatar`,`description`)
            int k = 0;
            st.setString(++k, user.getLogin());
            st.setString(++k, user.getPass());
            st.setString(++k, user.getRole().toString());
            st.setString(++k, user.getEmail());
            st.setString(++k, user.getPhone());
            st.setString(++k, user.getName());
            st.setString(++k, user.getAddress());
            st.setString(++k, user.getAvatar());
            st.setString(++k, user.getDescription());
            log.debug(LOG_MSG_QUERY, st);
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                log.error("Can not add user");
                throw new SQLException("Can not add user");
            }
        }
        log.debug(LOG_MSG_RESULT, id);
        log.trace(LOG_MSG_FINISH);
        return id;
    }

}
