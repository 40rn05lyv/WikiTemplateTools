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

<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js">
<!-- Latest compiled and minified JavaScript -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
</head>
<body>
	<div>
	    The template to be translated: <b>${titleToBeTranslated}</b><br/>
	    The name of translated template: <input size="50" oninput="myFunction(this.value)"/><br/>
	    
	    <a id="mylink" href="#" style="color:red">http://${targetLang}.wikipedia.org/wiki/Template:</a><br/>

        Auto-translated text:  	    
	    
		<div style="float: left; width: 100%;">
			<textarea style="width: 100%" rows="20">${text}</textarea>
		</div>
		<div style="clear: both;"></div>
	</div>
	
	<script type="text/javascript">
	   var linkBase = 'http://${targetLang}.wikipedia.org/wiki/Template:';
	   function myFunction(val) {
		   var mylink = $("#mylink");
		   if (Boolean(val)) {
			   mylink.text(linkBase + val);
			   mylink.attr("href", linkBase + val);
			   mylink.attr("style", "");
		   } else {
			   mylink.text(linkBase);
			   mylink.attr("href", "#");
			   mylink.attr("style", "color:red");
		   }
	   }
	</script>
</body>
</html>