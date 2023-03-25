package ua.nure.order.server.dao.mysql;

import ua.nure.order.server.dao.BookDAO;
import ua.nure.order.server.dao.DAOFactory;
import ua.nure.order.server.dao.OrderDAO;
import ua.nure.order.server.dao.UserDAO;
/**
 * <p>Concrete factory for MySQL database.</p>
 * <p>Contains methods for create each domain DAO and methods to manage database connection.</p>
 * @see DAOFactory
 * @author engsyst
 *
 */
public class MysqlDAOFactory extends DAOFactory {
	final ConnectionManager connectionManager;
	private UserDAO userDAO;
	private BookDAO bookDAO;
	private OrderDAO orderDAO;

	public MysqlDAOFactory(String resourceName) {
		connectionManager = new ConnectionManager(resourceName);
	}

	@Override
	public UserDAO getUserDAO() {
		if (userDAO == null) {
			userDAO = new MysqlUserDAO(connectionManager);
		}
		return userDAO;
	}

	@Override
	public BookDAO getBookDAO() {
		if (bookDAO == null) {
			bookDAO = new MysqlBookDAO(connectionManager);
		}
		return bookDAO;
	}

	@Override
	public OrderDAO getOrderDAO() {
		if (orderDAO == null) {
			orderDAO = new MysqlOrderDAO(connectionManager);
		}
		return orderDAO;
	}
}
