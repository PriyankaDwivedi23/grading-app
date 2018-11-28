<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Student List</title>
</head>
<body>
	<h3>List of Students</h3>

	<br> Homework:
	<b>${gradeHomework.homework}</b>

	<br>
	<br> Students:
	<br>

	<c:forEach items="${studentList}" var="student">
		<a href="student?studentName=${student}">${student}</a> <br />
	</c:forEach>

</body>
</html>