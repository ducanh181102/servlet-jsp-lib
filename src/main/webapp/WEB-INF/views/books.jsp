<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thư viện Đức Anh</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css?v=<%= System.currentTimeMillis() %>">
<body>
<div class="wrapper">
    <div class="header">
        <div class="logo">TV</div>
        <label class="label">Thư viện Đức Anh</label>
        <nav aria-label="Điều hướng chính">
            <a aria-current="page" href="${pageContext.request.contextPath}/borrow">Mượn sách</a>
            <a href="${pageContext.request.contextPath}/return">Trả sách</a>
        </nav>
    </div>
    <div class="line"></div>
    <div class="body">
        <form action="${pageContext.request.contextPath}/borrow" method="post">
            <div class="info">
                <div class="title-page">${borrowBookLabel}</div>
                <label class="label">${titlePageInfo}</label>
            </div>
            <div class="content">
                <div class="search-box">
                    <label class="label" for="memberId">${memberIdLabel}:</label>
                    <input class="input" placeholder="Ví dụ: DG001" type="text" id="memberId" name="memberId" value="${fn:escapeXml(param.memberId)}">
                    <label class="caption">${inputInfoLabel}</label>
                </div>
                <div class="list-box">
                    <label class="label">${selectBookLabel}</label>
                    <div class="book-list hide-scrollbar">
                        <c:forEach items="${books}" var="book">
                            <div class="book-card ${book.status ? 'is-borrowed' : ''}" id="book-card-${book.id}">
                                <input type="radio" <c:if test="${selectedBookId eq book.id}">checked</c:if> id="book-${book.id}" name="bookCard" value="${book.id}" ${book.status ? 'disabled' : ''}>
                                <div class="book-card-info">
                                    <label class="book-name">${book.name}</label>
                                    <label class="book-info">${book.author} · ${bookIdLabel} ${book.id}</label>
                                </div>
                                <c:choose>
                                    <c:when test="${book.status}">
                                        <label class="book-borrowed-status">${borrowedBookLabel}</label>
                                    </c:when>
                                    <c:otherwise>
                                        <label class="book-status">${availableBookLabel}</label>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                </div>
                <div class="button-box">
                    <c:if test="${not empty error}">
                        <p class="msg" role="alert"><c:out value="${error}" /></p>
                    </c:if>
                    <c:if test="${not empty message}">
                        <p class="message" role="status"><c:out value="${message}" /></p>
                    </c:if>
                    <button class="submit-btn" type="submit">${borrowBookLabel}</button>
                </div>
            </div>
        </form>
    </div>
</div>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        const radios = document.querySelectorAll('input[name="bookCard"]');

        function updateSelectedCard() {
            radios.forEach(function (radio) {
                const card = radio.closest(".book-card");
                if (card) {
                    card.classList.toggle("is-selected", radio.checked);
                }
            });
        }

        radios.forEach(function (radio) {
            // lắng nghe sự kiện change
            radio.addEventListener("change", updateSelectedCard);
        });

        document.querySelectorAll(".book-card").forEach(function (card) {
            card.addEventListener("click", function (event) {
                // trả true nếu tìm từ phần tử vừa bấm đi ngược lên các phần tử cha, cho đến khi gặp 1 trong các phần tử trong ngoặc ()
                if (event.target.closest('input[type="radio"], label[for]')) {
                    return;
                }

                const radio = card.querySelector('input[name="bookCard"]');
                if (!radio || radio.disabled) {
                    return;
                }

                radio.checked = true;
                // bắn sự kiện change lên các phần tử cha
                radio.dispatchEvent(new Event("change", { bubbles: true }));
            });
        });

        updateSelectedCard();
    });
</script>
</body>
</html>
