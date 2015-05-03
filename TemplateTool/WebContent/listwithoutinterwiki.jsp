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
	<h2>List Navbox Templates Without Interwiki Tool</h2>
	<br />
	<h3>Search was conducted for ${searchLang} wikipedia</h3>

	<table class="table">
		<thead>
			<tr>
				<th>#</th>
				<th>Template without interwiki</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${templates}" var="item" varStatus="status">
				<tr>
					<td><c:out value="${status.index}" /></td>
					<td><a href="http://${searchLang}.wikipedia.org/wiki/Template:<c:out value="${item}"/>" /> <c:out value="${item}" /> </a></td>
					<td><a
						href="/WikiNavbar/FindTemplateInterwikiDB/?pageTitle=Template:<c:out value="${item}"/>&pageLang=${searchLang}&searchLangs=en|ru|pl|be|be-x-old">
							Find Interwiki </a></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>

	<a href="/WikiNavbar/MyServlet/?continue=${continueParam}" class="btn btn-primary btn-lg active pull-right" role="button">Continue</a>
</body>
</html>