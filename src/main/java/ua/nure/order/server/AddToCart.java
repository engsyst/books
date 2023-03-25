package ua.nure.order.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.client.Cart;
import ua.nure.order.client.ReqParam;
import ua.nure.order.entity.book.Book;
import ua.nure.order.server.dao.BookDAO;
import ua.nure.order.server.dao.DAOException;

import java.io.IOException;
import java.io.Serial;

/**
 * Add product to the {@link Cart}. Cart must be in session as Attribute 'cart'.
 */
public class AddToCart extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(AddToCart.class);
	private transient BookDAO bookService = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AddToCart() {
		super();
	}

	@Override
	public void init() {
    	log.trace("init start");
    	bookService = (BookDAO) getServletContext().getAttribute("BookDao");
    	log.debug("Get BookDao from context --> {}", bookService);
    	log.trace("init finish");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		log.trace("doPost start");
		HttpSession session = request.getSession(false);
		log.debug("Params --> {}", request.getParameterMap());
		
		String sid = request.getParameter("tocart");
		@SuppressWarnings("unchecked")
		Cart<Book> cart = (Cart<Book>) session.getAttribute("cart");
		if (cart == null) {
			log.debug("Cart not found. Create new.");
			cart = new Cart<>();
		}
		try {
			int count = getCount(request);
			int id = Integer.parseInt(sid);
			cart.add(bookService.getBook(id), count);
			log.debug("Book added to cart --> {}", cart);
			request.setAttribute("info", "Вы купили книгу");
		} catch (NumberFormatException e) {
			log.debug("Wrong book id --> {}", sid);
			request.setAttribute("error", "Неизвестная книга");
		} catch (DAOException e) {
			log.debug("Wrong number of books");
			request.setAttribute("error", "Не достаточно книг в наличии");
		}
		session.setAttribute("cart", cart);
		// For return to the refered page
		ReqParam params = new ReqParam();
		params.setParams(request.getParameterMap());
		params.removeParam("tocart");
		params.removeParam("count");
		response.sendRedirect("list.jsp?" + params);
		log.debug("Params --> {}", params);
		log.debug("Redirect to list.jsp");
		log.trace("doPost finish");
	}

	private int getCount(HttpServletRequest request) {
		int count;
		try {
			count = Integer.parseInt(request.getParameter("count"));
			log.debug("Get parameter count --> {}", count);
			if (count < 0) count = 1;
		} catch (NumberFormatException e) {
			count = 1;
			log.debug("Set count --> {}", count);
		}
		return count;
	}

}
