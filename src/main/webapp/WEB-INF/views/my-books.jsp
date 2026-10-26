<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thư viện Đức Anh</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<main class="wrapper return-screen">
    <header class="header">
        <div class="logo">TV</div>
        <span class="label">Thư viện Đức Anh</span>
        <nav aria-label="Điều hướng chính">
            <a href="${pageContext.request.contextPath}/borrow">Mượn sách</a>
            <a aria-current="page" href="${pageContext.request.contextPath}/return">Trả sách</a>
        </nav>
    </header>

    <div class="line"></div>

    <div class="body return-body">
        <section class="info return-info">
            <label class="title-page">Trả sách</label>
            <p class="label">Theo dõi sách đang mượn và thời hạn trả.</p>
        </section>

        <section class="return-content" aria-label="Thông tin sách đang mượn">
            <form class="reader-lookup" action="${pageContext.request.contextPath}/return" method="get">
                <label for="memberId">Mã độc giả</label>
                <div class="lookup-controls">
                    <input id="memberId" name="memberId" type="text" placeholder="Ví dụ: DG001"
                           value="${fn:escapeXml(memberId)}">
                    <button class="lookup-btn" type="submit">Tra cứu</button>
                </div>
            </form>

            <c:if test="${result eq 'returned'}">
                <p class="return-notice return-success" role="status">Đã ghi nhận trả sách thành công.</p>
            </c:if>
            <c:if test="${result eq 'error'}">
                <p class="return-notice return-error" role="alert">Không tìm thấy sách đang mượn để trả.</p>
            </c:if>

            <c:if test="${hasSearched}">
                <div class="loan-summary" aria-label="Tóm tắt sách mượn">
                    <div class="summary-card">
                        <span class="summary-number"><c:out value="${borrowedBooks.size()}"/></span>
                        <span class="summary-label">Sách đang mượn</span>
                    </div>
                    <div class="summary-card summary-card-due">
                        <span class="summary-number"><c:out value="${dueSoonCount}"/></span>
                        <span class="summary-label">Sắp đến hạn</span>
                    </div>
                </div>

                <section class="return-book-list" aria-labelledby="borrowed-books-heading">
                    <div class="return-list-heading">
                        <h2 id="borrowed-books-heading">Danh sách sách</h2>
                        <span><c:out value="${borrowedBooks.size()}"/> cuốn</span>
                    </div>

                    <c:choose>
                        <c:when test="${empty borrowedBooks}">
                            <div class="return-empty-state">
                                <span class="empty-icon" aria-hidden="true">✓</span>
                                <h3>Bạn chưa mượn cuốn sách nào</h3>
                                <p>Sách mượn bằng mã độc giả này sẽ xuất hiện tại đây.</p>
                                <a href="${pageContext.request.contextPath}/borrow">Tìm sách để mượn</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="borrowed-book-list hide-scrollbar">
                                <c:forEach items="${borrowedBooks}" var="loan">
                                    <article class="borrowed-book-card">
                                        <div class="borrowed-book-main">
                                            <div class="book-cover" aria-hidden="true">SÁCH</div>
                                            <div class="borrowed-book-info">
                                                <h3><c:out value="${loan.book.name}"/></h3>
                                                <p><c:out value="${loan.book.author}"/> <span>·</span> Mã sách <c:out
                                                        value="${loan.book.id}"/></p>
                                                <div class="loan-dates">
                                                    <span>Ngày mượn <strong><c:out
                                                            value="${loan.borrowedOnLabel}"/></strong></span>
                                                    <span>Hạn trả <strong><c:out
                                                            value="${loan.dueOnLabel}"/></strong></span>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="borrowed-book-actions">
                                        <span class="due-badge ${loan.dueSoon ? 'due-soon' : ''} ${loan.overdue ? 'overdue' : ''}">
                                            <c:out value="${loan.dueStatusLabel}"/>
                                        </span>
                                            <form class="return-action-form"
                                                  action="${pageContext.request.contextPath}/return" method="post">
                                                <input type="hidden" name="memberId" value="${fn:escapeXml(memberId)}">
                                                <input type="hidden" name="bookId" value="${loan.book.id}">
                                                <button class="return-book-btn" type="submit">Trả sách</button>
                                            </form>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:if>

            <c:if test="${not hasSearched}">
                <div class="return-prompt">
                    <span class="empty-icon" aria-hidden="true">↗</span>
                    <h2>Tra cứu sách bạn đang mượn</h2>
                    <p>Nhập mã độc giả để xem danh sách sách và hạn trả.</p>
                </div>
            </c:if>
        </section>
        <c:if test="${not empty error}">
            <p class="msg" role="alert"><c:out value="${error}"/></p>
        </c:if>
    </div>
</main>
</body>
</html>
