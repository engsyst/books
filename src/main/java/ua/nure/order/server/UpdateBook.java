package ua.nure.order.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.entity.book.Book;
import ua.nure.order.entity.book.Category;
import ua.nure.order.server.dao.BookDAO;
import ua.nure.order.server.dao.DAOException;
import ua.nure.order.server.dao.InsertException;
import ua.nure.order.server.dao.UpdateException;
import ua.nure.order.shared.Util;

import java.io.IOException;

/**
 * Get book from web-form and updates it in database
 * 
 * @author engsyst
 *
 */
@WebServlet("/book/update")
public class UpdateBook extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(UpdateBook.class);
	private transient BookDAO bookService = null;
       
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
		Book book = createBook(request);
		log.debug("Book --> {}", book);
		try {
			if (book.getId() == null) {
				book.setId(bookService.addBook(book));
			} else {
				bookService.updateBook(book);
			}
		} catch (InsertException e) {
			log.error(e.getMessage(), e.getCause());
			request.setAttribute("error", "Невозможно добавить книгу. Повторите запрос позже.");
		} catch (UpdateException e) {
			log.error(e.getMessage(), e.getCause());
			request.setAttribute("error", "Невозможно обновить книгу. Повторите запрос позже.");
		} catch (DAOException e) {
			log.error(e.getMessage(), e.getCause());
			request.setAttribute("error", "Ошибка доступа базе данных. Повторите запрос позже.");
		}
		request.setAttribute("book", book);
		response.sendRedirect("list.jsp");
	}

	private Book createBook(HttpServletRequest request) {
		Book book = new Book();
		try {
			String param = request.getParameter("id");
			log.trace("param id --> {}", param);
			book.setId(Util.getIntOrElse(param, null));
			param = request.getParameter("title");
			log.trace("param title --> {}", param);
			book.setTitle(param);
			param = request.getParameter("author");
			log.trace("param author --> {}", param);
			book.setAuthor(param);
			param = request.getParameter("isbn");
			log.trace("param isbn --> {}", param);
			book.setIsbn(param);
			param = request.getParameter("category");
			log.trace("param category --> {}", param);
			book.setCategory(Category.fromValue(param));
			param = request.getParameter("price");
			log.trace("param price --> {}", param);
			book.setPrice(Util.getDoubleOrElse(param, null));
			param = request.getParameter("count");
			log.trace("param count --> {}", param);
			book.setCount(Util.getIntOrElse(param, null));
			param = request.getParameter("description");
			log.trace("param description --> {}", param);
			book.setDescription(param);
			param = request.getParameter("cover");
			log.trace("param cover --> {}", param);
			book.setCover(request.getParameter("cover"));
		} catch (Exception e) {
			// do nothing
		}
		return book;
	}

}
