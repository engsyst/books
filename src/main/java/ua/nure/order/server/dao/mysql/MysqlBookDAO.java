package ua.nure.order.server.dao.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.client.SQLCountWrapper;
import ua.nure.order.entity.book.Author;
import ua.nure.order.entity.book.Book;
import ua.nure.order.entity.book.Category;
import ua.nure.order.server.dao.BookDAO;
import ua.nure.order.server.dao.DAOException;
import ua.nure.order.server.dao.InsertException;
import ua.nure.order.server.dao.UpdateException;

import java.sql.*;
import java.util.*;

class MysqlBookDAO implements BookDAO {
    private static final Logger log = LoggerFactory.getLogger(MysqlBookDAO.class);
    public static final String LOG_MSG_QUERY = "Query --> {}";
    public static final String LOG_MSG_START = "Start";
    public static final String LOG_MSG_FINISH = "Finish";
    public static final String LOG_MSG_RESULT = "Result --> {}";
    private final ConnectionManager cm;

    MysqlBookDAO(ConnectionManager cm) {
        this.cm = cm;
    }

    void mapBook(PreparedStatement st, Book item) throws SQLException {
        // book` (`title`, `isbn`, `price`, `count`, `category_id`)
        int k = 0;
        st.setString(++k, item.getTitle());
        st.setString(++k, item.getIsbn());
        st.setDouble(++k, item.getPrice());
        st.setInt(++k, item.getCount());
        st.setInt(++k, item.getCategory().ordinal() + 1);
    }

    Book unmapBook(ResultSet rs) throws SQLException {
        List<Author> authors = new ArrayList<>();
        String[] a = rs.getString("authors").split(",");
        for (String title : a) {
            authors.add(new Author(title));
        }
        return new Book(rs.getInt("id"), rs.getString("title"), authors, rs.getString("isbn"),
                rs.getDouble("price"), Category.fromValue(rs.getString("category")), rs.getInt("count"));
    }

