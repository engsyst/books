package ua.nure.order.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ErrorHandler extends HttpServlet {
    static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    String getCause(String msg, Throwable exception) {
    	StringBuilder sb = new StringBuilder("<pre>");
        sb.append(msg)
        	.append(exception.getMessage());
        while (exception.getCause() != null) {
            exception = exception.getCause();
			sb.append("\n")
				.append(exception.getMessage());
        }
        sb.append("</pre>");
        return sb.toString();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // <c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}"/>
        // <c:set var="message" value="${requestScope['jakarta.servlet.error.message']}"/>
        // <c:set var="exception" value="${requestScope['jakarta.servlet.error.exception']}"/>
        int code = (int) req.getAttribute("jakarta.servlet.error.status_code");
        if (code == 404) {
            req.getRequestDispatcher("error.jsp").forward(req, resp);
            return;
        }
        String message = (String) req.getAttribute("jakarta.servlet.error.message");
        Exception exception = (Exception) req.getAttribute("jakarta.servlet.error.exception");
        String causes = getCause(message, exception);
        log.error("Error cause: {}", causes);
        req.setAttribute("causes", causes);
        req.getRequestDispatcher("error.jsp").forward(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
