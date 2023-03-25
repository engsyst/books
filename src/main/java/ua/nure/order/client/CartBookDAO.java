package ua.nure.order.client;

import ua.nure.order.entity.book.Book;
import ua.nure.order.server.dao.BookDAO;
import ua.nure.order.server.dao.DAOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Wrapper class used to get data from the {@link Cart} at jsp.
 * @author engsyst
 *
 */
public class CartBookDAO implements Paginable<Book> {

	private static final Comparator<Book> titleAsc  = Comparator.comparing(Book::getTitle);
	private static final Comparator<Book> titleDesc  = (o1, o2) -> o2.getTitle().compareTo(o1.getTitle());
	private static final Comparator<Book> price  = Comparator.comparingDouble(Book::getPrice);
	private static final Comparator<Book> countComp  = Comparator.comparingInt(Book::getCount);
	private static final Comparator<Book> author  = Comparator.comparing(o -> o.getAuthor().get(0).getTitle());

	private Cart<Book> cart;
	private BookDAO bookDao;

	public Cart<Book> getCart() {
		return cart;
	}

	public void setCart(Cart<Book> cart) {
		this.cart = cart;
	}

	public void setBookDao(BookDAO bookDao) {
		this.bookDao = bookDao;
	}

	@Override
	public List<Book> list(String pattern, String orderColumn, boolean ascending, int start, int count,
			SQLCountWrapper total) throws DAOException {
		Comparator<Book> comparator = switch (orderColumn) {
			case "title" -> ascending ? titleAsc : titleDesc;
			case "author" -> author;
			case "count" -> countComp;
			case "price" -> price;
			default -> titleAsc;
		};
		if (cart == null) {
			total.setCount(0);
			return new ArrayList<>();
		}

		bookDao.getBooksCount(cart.keySet());
		Set<Book> b = cart.keySet();
		ArrayList<Book> books = new ArrayList<>(b);
		books.sort(comparator);
		total.setCount(books.size());
		return books;
	}

}
