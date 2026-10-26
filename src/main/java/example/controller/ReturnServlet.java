package example.controller;

import example.model.BorrowedBook;
import example.service.LibraryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/return")
public class ReturnServlet extends HttpServlet {
    private final LibraryService libraryService = LibraryService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String memberId = req.getParameter("memberId");
        if (memberId != null && memberId.isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập mã độc giả.");
            req.getRequestDispatcher("/WEB-INF/views/my-books.jsp").forward(req, resp);
            return;
        }
        memberId = memberId == null ? "" : memberId.trim();
        req.setAttribute("memberId", memberId);
        req.setAttribute("hasSearched", !memberId.isBlank());
        List<BorrowedBook> borrowedBooks = null;
        try {
            borrowedBooks = libraryService.getBorrowedBooks(memberId);
        } catch (SQLException exception) {
            getServletContext().log("Không tải được danh sách sách đang mượn từ database.", exception);
            req.setAttribute("error", "Không tải được dữ liệu mượn trả. Kiểm tra cấu hình SQLite và log của Tomcat.");
            borrowedBooks = List.of();
        }
        long dueSoonCount = borrowedBooks.stream().filter(BorrowedBook::isDueSoon).count();
        req.setAttribute("borrowedBooks", borrowedBooks);
        req.setAttribute("dueSoonCount", dueSoonCount);
        req.setAttribute("result", req.getParameter("result"));

        req.getRequestDispatcher("/WEB-INF/views/my-books.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String memberId = req.getParameter("memberId");
        memberId = memberId == null ? "" : memberId.trim();

        String result = "error";
        String bookIdValue = req.getParameter("bookId");
        if (!memberId.isBlank() && bookIdValue != null) {
            try {
                if (libraryService.returnBook(memberId, Integer.parseInt(bookIdValue))) {
                    result = "returned";
                }
            } catch (NumberFormatException exception) {
                // Keep the error result for an invalid book id.
            } catch (SQLException exception) {
                getServletContext().log("Không cập nhật được lượt trả sách.", exception);
            }
        }

        String encodedMemberId = URLEncoder.encode(memberId, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/return?memberId=" + encodedMemberId + "&result=" + result);
    }
}
