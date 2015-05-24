<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<html>
<head>
<!-- Latest compiled and minified CSS -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">

<!-- Optional theme -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme.min.css">

<!-- Latest compiled and minified JavaScript -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
</head>
<body>
	<h2>This is the list of Navbox template subtemplates</h2>
	<table class="table">
		<thead>
			<tr>
				<th>#</th>
				<th>Original template title</th>
				<th>Existing articles</th>
				<th>Non-existing articles</th>
				<th>Percentage</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${list}" var="item" varStatus="status">
				<tr class="${item.context}">
					<td><c:out value="${status.index}" /></td>
					<td>
					   <a href="http://en.wikipedia.org/wiki/<c:out value="${item.article}"/>">
					       <c:out value="${item.article}"/>
					   </a>
				    </td>
                    <td><c:out value="${item.existing}"/></td>
                    <td><c:out value="${item.nonExisting}"/></td>
                    <td><fmt:formatNumber type="number" pattern="#.00" value="${item.percentage}" />%</td>
                    <td><a href="/WikiNavbar/MyServlet2/?langFrom=en&langTo=uk&titleFrom=${item.article}">Translate</a></td>
                </tr>
            </c:forEach>
        </tbody>
	</table>
	
	<a href="/WikiNavbar/MyServlet/?continue=${continueParam}" class="btn btn-primary btn-lg active pull-right" role="button">Continue</a>
</body>
</html>