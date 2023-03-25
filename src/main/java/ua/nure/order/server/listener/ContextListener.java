package ua.nure.order.server.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.server.dao.DAOFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Application Lifecycle Listener implementation class ContextListener
 *
 */
@WebListener
public class ContextListener implements ServletContextListener {
	private static final Logger log = LoggerFactory.getLogger(ContextListener.class);


	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    @Override
	public void contextInitialized(ServletContextEvent event)  {
    	ServletContext ctx = event.getServletContext();
		try {
			DAOFactory factory = DAOFactory.newInstance(ctx.getInitParameter("daoFactoryClassName"),
					ctx.getInitParameter("jdbcResourceName"));
			ctx.setAttribute("UserDao", factory.getUserDAO());
			log.debug(ctx.getAttribute("UserDao").toString());
			ctx.setAttribute("BookDao", factory.getBookDAO());
			log.debug(ctx.getAttribute("BookDao").toString());
			ctx.setAttribute("OrderDao",factory.getOrderDAO());
			log.debug(ctx.getAttribute("OrderDao").toString());
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
    }
}
