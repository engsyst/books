package ua.nure.order.server;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.nure.order.entity.order.Order;
import ua.nure.order.server.dao.DAOException;
import ua.nure.order.server.dao.OrderDAO;

import java.io.IOException;

/**
 * Get order from database by id and forward to view order page
 * @param id in the request
 * 
 * @author engsyst
 *
 */
@WebServlet("/order/orderdetal")
public class OrderDetal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private transient OrderDAO orderService = null;

	@Override
	public void init() {
		ServletContext ctx = getServletContext();
		orderService = (OrderDAO) ctx.getAttribute("OrderDao");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("Cache-control", "no-cache");
		try {
			Order order = orderService.getOrderDetail(Integer.parseInt(request.getParameter("id")));
			request.setAttribute("order", order);
		} catch (NumberFormatException | DAOException e) {
			request.setAttribute("error", "Order not found. Id: " + request.getParameter("id"));
		}
		request.getRequestDispatcher("orderdetal.jsp").forward(request, response);
	}
}
