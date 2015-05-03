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
	<h2>Find Template Interwiki Tool</h2>
	<br />
	<h3>
		Template to search: <a href="http://${templateLang}.wikipedia.org/wiki/${template}">${template}</a>
	</h3>

	<c:choose>
		<c:when test="${not hasTransclusions}">
			<h2>This templates does not have transclusions or transclusions don't have interwiki.</h2>
		</c:when>
		<c:otherwise>
			<table class="table">
				<thead>
					<tr>
						<th>#</th>
						<th>Language</th>
						<th>Template interwiki candidate</th>
						<th>Occurences</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${list}" var="item" varStatus="status">
						<tr>
							<td><c:out value="${status.index}" /></td>
							<td>${item.left}</td>
							<td><a href="http://${item.left}.wikipedia.org/wiki/<c:out value="${item.middle}"/>" /> <c:out value="${item.middle}" /></a></td>
							<td>${item.right}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:otherwise>
	</c:choose>

</body>
</html>