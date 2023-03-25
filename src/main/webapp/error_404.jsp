<%@ page pageEncoding="UTF-8" %>
<%@ page isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.io.PrintWriter" %>
	<c:set var="context" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>

<c:set var="title" value="Error" scope="page" />
<body>
	<table id="main-container">
		<tr >
			<td class="content">
			<%-- CONTENT --%>
				
				<h2 class="error"><img alt="error" src="${context }/img/error.gif">
					The following error occurred
				</h2>
				<hr/>
			
				<%-- this way we get the error information (error 404)--%>
				<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}"/>
                <p>Page was not found</p>
				
			<%-- CONTENT --%>
			</td>
		</tr>
	</table>
</body>
</html>