<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>
<!-- Latest compiled and minified CSS -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">

<!-- Optional theme -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme.min.css">

<!-- Latest compiled and minified JavaScript -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
</head>
<body>
    <div class="container">

		<div class="page-header">
			<h2>
				Templates without Interwiki
				<p><small>Tool that helps to find templates without interwiki</small></p>
			</h2>
		</div>

		<form id="searchForm" class="form-horizontal" method="get" action="/WikiNavbar/ListTemplatesWithoutInterwiki/">
			<div class="form-group">
				<label for="lang" class="col-sm-2 control-label">Language code:</label>
				<div class="col-sm-1">
					<select name="lang" class="form-control">
					   <option value=""></option>
					<c:forEach items="${supportedLangs}" var="supportedLang">
					   <option value="${supportedLang}" <c:if test="${supportedLang eq searchLang}">selected</c:if>>${supportedLang}</option>
					</c:forEach>
					</select>
				</div>
			</div>
			<div class="form-group">
				<label for="parentTemplate" class="col-sm-2 control-label">Transclude filter:</label>
				<div class="col-sm-3">
					<input type="text" class="form-control" name="parentTemplate" value="${searchTemplate}" placeholder='e.g. "Navbox", "Sidebar" etc.'>
				</div>
				<div class="col-sm-7">
				    <p>Transclude filter shows only templates that transclude given template. For example, it can be useful to see only navigation templates (that transclude "Navbox" template).</p> 
				</div> 
			</div>
			<div class="form-group">
				<div class="checkbox col-sm-offset-2 col-sm-3">
					<label><input type="checkbox" name="includeSubtemplates" <c:if test="${includeSubtemplates}">checked</c:if>> Include subtemplates</label>
				</div>
				<div class="col-sm-6">
                     <p>Include subtemplates like "Template:Navbox/documentation" etc. <strong>Not recommended.</strong></p>
                </div>
			</div>
			<div class="col-sm-offset-4">
			     <button type="submit" class="btn btn-primary">Search</button>
			</div>
		</form>

		<c:if test="${not hideTable}">
			<table class="table table-condensed table-hover">
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
							<td><c:out value="${status.index + 1}" /></td>
							<td><a target="_blank" href="http://${searchLang}.wikipedia.org/wiki/Template:<c:out value="${item}"/>" /> <c:out value="${item}" /> </a></td>
							<td><a target="_blank"
								href="/WikiNavbar/FindTemplateInterwikiDB/?templateName=Template:<c:out value="${item}"/>&templateLang=${searchLang}&searchLangs=en,ru,pl,be,be-x-old&freeze=true">
									Find Interwiki </a></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:if>
		<!-- 		<nav>
			<ul class="pagination pagination-lg">
				<li><a href="#" aria-label="Previous"> <span aria-hidden="true">&laquo;</span>
				</a></li>
				<li><a href="#">1</a></li>
				<li><a href="#">2</a></li>
				<li><a href="#">3</a></li>
				<li><a href="#">4</a></li>
				<li><a href="#">5</a></li>
				<li><a href="#" aria-label="Next"> <span aria-hidden="true">&raquo;</span>
				</a></li>
			</ul>
		</nav> -->
	</div>
</body>
</html>