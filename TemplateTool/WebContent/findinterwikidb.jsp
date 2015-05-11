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

<!-- JQuery -->
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>

<!-- Latest compiled and minified JavaScript -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
</head>
<body>
  <div class="container">

    <div class="page-header">
      <h2>
        Find Template Interwiki
        <p>
          <small>Tool that helps to find interwiki for template</small>
        </p>
      </h2>
    </div>

    <form id="searchForm" class="form-horizontal" method="get" action="/WikiNavbar/FindTemplateInterwikiDB/">
      <div class="form-group">
        <label for="templateLang" class="col-sm-3 control-label">Language: </label>
        <div class="col-sm-1">
          <select id="templateLang" name="templateLang" class="form-control">
            <option value=""></option>
            <c:forEach items="${bean.supportedLangs}" var="supportedLang">
              <option value="${supportedLang}" <c:if test="${supportedLang eq bean.templateLang}">selected</c:if>>${supportedLang}</option>
            </c:forEach>
          </select>
        </div>
      </div>
      <div class="form-group">
        <label for="templateName" class="col-sm-3 control-label">Template:</label>
        <div class="col-sm-4">
          <input id="template" type="text" class="form-control" name="templateName" value="${bean.templateName}">
        </div>
        <a id="templateLink" href="http://${bean.templateLang}.wikipedia.org/wiki/Template:${bean.templateName}" target="_blank"
          class="btn btn-link" role="button" style="display: none">Open template</a>
      </div>
      <div class="form-group">
        <label for="searchLangs" class="col-sm-3 control-label">Search interwiki in languages:</label>
        <div class="col-sm-4">
          <input type="text" class="form-control" name="searchLangs" placeholder="e.g. en,de,fr,pl,uk,ru,be,be-x-old"
            value="${bean.getSearchLangsForDisplay()}">
        </div>
        <button type="submit" class="btn btn-primary">Search Interwiki</button>
      </div>
    </form>

    <c:choose>
      <c:when test="${bean.freeze}">
        <c:if test="${bean.result.isTemplateDontExist()}">
          <div class="alert alert-danger" role="alert">This template does not exist.</div>
        </c:if>
        <c:if test="${bean.result.hasNoTransclusions()}">
          <div class="alert alert-danger" role="alert">This template does not have transclusions. Unable to determine interwiki.</div>
        </c:if>
        <c:if test="${bean.result.hasInterwiki()}">
          <div class="alert alert-info" role="alert">This template already has interwiki.</div>
        </c:if>
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
            <c:forEach items="${bean.result.candidates}" var="candidate" varStatus="status">
              <tr>
                <td>${status.index + 1}</td>
                <td><c:forEach items="${candidate.langs.elementSet()}" var="lang" varStatus="subStatus">
                                ${lang}<c:if test="${not subStatus.last}">, </c:if>
                  </c:forEach></td>
                <td><c:forEach items="${candidate.langs.elementSet()}" var="lang" varStatus="subStatus">
                    <a target="_blank" href="http://${lang}.wikipedia.org/wiki/${candidate.get(lang)}"> <c:out
                        value="${candidate.getForDisplay(lang)}" />
                    </a>
                    <c:if test="${not subStatus.last}">, </c:if>
                  </c:forEach></td>
                <td>${candidate.size()}<c:if test="${candidate.langs.elementSet().size() > 1}"> 
                            (<c:forEach items="${candidate.langs.elementSet()}" var="lang" varStatus="subStatus">${candidate.langs.count(lang)}<c:if
                        test="${not subStatus.last}">, </c:if>
                    </c:forEach>)
                            </c:if>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>

  </div>

  <script type="text/javascript">
			var lang = $("#templateLang");
			var template = $("#template");
			var templateLink = $("#templateLink");
			$("#template, #templateLang").on("keyup change", function() {
				var langValue = lang.val();
				var templateValue = template.val();
				if (Boolean(templateValue) && Boolean(langValue)) {
					var newTemplateLink = "http://" + langValue + ".wikipedia.org/wiki/";
					if (templateValue.indexOf(":") == -1) {
						newTemplateLink += "Template:";
					}
					newTemplateLink += templateValue;
					templateLink.attr("href", newTemplateLink);
					templateLink.show();
				} else {
					templateLink.attr("href", "#");
					templateLink.hide();
				}
			});
			template.trigger("change");
		</script>
</body>
</html>