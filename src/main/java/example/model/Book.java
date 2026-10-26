package example.model;

public class Book {
    private final int id;
    private final String name;
    private final String author;
    private boolean status;
    private final int exp;

    public Book(int id, String name, String author) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.status = false;
        this.exp = 30;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public int getExp() {
        return exp;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
