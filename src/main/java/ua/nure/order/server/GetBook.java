package ua.nure.order.server;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.entity.book.Book;
import ua.nure.order.server.dao.BookDAO;
import ua.nure.order.server.dao.DAOException;

import java.io.IOException;
import java.util.Map;

/**
 * Get book from database by id and forward to edit book form
 *
 * @author engsyst
 *
 */
@WebServlet("/book/get")
public class GetBook extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(GetBook.class);
	private transient BookDAO bookService = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetBook() {
        super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
    @Override
	public void init() {
    	log.trace("init start");
    	bookService = (BookDAO) getServletContext().getAttribute("BookDao");
    	log.debug("Get BookDao from context --> {}", bookService);
    	log.trace("init finish");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.trace("Start");
		String sId = request.getParameter("id");
		
		Book book = null;
		Map<Integer, String> categories = null;
		try {
			categories = bookService.getCategories();
			log.debug("Found categories --> {}", categories);
			request.setAttribute("categories", categories);
			if (sId == null || sId.isEmpty()) {
				book = new Book();
				log.debug("Create new Book -- > ");
			} else {
				book = bookService.getBook(Integer.parseInt(sId));
				log.debug("Found Book --> {}", book);
			}
			request.setAttribute("book", book);
		} catch (DAOException e) {
			log.error("DB access error --> ", e.getCause());
			request.setAttribute("error", e.getMessage());
		} catch (NumberFormatException e) {
			log.error("Unknown id --> {}", sId);
			throw new ServletException("Unknown id -->" + sId);
		}
		request.getRequestDispatcher("bookform.jsp").forward(request, response);
		log.debug("Forward to -- > bookform.jsp");
		log.trace("Finish");
	}
}