    List<Integer> getExistedAuthors(Connection con, List<Author> auth) throws SQLException {
        log.trace(LOG_MSG_START);
        // Make query string
        StringBuilder sb = new StringBuilder();
        for (Author a : auth) {
            sb.append("title = '");
            sb.append(a.getTitle());
            sb.append("' or ");
        }
        sb.delete(sb.lastIndexOf("'") + 1, sb.length());
        sb.append(";");
        String getQuery = Querys.SQL_GET_AUTHORS + sb;
        log.debug(LOG_MSG_QUERY, getQuery);

        List<Integer> res = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(getQuery)) {
            log.debug("Try execute");
            try (ResultSet rs = st.executeQuery()) {
                log.debug("Try get result");
                while (rs.next()) {
                    res.add(rs.getInt(1));
                    auth.remove(new Author(rs.getString(2)));
                }
            }
        }
        log.debug(LOG_MSG_RESULT, res);
        log.trace(LOG_MSG_FINISH);
        return res;

    }

    @Override
    public int addBook(Book item) throws InsertException {
        Connection con = null;
        int bookId;
        try {
            con = cm.getConnection(false);
            log.debug("Try add book");
            bookId = addBook(con, item);
            con.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(con);
            log.error("Can not add book: " + item, e);
            throw new InsertException("Can not add book", e);
        } finally {
            ConnectionManager.close(con);
        }
        return bookId;
    }

    int addBook(Connection con, Book item) throws SQLException {
        log.trace(LOG_MSG_START);
        int bookId;
        log.debug(LOG_MSG_QUERY, Querys.SQL_ADD_BOOK);
        log.debug("Book --> {}", item);

        try (PreparedStatement st = con.prepareStatement(Querys.SQL_ADD_BOOK, Statement.RETURN_GENERATED_KEYS)) {
            mapBook(st, item);
            int count = st.executeUpdate();
            if (count == 0) {
                throw new SQLException("addAuthors: No data inserted");
            }
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    bookId = rs.getInt(1);
                    log.debug("Inserted book id --> {}", bookId);
                } else {
                    log.error("No data inserted");
                    throw new SQLException("addBook: No data inserted");
                }
            }
        }
        log.debug("Try add authors");
        List<Integer> aIds = addAuthors(con, item.getAuthor());
        String q = Querys.SQL_ADD_BOOK_AUTHORS + SqlUtil.pairToValues(aIds, bookId);
        log.debug(LOG_MSG_QUERY, q);
        try (PreparedStatement st = con.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            st.executeUpdate();
        }
        log.debug(LOG_MSG_RESULT, bookId);
        log.trace(LOG_MSG_FINISH);
        return bookId;

    }

    @Override
    public List<Book> listBooks(String pattern) throws DAOException {
        return list(pattern, "title", true, 0, 0, null);
    }

    @Override
    public List<Book> list(String pattern, String orderColumn, boolean ascending,
                           int start, int count, SQLCountWrapper total) throws DAOException {
        log.trace(LOG_MSG_START);
        List<Book> books;
        try (Connection con = cm.getConnection()) {
            log.debug("Try list book with pattern --> {}; orderColumn --> {}; ascending --> {}; start --> {}; count --> {}",
                    pattern, orderColumn, ascending, start, count);
            books = listBooks(con, pattern, orderColumn, ascending, start, count, total);
        } catch (SQLException e) {
            String msg = "Can not list books: '" + pattern + "', '" + orderColumn
                    + "', from: " + start + ", by: " + count;
            log.error(msg + count, e);
            throw new DAOException(msg, e);
        }
        log.trace(LOG_MSG_FINISH);
        return books;
    }

    List<Book> listBooks(Connection con, String pattern, String orderColumn, boolean ascending,
                         int start, int count, SQLCountWrapper total) throws SQLException {
        log.trace(LOG_MSG_START);
        List<Book> books = new ArrayList<>();
        String where = pattern == null || pattern.length() == 0 ? "" :
                " WHERE title LIKE '%" + pattern + "%' OR authors LIKE '%" + pattern + "%' ";
        String order = orderColumn == null || orderColumn.length() == 0 ? "" : "ORDER BY "
                + orderColumn + (ascending ? " ASC" : " DESC");
        String limit = (count == 0 ? "" : " LIMIT " + start + "," + count);
        String query = Querys.SQL_FIND_BOOKS + where + order + limit;
        log.debug(LOG_MSG_QUERY, query);
        try (PreparedStatement st = con.prepareStatement(query)) {
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    books.add(unmapBook(rs));
                }
            }
            if (total != null) {
                log.debug("Try get total books.");
                query = Querys.SQL_FIND_BOOKS_COUNT + where;
                log.debug(LOG_MSG_QUERY, query);
                try (PreparedStatement pst = con.prepareStatement(query)) {
                    try (ResultSet rs = pst.executeQuery()) {
                        while (rs.next()) {
                            total.setCount(rs.getInt(1));
                            log.debug("Total --> {}", total);
                        }
                    }
                }
            }
        }
        log.debug(LOG_MSG_RESULT, books);
        log.trace(LOG_MSG_FINISH);
        return books;
    }

    @Override
    public Map<Integer, String> listAuthors() throws DAOException {
        log.trace(LOG_MSG_START);
        Map<Integer, String> authors;
        try (Connection con = cm.getConnection()) {
            log.debug("Try list authors.");
            authors = listAuthors(con);
        } catch (SQLException e) {
            log.error("Can not list authors.", e);
            throw new DAOException("Can not list authors", e);
        }
        log.trace(LOG_MSG_FINISH);
        return authors;
    }

    public Map<Integer, String> listAuthors(Connection con) throws SQLException {
        log.trace(LOG_MSG_START);
        Map<Integer, String> authors = new TreeMap<>();
        log.debug(LOG_MSG_QUERY, Querys.SQL_LIST_AUTHORS);
        try (PreparedStatement st = con.prepareStatement(Querys.SQL_LIST_AUTHORS)) {
            try(ResultSet rs = st.executeQuery()){
                while (rs.next()) {
                    authors.put(rs.getInt(1), rs.getString(2));
                }
            }
        }
        log.debug(LOG_MSG_RESULT, authors);
        log.trace(LOG_MSG_FINISH);
        return authors;
    }

    List<Integer> addAuthors(Connection con, List<Author> list) throws SQLException {
        log.trace(LOG_MSG_START);
        assert list.isEmpty() : "Empty authors";
        List<Author> auth = new ArrayList<>(list);
        log.debug("Make copy of book authors --> {}", auth);
        log.debug("Try get Existed Authors and remove it from insert -- >");
        List<Integer> res = getExistedAuthors(con, auth);
        if (auth.isEmpty()) {
            log.debug("Nothing to add.");
            log.trace(LOG_MSG_FINISH);
            return res;
        }
        List<String> toAdd = new ArrayList<>();
        for (Author a : auth) {
            toAdd.add(a.getTitle());
        }
        String addQuery = Querys.SQL_ADD_AUTHOR + SqlUtil.listToValues(toAdd);
        log.debug(LOG_MSG_QUERY, addQuery);
        try (PreparedStatement st = con.prepareStatement(addQuery, Statement.RETURN_GENERATED_KEYS)) {
            int count = st.executeUpdate();
            if (count == 0) {
                log.error("No data inserted");
                throw new SQLException("addAuthors: No data inserted");
            }
            try (ResultSet rs = st.getGeneratedKeys()) {
                while (rs.next()) {
                    res.add(rs.getInt(1));
                }
            }
        }
        log.debug(LOG_MSG_RESULT, res);
        log.trace(LOG_MSG_FINISH);
        return res;
    }

    @Override
    public Book getBook(int id) throws DAOException {
        log.trace(LOG_MSG_START);
        try (Connection con = cm.getConnection()) {
            log.debug("Try get book --> {}", id);
            return getBook(con, id);
        } catch (SQLException e) {
            log.error("Can not get book by id: " + id, e);
            throw new DAOException("Can not getBook by id " + id, e);
        }
    }

    public Book getBook(Connection con, int id) throws SQLException {
        log.trace(LOG_MSG_START);
        Book book;
        log.debug(LOG_MSG_QUERY, Querys.SQL_FIND_BOOK_BY_ID);
        try (PreparedStatement st = con.prepareStatement(Querys.SQL_FIND_BOOK_BY_ID)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                book = unmapBook(rs);
                log.debug(LOG_MSG_RESULT, book);
                return book;
            }
        }
        log.trace(LOG_MSG_FINISH);
        return null;
    }

    @Override
    public boolean getBooksCount(Set<Book> books) throws DAOException {
        log.trace(LOG_MSG_START);
        try (Connection con = cm.getConnection()) {
            log.debug("Try get books count.");
            return getBooksCount(con, books);
        } catch (SQLException e) {
            log.error("Can not get book count.", e);
            throw new DAOException("Can not get book count", e);
        }
    }

    boolean getBooksCount(Connection con, Set<Book> books) throws SQLException {
        log.trace(LOG_MSG_START);
        if (books == null || books.isEmpty()) {
            log.debug("Initial --> {}", books);
            return false;
        }
        List<Integer> ids = new ArrayList<>();
        for (Book b : books) {
            ids.add(b.getId());
        }
        log.debug("Initial --> {}", books);
        String query = Querys.SQL_GET_BOOKS_COUNT + SqlUtil.listToIN(ids);
        log.debug(LOG_MSG_QUERY, query);
        try (PreparedStatement st = con.prepareStatement(query)) {
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                int c = rs.getInt(2);
                for (Book b : books) {
                    if (b.getId() == id) {
                        b.setCount(c);
                        break;
                    }
                }
            }
            log.debug(LOG_MSG_RESULT, books);
            log.trace(LOG_MSG_FINISH);
            return true;
        }
    }

    @Override
    public void updateBook(Book item) throws UpdateException {
        // 1. ?????????
        // getBook with id
        // replace changed fields
        // updateBook
        // OR
        // 2. ????????????
        // simply updateBook
        log.trace(LOG_MSG_START);
        Connection con = null;
        try {
            con = cm.getConnection(false);
            log.debug("Try update book");
            updateBook(con, item);
            con.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(con);
            log.error("Can not update book: '" + item + "'", e);
            throw new UpdateException("Can not update book", e);
        } finally {
            ConnectionManager.close(con);
        }
        log.trace(LOG_MSG_FINISH);
    }

    void updateBook(Connection con, Book book) throws SQLException {
        log.trace(LOG_MSG_START);

        try (PreparedStatement st = con.prepareStatement(Querys.SQL_UPDATE_BOOK)) {
            int k = 0;
            // SET `title` = ?,`isbn` = ?,`price` = ?,`count` = ?,`category_id`= ?,`cover` = ?,`description`= ? WHERE `id` = ?;
            st.setString(++k, book.getTitle());
            st.setString(++k, book.getIsbn());
            st.setDouble(++k, book.getPrice());
            st.setInt(++k, book.getCount());
            st.setInt(++k, book.getCategory().ordinal() + 1);
            st.setString(++k, book.getCover());
            st.setString(++k, book.getDescription());
            st.setInt(++k, book.getId());
            log.debug(LOG_MSG_QUERY, st);
            st.executeUpdate();
        }
        try (PreparedStatement st = con.prepareStatement(Querys.SQL_DELETE_AUTHOR_HAS_BOOK)) {
            st.setInt(1, book.getId());
            log.debug(LOG_MSG_QUERY, st);
            st.executeUpdate();
        }
        log.debug("Try add authors");
        List<Integer> aIds = addAuthors(con, book.getAuthor());
        String q = Querys.SQL_ADD_BOOK_AUTHORS + SqlUtil.pairToValues(aIds, book.getId());
        log.debug(LOG_MSG_QUERY, q);
        try (PreparedStatement st = con.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            st.executeUpdate();
        }
        log.debug(LOG_MSG_RESULT, book);
        log.trace(LOG_MSG_FINISH);
    }

    @Override
    public Map<Integer, String> getCategories() throws DAOException {
        Map<Integer, String> cats;
        try (Connection con = cm.getConnection()) {
            log.debug("Try get categories");
            cats = getCategories(con);
        } catch (SQLException e) {
            log.error("Can not get categories.", e);
            throw new DAOException("Can not get categories", e);
        }
        return cats;
    }

    Map<Integer, String> getCategories(Connection con) throws SQLException {
        log.trace(LOG_MSG_START);
        LinkedHashMap<Integer, String> cats = new LinkedHashMap<>();
        try (PreparedStatement st = con.prepareStatement(Querys.SQL_GET_CATEGORIES)) {
            log.debug(LOG_MSG_QUERY, st);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    cats.put(rs.getInt(1), rs.getString(2));
                }
            }
        }
        log.debug(LOG_MSG_RESULT, cats);
        log.trace(LOG_MSG_FINISH);
        return cats;
    }

}
