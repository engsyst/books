package ua.nure.order.server.dao;

import java.lang.reflect.Constructor;

/**
 * <p>Represent factory methods for creating Domain DAO</p>
 * <p>see {@link <a href="http://www.oracle.com/technetwork/java/dataaccessobject-138824.html">DAO pattern</a>}.</p>
 * Example:
 * {@code UserDAO = DAOFactory.getDAOFactory().getUserDAO();}
 * @author engsyst
 *
 */
public abstract class DAOFactory {
	
	// There will be a method for each DAO that can be
	// created. The concrete factories will have to
	// implement these methods.
	public abstract UserDAO getUserDAO();

	public abstract BookDAO getBookDAO();

	public abstract OrderDAO getOrderDAO();

	public static DAOFactory newInstance(String className, String resourceName)
			throws ReflectiveOperationException {
		Class<?> aClass = Class.forName(className);
		Constructor<?> constructor = aClass.getDeclaredConstructor(String.class);
		return (DAOFactory) constructor.newInstance(resourceName);
	}
}
