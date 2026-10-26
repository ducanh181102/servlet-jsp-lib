package example.repository;

import example.model.Book;
import example.model.BorrowedBook;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Data-access contract used by the library service.
 */
public interface LibraryRepository {
    List<Book> findAllBooks(
    ) throws SQLException;

    List<BorrowedBook> findBorrowedBooks(String memberId) throws SQLException;

    boolean readerExists(String memberId) throws SQLException;

    boolean bookExists(int bookId) throws SQLException;

    boolean isBookAvailable(int bookId) throws SQLException;

    boolean createLoan(String memberId, int bookId, LocalDate borrowedOn, LocalDate dueOn) throws SQLException;

    boolean closeLoan(String memberId, int bookId) throws SQLException;
}
