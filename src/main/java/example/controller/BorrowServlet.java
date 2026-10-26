package example.controller;

import example.service.LibraryService;
import example.service.LibraryService.BorrowResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sqlite.SQLiteErrorCode;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/borrow")
public class BorrowServlet extends HttpServlet {
    private final LibraryService libraryService = LibraryService.getInstance();
    private static final String titlePageInfo = "Nhập mã độc giả và chọn một cuốn sách còn sẵn.";
    private static final String memberIdLabel = "Mã độc giả";
    private static final String selectBookLabel = "Chọn sách muốn mượn";
    private static final String inputInfoLabel = "Mã được cấp khi bạn đăng ký thẻ thư viện.";
    private static final String bookIdLabel = "Mã sách";
    private static final String borrowedBookLabel = "Đang được mượn";
    private static final String availableBookLabel = "Còn sách";
    private static final String borrowBookLabel = "Gửi yêu cầu mượn";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            setPageAttributes(req);
        } catch (SQLException e) {
            getServletContext().log("Không tải được danh sách sách từ database.", e);
            req.setAttribute("error", "Không tải được dữ liệu sách. Kiểm tra cấu hình SQLite và log của Tomcat.");
        }
        req.getRequestDispatcher("/WEB-INF/views/books.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String memberId = req.getParameter("memberId");
        memberId = memberId == null ? "" : memberId.trim();
        String selectedBookIdValue = req.getParameter("bookCard");
        Integer selectedBookId = null;
        boolean invalidBookId = false;

        if (selectedBookIdValue != null && !selectedBookIdValue.isBlank()) {
            try {
                selectedBookId = Integer.valueOf(selectedBookIdValue);
            } catch (NumberFormatException exception) {
                invalidBookId = true;
            }
        }

        if (memberId.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập mã độc giả.");
        } else if (selectedBookIdValue == null || selectedBookIdValue.isBlank()) {
            req.setAttribute("error", "Vui lòng chọn sách.");
        } else if (invalidBookId) {
            req.setAttribute("error", "Mã sách không hợp lệ.");
        } else {
            try {
                BorrowResult result = libraryService.borrowBook(memberId, selectedBookId);
                switch (result) {
                    case SUCCESS -> req.setAttribute("message", "Mượn sách thành công. Hạn trả sau 14 ngày.");
                    case INVALID_MEMBER_ID -> req.setAttribute("error", "Vui lòng nhập mã độc giả.");
                    case READER_NOT_FOUND -> req.setAttribute("error", "Không tìm thấy mã độc giả. Vui lòng kiểm tra lại.");
                    case BOOK_NOT_FOUND -> req.setAttribute("error", "Không tìm thấy sách. Vui lòng tải lại trang và chọn sách khác.");
                    case BOOK_UNAVAILABLE -> req.setAttribute("error", "Sách này hiện đang được mượn. Vui lòng chọn cuốn khác.");
                }
            } catch (SQLException exception) {
                getServletContext().log("Không lưu được yêu cầu mượn sách.", exception);
                req.setAttribute("error", "Không ghi được yêu cầu mượn vào database. Kiểm tra log của Tomcat.");
            }
        }

        req.setAttribute("selectedBookId", selectedBookId);

//        org.sqlite.SQLiteException: [SQLITE_CONSTRAINT_UNIQUE] A UNIQUE constraint failed (UNIQUE constraint failed: loans.book_id)
//        org.sqlite.SQLiteException: [SQLITE_CONSTRAINT_FOREIGNKEY] A foreign key constraint failed (FOREIGN KEY constraint failed)
        try {
            setPageAttributes(req);
        } catch (SQLException exception) {
            getServletContext().log("Không tải lại được danh sách sách từ database.", exception);
            req.setAttribute("error", "Không tải được dữ liệu sách. Kiểm tra cấu hình SQLite và log của Tomcat.");
        }
        req.getRequestDispatcher("/WEB-INF/views/books.jsp").forward(req, resp);
    }

    private void setPageAttributes(HttpServletRequest req) throws SQLException {
        req.setAttribute("titlePageInfo", titlePageInfo);
        req.setAttribute("memberIdLabel", memberIdLabel);
        req.setAttribute("selectBookLabel", selectBookLabel);
        req.setAttribute("bookIdLabel", bookIdLabel);
        req.setAttribute("borrowBookLabel", borrowBookLabel);
        req.setAttribute("inputInfoLabel", inputInfoLabel);
        req.setAttribute("borrowedBookLabel", borrowedBookLabel);
        req.setAttribute("availableBookLabel", availableBookLabel);
        req.setAttribute("books", libraryService.getBooks());
    }
}
