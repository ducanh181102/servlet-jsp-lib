package example.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class BorrowedBook {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Book book;
    private final LocalDate borrowedOn;
    private final LocalDate dueOn;

    public BorrowedBook(Book book, LocalDate borrowedOn, LocalDate dueOn) {
        this.book = book;
        this.borrowedOn = borrowedOn;
        this.dueOn = dueOn;
    }

    public Book getBook() {
        return book;
    }

    public String getBorrowedOnLabel() {
        return borrowedOn.format(DATE_FORMAT);
    }

    public String getDueOnLabel() {
        return dueOn.format(DATE_FORMAT);
    }

    public long getDaysRemaining() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueOn);
    }

    public String getDueStatusLabel() {
        long days = getDaysRemaining();
        if (days < 0) {
            return "Quá hạn " + Math.abs(days) + " ngày";
        }
        if (days == 0) {
            return "Đến hạn hôm nay";
        }
        return "Còn " + days + " ngày";
    }

    public boolean isDueSoon() {
        long days = getDaysRemaining();
        return days >= 0 && days <= 4;
    }

    public boolean isOverdue() {
        return getDaysRemaining() < 0;
    }
}
