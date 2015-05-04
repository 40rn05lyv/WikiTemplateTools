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
                Find Template Interwiki
                <p><small>Tool that helps to find interwiki for template</small></p>
            </h2>
        </div>
        
        <form id="searchForm" class="form-horizontal" method="get" action="/WikiNavbar/FindTemplateInterwikiDB/">
            <div class="form-group">
                <label for="lang" class="col-sm-2 control-label">Language code:</label>
                <div class="col-sm-1">
                    <select name="lang" class="form-control">
                    <c:forEach items="${supportedLangs}" var="supportedLang">
                       <option value="${supportedLang}" <c:if test="${supportedLang eq templateLang}">selected</c:if>>${supportedLang}</option>
                    </c:forEach>
                    </select>
                    <p>wikipedia.org</p>
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
                <div class="col-sm-offset-2 col-sm-3">
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
        
        
		Template to search: <a href="http://${templateLang}.wikipedia.org/wiki/${template}">${template}</a>

	<c:choose>
		<c:when test="${not hasTransclusions}">
			<h2>This templates does not have transclusions or transclusions don't have interwiki.</h2>
		</c:when>
		<c:otherwise>
            <table class="table table-condensed table-hover">
				<thead>
					<tr>
						<th>#</th>
						<th>Language</th>
						<th>Template interwiki candidate</th>
						<th>Occurences</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${candidates}" var="candidate" varStatus="status">
						<tr>
							<td>${status.index + 1}</td>
							<td><c:forEach items="${candidate.langs.elementSet()}" var="lang" varStatus="subStatus">
								${lang}<c:if test="${not subStatus.last}">, </c:if>
								</c:forEach></td>
							<td><c:forEach items="${candidate.langs.elementSet()}" var="lang" varStatus="subStatus">
									<a href="http://${lang}.wikipedia.org/wiki/<c:out value="${candidate.get(lang)}"/>" />
									<c:out value="${candidate.getForDisplay(lang)}" />
									</a>
									<c:if test="${not subStatus.last}">, </c:if>
								</c:forEach></td>
							<td>${candidate.size()}
							<c:if test="${candidate.langs.elementSet().size() > 1}"> 
							(<c:forEach items="${candidate.langs.elementSet()}" var="lang" varStatus="subStatus">${candidate.langs.count(lang)}<c:if test="${not subStatus.last}">, </c:if></c:forEach>)
							</c:if>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:otherwise>
	</c:choose>
    
    </div>
</body>
</html>