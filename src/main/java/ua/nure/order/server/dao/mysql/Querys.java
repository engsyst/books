package ua.nure.order.server.dao.mysql;

public interface Querys {
	String SQL_ADD_BOOK = "INSERT INTO `book` "
			+ "(`title`, `isbn`, `price`, `count`, `category_id`) "
			+ "VALUES (?, ?, ?, ?, ?);";
	
	String SQL_FIND_BOOKS = "SELECT `id`, `title`, `authors`, "
			+ "`isbn`, `price`, `count`, `category` FROM `books` ";
	
	String SQL_FIND_BOOK_BY_ID = "SELECT `id`, `title`, `authors`, "
			+ "`isbn`, `price`, `count`, `category` FROM `books` WHERE ID = ?";
	
	String SQL_FIND_BOOKS_COUNT = "SELECT count(*) FROM `books` ";

	String SQL_UPDATE_BOOKS_COUNT = "SELECT count(*) FROM `books` ";

	String SQL_DELETE_AUTHOR_HAS_BOOK = "DELETE FROM `author_has_book` WHERE `book_id`=?";
	
	String SQL_UPDATE_BOOK = "UPDATE `book` SET `title` = ?,`isbn` = ?,`price` = ?,`count` = ?,`category_id`= ?,`cover` = ?,`description`= ? WHERE `id` = ?";

	String SQL_GET_CATEGORIES = "SELECT `id`,`title` FROM `category`";
	
	/**
	 *  use makeAuthorsValues(List? authors, int bookId)
	 *  to generate values string in format: (name1,n),(name2,n) ... ;
	 */
    String SQL_ADD_AUTHOR = "INSERT INTO `author` "
			+ "(`title`) VALUES ";

	String SQL_ADD_BOOK_AUTHORS = "INSERT INTO `author_has_book` (`author_id`, `book_id`) VALUES ";

	String SQL_GET_CATEGORY_ID = "SELECT id FROM category WHERE title = ?";
	
	String SQL_GET_AUTHORS = "SELECT id, title FROM author WHERE ";
	
	String SQL_GET_BOOK_AUTHORS = "select author.id, author.title from author "
			+ "inner join author_has_book on author.id = author_id "
			+ "inner join book on book_id = book.id where book.id = ?";
	
	String SQL_LIST_AUTHORS = "SELECT id, title FROM author";
	
	String SQL_GET_USER = "SELECT `id`,`login`,`password`,`role`,`e-mail`,`phone`,`name`,`address`,`avatar`,`description` FROM `user` WHERE login = ?";

	String SQL_ADD_USER = "INSERT INTO `user` (`login`,`password`,`role`,`e-mail`,`phone`,`name`,`address`,`avatar`,`description`) "
			+ "VALUES (?,?,?,?,?,?,?,?,?)";

	String SQL_UPDATE_USER = "UPDATE `user` "
			+ "SET `login` = ?,`password` = ?,`role` = ?,`e-mail` = ?,`phone` = ?,"
			+ "`name` = ?,`address` = ?,`avatar` = ?,`description` = ? WHERE `id` = ?;";
	
	String SQL_INSERT_ORDER = "INSERT INTO `order` (`no`, `delivery_id`, `user_id`) VALUES (?, ?, ?);";
	
	String SQL_INSERT_BOOK_HAS_ORDER = "INSERT INTO `book_has_order` "
			+ "(`book_id`, `order_id`, `count`) VALUES (?, ?, ?);";
	
	String SQL_INSERT_DELIVERY = "INSERT INTO `delivery` (`name`, `phone`, `email`, `address`,  `description`, `user_id`) VALUES (?, ?, ?, ?, ?, ?)";

	String SQL_GET_BOOKS_COUNT = "SELECT `id`, `count` FROM `book` WHERE `id` IN ";
	
	String SQL_GET_FULL_ORDERS = "SELECT `user_id`,`login`,`order_id`,`status`,`book_id`,`title`,`count`,`price`,`osum` FROM orders ";
	
	String SQL_GET_ORDER_BY_ID = "SELECT `id`,`no`,`user_id`,`date`,`status` FROM `order` WHERE `id` = ?";

	String SQL_GET_ORDER_STATUS = "SELECT `id`,`status` FROM `order` WHERE `id` = ?";

	String SQL_GET_ORDER_DETAL = "SELECT DISTINCT `orders`.`user_id`,`login`,`order_id`,`status`,"
			+ "`book_id`,`title`,`count`,`price`,`osum`,`name`,`phone`,`email`,`address`,`description` "
			+ "FROM `orders`,`delivery` WHERE `order_id` = ? AND `delivery_id` = `delivery`.`id`;";

	String SQL_UPDATE_ORDER_STATUS = "UPDATE `order` SET `status` = ? WHERE `id` = ?";

	String SQL_FIND_ORDERS_COUNT = "SELECT count(*) FROM `order` ";

	String SQL_GET_ORDERS_ID = "SELECT DISTINCT `order_id` FROM `orders` ";

}
