package ua.nure.order.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.client.Cart;
import ua.nure.order.client.ReqParam;
import ua.nure.order.entity.book.Book;

import java.io.IOException;
import java.io.Serial;

/**
 * <p>
 * Updates count of one product in the {@link Cart}. Cart must be in session as
 * attribute 'cart'. Perform two actions {@code update} and {@code remove}.
 * Value of action is {@code id} of product.
 * <p>
 * If action is {@code update} then sets {@code count} of product in the
 * {@code cart} from request parameter. If action is {@code remove} then remove
 * product from the {@code cart}.
 * 
 * @author engsyst
 */
@WebServlet("/updatecart")
public class UpdateCart extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(UpdateCart.class);

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.trace("doPost start");
		HttpSession session = request.getSession(false);
		@SuppressWarnings("unchecked")
		Cart<Book> cart = (Cart<Book>) session.getAttribute("cart");
		log.debug("Get cart from session --> {}", cart);
		if (cart == null) {
			cart = new Cart<>();
			session.setAttribute("cart", cart);
			response.sendRedirect("listcart.jsp");
			log.debug("New Cart created. Redirect to listcart.jsp");
		}
		int id;
		String uId = request.getParameter("update");
		log.debug("Update id --> {}", uId);
		String rId = request.getParameter("remove");
		log.debug("Remove id --> {}", uId);
		int count;
		if (uId != null && !uId.trim().equals("")) {
			try {
				id = Integer.parseInt(uId);
				String sCount = request.getParameter("count");
				log.debug("Count of books --> {}", sCount);
				count = Integer.parseInt(sCount);
				if (count <= 0) {
					session.setAttribute("error", "Не допустимое количество товара");
				} else {
					cart.put(cart.getItem(id), count);
					session.setAttribute("info", "Корзина обновлена");
				}
				log.debug("Cart updated --> {}", cart);
			} catch (Exception e) {
				log.error("Unknown product. Operation update. Id: {}", uId);
				throw new ServletException("Unknown product. Operation update. Id: " + uId);
			}
		}
		if (rId != null && !rId.trim().equals("")) {
			try {
				id = Integer.parseInt(rId);
				cart.remove(cart.getItem(id));
				session.setAttribute("info", "Книга удалена из корзины");
				log.debug("Cart updated. Removed book --> {}", rId);
			} catch (Exception e) {
				log.error("Unknown product. Operation remove. Id: {}", uId);
				throw new ServletException("Unknown product. Operation remove. Id: " + rId);
			}
		}
		session.setAttribute("cart", cart);
		log.debug("Cart to session --> {}", cart);
		ReqParam params = new ReqParam();
		params.setParams(request.getParameterMap());
		params.removeParam("update").removeParam("remove").removeParam("count");
		log.debug("Params --> {}", params);
		response.sendRedirect("listcart.jsp?" + params);
		log.debug("Redirect to listcart.jsp");
		log.trace("doPost finish");
	}

}
