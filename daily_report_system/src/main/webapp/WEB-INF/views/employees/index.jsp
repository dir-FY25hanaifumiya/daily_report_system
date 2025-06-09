<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="constants.AttributeConst" %>
<%@ page import="constants.ForwardConst" %>

<c:set var="action" value="${ForwardConst.ACT_EMP.getValue()}" />
<c:set var="cmdIdx" value="${ForwardConst.CMD_INDEX.getValue()}" />
<c:set var="cmdNew" value="${ForwardConst.CMD_NEW.getValue()}" />
<c:set var="cmdCreate" value="${ForwardConst.CMD_CREATE.getValue()}" />
<c:set var="cmdShow" value="${ForwardConst.CMD_SHOW.getValue()}" />
<c:set var="cmdEdit" value="${ForwardConst.CMD_EDIT.getValue()}" />
<c:set var="cmdUpdate" value="${ForwardConst.CMD_UPDATE.getValue()}" />
<c:set var="cmdDestroy" value="${ForwardConst.CMD_DESTROY.getValue()}" />

<c:import url="../layout/app.jsp">
    <c:param name="content">
        <c:if test="${flush != null}">
            <div id="flush_success">
                <div id="flush">${flush}</div>
            </div>
        </c:if>
        <h2>従業員 一覧</h2>
        <table id="employee_list">
            <tbody>
                <tr>
                    <th>社員番号</th>
                    <th>氏名</th>
                    <th>削除</th>
                </tr>
                <c:forEach var="employee" items="${employees}" varStatus="status">
                    <tr class="${status.count % 2}">
                        <td><c:out value="${employee.code}" /></td>
                        <td><c:out value="${employee.name}" /></td>
                        <td>
                            <c:choose>
                                <c:when test="${employee.deleteFlag == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()}">
                                    削除済み
                                </c:when>
                                <c:otherwise>
                                    <a href="<c:url value='?action=${action}&command=${cmdShow}&id=${employee.id}' />">詳細を見る</a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

        <div id="pagination">
            (全${employees_count}件) <br />
            <c:forEach begin="1" end="${(employees_count / maxRow) + 1}" step="1" var="i">
                <c:choose>
                    <c:when test="${i == page}">
                        ${i} &nbsp;
                    </c:when>
                    <c:otherwise>
                        <a href="<c:url value='?action=${action}&command=${cmdIdx}&page=${i}' />"><c:out value="${i}" /></a>&nbsp;
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
        <p><a href="<c:url value='?action=${action}&command=${cmdNew}' />">新規従業員の登録</a></p>
    </c:param>
</c:import>