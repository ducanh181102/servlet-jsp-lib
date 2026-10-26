package example.service;

import example.model.Book;
import example.model.BorrowedBook;
import example.repository.JdbcLibraryRepository;
import example.repository.LibraryRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Business rules for borrowing and returning library books. */
public final class LibraryService {
    public enum BorrowResult {
        SUCCESS,
        INVALID_MEMBER_ID,
        READER_NOT_FOUND,
        BOOK_NOT_FOUND,
        BOOK_UNAVAILABLE
    }

    private static final long LOAN_PERIOD_DAYS = 14;
    private static final LibraryService INSTANCE = new LibraryService(
            new JdbcLibraryRepository());

    private final LibraryRepository libraryRepository;

    private LibraryService(LibraryRepository libraryRepository) {
        this.libraryRepository = libraryRepository;
    }

    public static LibraryService getInstance() {
        return INSTANCE;
    }

    public List<Book> getBooks() throws SQLException {
        return libraryRepository.findAllBooks();
    }

    public List<BorrowedBook> getBorrowedBooks(String memberId) throws SQLException {
        return libraryRepository.findBorrowedBooks(normalizeMemberId(memberId));
    }

    public BorrowResult borrowBook(String memberId, int bookId) throws SQLException {
        String normalizedId = normalizeMemberId(memberId);
        if (normalizedId.isEmpty()) {
            return BorrowResult.INVALID_MEMBER_ID;
        }

        if (!libraryRepository.readerExists(normalizedId)) {
            return BorrowResult.READER_NOT_FOUND;
        }
        if (!libraryRepository.bookExists(bookId)) {
            return BorrowResult.BOOK_NOT_FOUND;
        }
        if (!libraryRepository.isBookAvailable(bookId)) {
            return BorrowResult.BOOK_UNAVAILABLE;
        }

        LocalDate borrowedOn = LocalDate.now();
        boolean created = libraryRepository.createLoan(
                normalizedId, bookId, borrowedOn, borrowedOn.plusDays(LOAN_PERIOD_DAYS));
        return created ? BorrowResult.SUCCESS : BorrowResult.BOOK_UNAVAILABLE;
    }

    public boolean returnBook(String memberId, int bookId) throws SQLException {
        String normalizedId = normalizeMemberId(memberId);
        return !normalizedId.isEmpty() && libraryRepository.closeLoan(normalizedId, bookId);
    }

    private String normalizeMemberId(String memberId) {
        return memberId == null ? "" : memberId.trim().toUpperCase();
    }
}
