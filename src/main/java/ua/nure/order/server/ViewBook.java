package ua.nure.order.server;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.entity.book.Book;
import ua.nure.order.server.dao.BookDAO;

import java.io.IOException;
import java.io.Serial;

/**
 * Get book from database by id and forward to view book page
 * 
 * @author engsyst
 *
 */
public class ViewBook extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(ViewBook.class);
	private transient BookDAO bookService = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ViewBook() {
        super();
    }

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
		log.trace("doPost start");
		RequestDispatcher rd = request.getRequestDispatcher("/error.jsp");
		String id = request.getParameter("id");
		Book book = null;
		if (id != null)
			try {
				book = bookService.getBook(Integer.parseInt(id));
				log.debug("Get Book from dao --> {}", book);
			} catch (Exception e) {
				log.error("Book with id = " + id + " not found", e.getCause());
				rd.forward(request, response);
			}
		rd = request.getRequestDispatcher("viewbook.jsp");
		request.setAttribute("book", book);
		rd.forward(request, response);
		log.debug("Forvard to viewbook.jsp");
		log.trace("doPost finish");
	}
	
}
