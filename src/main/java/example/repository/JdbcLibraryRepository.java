package example.repository;

import example.config.DatabaseConfig;
import example.model.Book;
import example.model.BorrowedBook;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-backed implementation of the library data-access contract.
 */
public class JdbcLibraryRepository implements LibraryRepository {
    @Override
    public List<Book> findAllBooks() throws SQLException {
        String sql = """
                SELECT b.id, b.title, b.author,
                       EXISTS (
                           SELECT 1
                           FROM loans l
                           WHERE l.book_id = b.id AND l.returned_at IS NULL
                       ) AS borrowed
                FROM books b
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            List<Book> books = new ArrayList<>();
            while (resultSet.next()) {
                Book book = new Book(resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("author")
                );
                book.setStatus(resultSet.getInt("borrowed") != 0);
                books.add(book);
            }
            return books;
        }
    }

    @Override
    public List<BorrowedBook> findBorrowedBooks(String memberId) throws SQLException {
        String sql = """
                SELECT b.id, title, author, l.borrowed_at, l.due_at
                FROM loans l
                INNER JOIN books b
                ON l.book_id = b.id AND l.returned_at IS NULL
                WHERE l.member_id = (?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<BorrowedBook> borrowedBooks = new ArrayList<>();

                while (resultSet.next()) {
                    Book book = new Book(resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("author"));
                    LocalDate borrowed_at = LocalDate.parse(resultSet.getString("borrowed_at"));
                    LocalDate due_at = LocalDate.parse(resultSet.getString("due_at"));
                    BorrowedBook borrowedBook = new BorrowedBook(book,
                            borrowed_at,
                            due_at
                    );
                    borrowedBooks.add(borrowedBook);
                }

                return borrowedBooks;
            }
        }
    }

    @Override
    public boolean readerExists(String memberId) throws SQLException {
        String sql = "SELECT 1 FROM readers WHERE member_id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean bookExists(int bookId) throws SQLException {
        String sql = "SELECT 1 FROM books WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean isBookAvailable(int bookId) throws SQLException {
        String sql = """
                SELECT 1
                FROM books b
                WHERE b.id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM loans l
                      WHERE l.book_id = b.id AND l.returned_at IS NULL
                  )
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean createLoan(String memberId, int bookId, LocalDate borrowedOn, LocalDate dueOn) throws SQLException {
        String sql = """
                INSERT INTO loans (member_id, book_id, borrowed_at, due_at)
                VALUES ((?), (?), (?), (?));
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, memberId);
            statement.setInt(2, bookId);
            statement.setString(3, borrowedOn.toString());
            statement.setString(4, dueOn.toString());

            try {
                return statement.executeUpdate() == 1;
            } catch (SQLiteException exception) {
                if (exception.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                    return false;
                }
                throw exception;
            }
        }
    }

    @Override
    public boolean closeLoan(String memberId, int bookId) throws SQLException {
        String sql = """
                UPDATE loans SET returned_at = (?) WHERE member_id = (?) AND book_id=(?) AND returned_at IS NULL
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String today = LocalDate.now().toString();
            statement.setString(1, today);
            statement.setString(2, memberId);
            statement.setInt(3, bookId);
            return statement.executeUpdate() == 1;
        }
    }
}
