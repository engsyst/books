package ua.nure.order.server.dao.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.nure.order.client.SQLCountWrapper;
import ua.nure.order.entity.Product;
import ua.nure.order.entity.book.Book;
import ua.nure.order.entity.order.Delivery;
import ua.nure.order.entity.order.Order;
import ua.nure.order.entity.order.OrderStatus;
import ua.nure.order.server.dao.DAOException;
import ua.nure.order.server.dao.OrderDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class MysqlOrderDAO implements OrderDAO {

    private static final Logger log = LoggerFactory.getLogger(MysqlOrderDAO.class);
    public static final String LOG_MSG_QUERY = "Query --> {}";
    public static final String LOG_MSG_START = "Start";
    public static final String LOG_MSG_FINISH = "Finish";
    public static final String LOG_MSG_RESULT = "Result --> {}";
    private final ConnectionManager cm;

    MysqlOrderDAO(ConnectionManager cm) {
        this.cm = cm;
    }

    @Override
    public int makeOrder(Integer userId, Map<Product, Integer> items, Delivery delivery)
            throws DAOException {
        log.trace(LOG_MSG_START);
        Connection con = null;
        int orderId;
        int dId;
        try {
            con = cm.getConnection(false);
            log.debug("Try add delivery.");
            dId = addDelivery(con, delivery, userId);
            log.debug("Try add order.");
            orderId = addOrder(con, items, dId, userId);
            for (Entry<Product, Integer> e : items.entrySet()) {
                log.debug("Try add book to the order. {}", e);
                addBookHasOrder(con, e.getKey().getId(), orderId, e.getValue());
            }
            con.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(con);
            log.error("Can not add order", e);
            throw new DAOException("Can not add order", e);
        } finally {
            ConnectionManager.close(con);
        }
        log.debug(LOG_MSG_RESULT, orderId);
        log.trace(LOG_MSG_START);
        return orderId;
    }

    int addDelivery(Connection con, Delivery delivery, Integer userId) throws SQLException {
        log.trace(LOG_MSG_START);

        try (PreparedStatement st = con.prepareStatement(Querys.SQL_INSERT_DELIVERY,
                Statement.RETURN_GENERATED_KEYS)) {
            // "INSERT INTO `delivery` (`name`, `phone`, `email`, `address`,  `description`, `user_id`) VALUES (?, ?, ?, ?, ?, ?)"
            int k = 0;
            st.setString(++k, delivery.getName());
            st.setString(++k, delivery.getPhone());
            st.setString(++k, delivery.getEmail());
            st.setString(++k, delivery.getAddress());
            st.setString(++k, delivery.getDescription());
            SqlUtil.setIntOrNull(st, ++k, userId, 0);
            log.debug(LOG_MSG_QUERY, st);
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) {
                rs.next();
                int newId = rs.getInt(1);
                log.debug(LOG_MSG_RESULT, newId);
                log.trace(LOG_MSG_FINISH);
                return newId;
            }
        }
    }

    private void addBookHasOrder(Connection con, Integer bookId, int orderId, Integer count)
            throws SQLException {
        log.trace(LOG_MSG_START);
        try (PreparedStatement st = con.prepareStatement(Querys.SQL_INSERT_BOOK_HAS_ORDER)) {
            st.setInt(1, bookId);
            st.setInt(2, orderId);
            st.setInt(3, count);
            log.debug(LOG_MSG_QUERY, st);
            st.executeUpdate();
            log.trace(LOG_MSG_FINISH);
        }
    }

    public int addOrder(Connection con, Map<Product, Integer> items, int deliveryId, Integer userId) throws SQLException {
        log.trace(LOG_MSG_START);

        try (PreparedStatement st = con.prepareStatement(Querys.SQL_INSERT_ORDER,
                Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, 0);
            st.setInt(2, deliveryId);
            SqlUtil.setIntOrNull(st, 3, userId, 0);
            log.debug(LOG_MSG_QUERY, st);
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            rs.next();
            int newId = rs.getInt(1);
            log.debug(LOG_MSG_RESULT, newId);
            log.trace(LOG_MSG_FINISH);
            return newId;
        }
    }

    @Override
    public List<Order> list(String pattern, String orderColumn, boolean ascending, int start, int count,
                            SQLCountWrapper total) throws DAOException {
        log.trace(LOG_MSG_START);
        List<Order> orders;
        try (Connection con = cm.getConnection()) {
            log.debug("Try list orders with params --> pattern: {}, orderColumn: {}, "
            		+ "ascending: {}, start: {}, count: {}, total: {}", 
            		pattern, orderColumn, ascending, start, count, total);
            orders = listOrders(con, pattern, orderColumn, ascending, start, count, total);
        } catch (SQLException e) {
            log.error("listBooks: Can not listBooks", e);
            throw new DAOException("Can not listBooks", e);
        }
        log.trace(LOG_MSG_FINISH);
        return orders;
    }

    private List<Order> listOrders(Connection con, String pattern, String orderColumn,
                                   boolean ascending, int start, int count, SQLCountWrapper total)
            throws SQLException {
        log.trace(LOG_MSG_START);
        List<Order> orders = new ArrayList<>();
        List<Integer> list;
        log.debug("Get orders ID with given pattern.");
        String where = pattern == null || pattern.length() == 0 ? "" :
                " WHERE `status` = '" + pattern + "' ";
        String order = orderColumn == null || orderColumn.length() == 0
                ? " ORDER BY `order_id` DESC"
                : " ORDER BY " + orderColumn + (ascending ? " ASC" : " DESC")
                + ",`order_id` DESC";
        String limit = (count == 0 ? "" : " LIMIT " + start + "," + count);
        String query = Querys.SQL_GET_ORDERS_ID + where + order + limit;
        try (PreparedStatement st = con.prepareStatement(query)) {
            log.debug(LOG_MSG_QUERY, query);
            try (ResultSet rs = st.executeQuery()) {
                list = SqlUtil.unmapIdList(rs);
            }
            // return if no orders with given status found
            if (list.isEmpty())
                return orders;
        }

        log.debug("Get orders with given IDs.");
        String whereBooks = " WHERE `order_id` IN " + SqlUtil.listToIN(list);
        query = Querys.SQL_GET_FULL_ORDERS + whereBooks + order;
        try (PreparedStatement st = con.prepareStatement(query,
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            log.debug(LOG_MSG_QUERY, query);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    orders.add(unmapOrder(rs));
                }
            }
        }

        if (total != null) {
            log.debug("Get count of founded orders.");
            query = Querys.SQL_FIND_ORDERS_COUNT + where;
            log.debug(LOG_MSG_QUERY, query);
            try (PreparedStatement st = con.prepareStatement(query)) {
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        total.setCount(rs.getInt(1));
                        log.debug("Count of orders --> {}", total.getCount());
                    }
                }
            }
        }
        log.trace(LOG_MSG_FINISH);
        return orders;
    }

    Order unmapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("order_id"));
        order.setTitle(rs.getString("login"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setPrice(rs.getInt("price"));
        try {
            order.setDelivery(unmapDelivery(rs));
        } catch (SQLException e) {
            // DO: nothing
        }
        order.setItems(new Hashtable<>());
        Product item = unmapProductForOrder(rs);
        order.getItems().put(item, rs.getInt("count"));
        while (rs.next()) {
            if (rs.getInt("order_id") != order.getId()) {
                rs.previous();
                break;
            }
            item = unmapProductForOrder(rs);
            order.getItems().put(item, rs.getInt("count"));
        }
        return order;
    }

    Delivery unmapDelivery(ResultSet rs) throws SQLException {
        Delivery delivery = new Delivery();
        delivery.setName(rs.getString("name"));
        delivery.setPhone(rs.getString("phone"));
        delivery.setEmail(rs.getString("email"));
        delivery.setAddress(rs.getString("address"));
        delivery.setDescription(rs.getString("description"));
        return delivery;

    }

    Product unmapProductForOrder(ResultSet rs) throws SQLException {
        Product item = new Book();
        item.setId(rs.getInt("book_id"));
        item.setTitle(rs.getString("title"));
        item.setPrice(rs.getDouble("price"));
        return item;
    }

    @Override
    public Order getOrderStatus(int id) throws DAOException {
        Connection con = null;
        Order order;
        try {
            con = cm.getConnection();
            order = getOrderStatus(con, id);
        } catch (SQLException e) {
            ConnectionManager.rollback(con);
            log.error("Can not add order", e);
            throw new DAOException("Can not add order", e);
        } finally {
            ConnectionManager.close(con);
        }
        return order;
    }

    Order getOrderStatus(Connection con, int id) throws SQLException {
        Order order = new Order();
        try(PreparedStatement st = con.prepareStatement(Querys.SQL_GET_ORDER_STATUS)){
            st.setInt(1, id);
            st.executeQuery();
            ResultSet rs = st.getResultSet();
            rs.next();
            order.setId(rs.getInt(1));
            order.setStatus(OrderStatus.valueOf(rs.getString(2)));
            return order;
        }
    }

    @Override
    public void updateStatus(int id, OrderStatus status) throws DAOException {
        Connection con = null;
        try {
            con = cm.getConnection(false);
            updateOrder(con, id, status);
            con.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(con);
            log.error("Can not add order", e);
            throw new DAOException("Can not update order status | " + e.getMessage(), e);
        } finally {
            ConnectionManager.close(con);
        }
    }

    private void updateOrder(Connection con, int id, OrderStatus status) throws SQLException {
        try (PreparedStatement st = con.prepareStatement(Querys.SQL_UPDATE_ORDER_STATUS)) {
            st.setInt(2, id);
            st.setString(1, status.toString());
            st.executeUpdate();
        }
    }

    @Override
    public Order getOrderDetail(int id) throws DAOException {
        Order order;
        try (Connection con = cm.getConnection()) {
            order = getOrderDetail(con, id);
        } catch (SQLException e) {
            log.error("Can not add order", e);
            throw new DAOException("Can not get order | " + e.getMessage(), e);
        }
        return order;
    }

    Order getOrderDetail(Connection con, int id) throws SQLException {
        Order order = new Order();
        String query = Querys.SQL_GET_ORDER_DETAL;
        try (PreparedStatement st = con.prepareStatement(query)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                order = unmapOrder(rs);
            }
        }
        return order;
    }

}
