package example.service;

import example.config.DatabaseConfig;
import example.repository.JdbcLibraryRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import static example.service.LibraryService.BorrowResult.BOOK_NOT_FOUND;
import static example.service.LibraryService.BorrowResult.BOOK_UNAVAILABLE;
import static example.service.LibraryService.BorrowResult.INVALID_MEMBER_ID;
import static example.service.LibraryService.BorrowResult.READER_NOT_FOUND;
import static example.service.LibraryService.BorrowResult.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryServiceTest {
    private static final int TEST_BOOK_ID = 990001;
    private static final int OTHER_TEST_BOOK_ID = 990002;
    private static final String FIRST_READER = "TEST001";
    private static final String SECOND_READER = "TEST002";

    private static LibraryService libraryService;

    @BeforeAll
    static void initializeTestDatabase() throws Exception {
        String testDatabaseDirectory = Path.of("target", "test-database")
                .toAbsolutePath()
                .normalize()
                .toString();
        System.setProperty("ducanh.library.database.directory", testDatabaseDirectory);

        libraryService = LibraryService.getInstance();
        libraryService.getBooks(); // Initializes the isolated schema and seed data.
    }

    @BeforeEach
    void prepareTestRecords() throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM loans WHERE member_id IN ('TEST001', 'TEST002') "
                    + "OR book_id IN (990001, 990002)");
            statement.executeUpdate("DELETE FROM books WHERE id IN (990001, 990002)");
            statement.executeUpdate("DELETE FROM readers WHERE member_id IN ('TEST001', 'TEST002')");
            statement.executeUpdate("INSERT INTO readers (member_id, full_name) VALUES "
                    + "('TEST001', 'Test reader one'), ('TEST002', 'Test reader two')");
            statement.executeUpdate("INSERT INTO books (id, title, author) VALUES "
                    + "(990001, 'Test book one', 'Test author'), "
                    + "(990002, 'Test book two', 'Test author')");
        }
    }

    @Test
    void borrowsAvailableBookAndSetsFourteenDayDueDate() throws SQLException {
        LocalDate today = LocalDate.now();

        assertEquals(SUCCESS, libraryService.borrowBook(" test001 ", TEST_BOOK_ID));

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT member_id, borrowed_at, due_at, returned_at
                     FROM loans WHERE book_id = ?
                     """)) {
            statement.setInt(1, TEST_BOOK_ID);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals(FIRST_READER, resultSet.getString("member_id"));
                assertEquals(today.toString(), resultSet.getString("borrowed_at"));
                assertEquals(today.plusDays(14).toString(), resultSet.getString("due_at"));
                assertNull(resultSet.getString("returned_at"));
                assertFalse(resultSet.next());
            }
        }
    }

    @Test
    void reportsUnknownReaderWithoutCreatingLoan() throws SQLException {
        assertEquals(READER_NOT_FOUND, libraryService.borrowBook("NO-SUCH-READER", TEST_BOOK_ID));
        assertEquals(0, countLoansForBook(TEST_BOOK_ID));
    }

    @Test
    void reportsUnknownBook() throws SQLException {
        assertEquals(BOOK_NOT_FOUND, libraryService.borrowBook(FIRST_READER, 999999));
        assertEquals(0, countLoansForBook(999999));
    }

    @Test
    void reportsUnavailableBookAndDoesNotCreateDuplicateLoan() throws SQLException {
        assertEquals(SUCCESS, libraryService.borrowBook(FIRST_READER, TEST_BOOK_ID));

        assertEquals(BOOK_UNAVAILABLE, libraryService.borrowBook(SECOND_READER, TEST_BOOK_ID));
        assertEquals(1, countActiveLoansForBook(TEST_BOOK_ID));
    }

    @Test
    void returnsLoanOnceAndAllowsTheBookToBeBorrowedAgain() throws SQLException {
        assertEquals(SUCCESS, libraryService.borrowBook(FIRST_READER, TEST_BOOK_ID));

        assertTrue(libraryService.returnBook(FIRST_READER, TEST_BOOK_ID));
        assertFalse(libraryService.returnBook(FIRST_READER, TEST_BOOK_ID));
        assertEquals(0, countActiveLoansForBook(TEST_BOOK_ID));
        assertEquals(SUCCESS, libraryService.borrowBook(SECOND_READER, TEST_BOOK_ID));
    }

    @Test
    void readerCannotReturnAnotherReadersLoan() throws SQLException {
        assertEquals(SUCCESS, libraryService.borrowBook(FIRST_READER, TEST_BOOK_ID));

        assertFalse(libraryService.returnBook(SECOND_READER, TEST_BOOK_ID));
        assertEquals(1, countActiveLoansForBook(TEST_BOOK_ID));
    }

    @Test
    void rejectsBlankReaderId() throws SQLException {
        assertEquals(INVALID_MEMBER_ID, libraryService.borrowBook("   ", TEST_BOOK_ID));
        assertEquals(0, countLoansForBook(TEST_BOOK_ID));
    }

    @Test
    void treatsConcurrentUniqueConflictAsUnavailable() throws SQLException {
        JdbcLibraryRepository repository = new JdbcLibraryRepository();
        LocalDate today = LocalDate.now();

        assertTrue(repository.createLoan(FIRST_READER, OTHER_TEST_BOOK_ID, today, today.plusDays(14)));
        assertFalse(repository.createLoan(SECOND_READER, OTHER_TEST_BOOK_ID, today, today.plusDays(14)));
        assertEquals(1, countActiveLoansForBook(OTHER_TEST_BOOK_ID));
    }

    private int countLoansForBook(int bookId) throws SQLException {
        return count("SELECT COUNT(*) FROM loans WHERE book_id = ?", bookId);
    }

    private int countActiveLoansForBook(int bookId) throws SQLException {
        return count("SELECT COUNT(*) FROM loans WHERE book_id = ? AND returned_at IS NULL", bookId);
    }

    private int count(String sql, int bookId) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getInt(1);
            }
        }
    }
}
