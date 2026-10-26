CREATE TABLE IF NOT EXISTS readers (
                                       member_id TEXT PRIMARY KEY,
                                       full_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS books (
                                     id INTEGER PRIMARY KEY,
                                     title TEXT NOT NULL,
                                     author TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS loans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    member_id TEXT NOT NULL REFERENCES readers(member_id),
    book_id INTEGER NOT NULL REFERENCES books(id),
    borrowed_at TEXT NOT NULL,
    due_at TEXT NOT NULL,
    returned_at TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_loans_one_active_loan_per_book
    ON loans(book_id)
    WHERE returned_at IS NULL;
