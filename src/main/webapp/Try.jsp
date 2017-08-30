<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<style>
h1 {
	color: black;
	font-family: verdana;
	font-size: 250%;
}

p {
	color: Black;
	font-family: courier;
	font-size: 120%;
}
</style>
</head>
<body>
	<%@ page import="java.util.*"%>
	<%@ page import="logic.*"%>
	<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

	<h1>DOI CHECKER INTERFACE</h1>
	<img src="DOI.jpg" alt="google.com"
		style="width: 320px; height: 180px;">
	<p>Willkommen beim DOI Checker!</p>

	<form action="Try.jsp" method="POST">
		<input type="submit" name="checkDoi" value="DOIs checken">
	</form>	<br>
	Historie abfragen
	<form action="Try.jsp" method="POST">
		<input type="text" name="displayDoiChecksByDoi" value="10.15771/">
		<input type="submit">
	</form><br>
	Nach ResponseCode sortieren
	<form action="Try.jsp" method="POST">
		<input type="text" name="displayDoiChecksByResponseCode" value="404">
		<input type="submit">
	</form>
	<%
		DoiChecker myDoiChecker = new DoiChecker(DoiChecker.DOXI_URL);
		if (request.getParameter("checkDoi") != null)
		{
			myDoiChecker.check();
		}
		else if (request.getParameter("displayDoiChecksByDoi") != null)
		{
			String doi = request.getParameter("displayDoiChecksByDoi");
			List<DoiResponse> myList = myDoiChecker.getHistorie(doi);
		pageContext.setAttribute("doiList", myList);	
		}
	else if (request.getParameter("displayDoiChecksByResponseCode") != null){
			String responseCode = request.getParameter("displayDoiChecksByResponseCode"); 	
			List<DoiResponse>myList = myDoiChecker.getByResponseCode(Integer.parseInt(responseCode));
			pageContext.setAttribute("doiList", myList);
		}
		
	%>
	<br>
	<table border="1">

		<thead>
			<tr>
				<th>Doi</th>
				<th>Response Code</th>
				<th>Timestamp</th>
			</tr>
		</thead>

		<tbody>
			<c:forEach items="${doiList}" var="item">
				<tr>
					<td><a href="https://doi.org/${item.doi}">${item.doi}</a></td>
					<td>${item.responseCode}</td>
					<td>${item.timestamp}</td>
				</tr>
			</c:forEach>
		</tbody>

	</table>

	<c:forEach items="${doiList}" var="item">
		<a href="10.15771/PURE.844693"></a>
	</c:forEach>

	<!--  
<p>Bitte DOI eingeben um Metadaten abzurufen.</p>
<input type="text" name="Metadaten" value="10.15771/"><input type="submit">
-->
</body>
</html>