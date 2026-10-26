# Cấu trúc dự án thư viện

Ứng dụng dùng JSP để hiển thị giao diện và Servlet để nhận yêu cầu HTTP.
`LibraryService` hiện dùng `JdbcLibraryRepository` để lưu dữ liệu trong SQLite.

```text
src/main/java/example/
├── config/       Cấu hình ứng dụng và kết nối cơ sở dữ liệu
├── controller/   Servlet nhận yêu cầu và chọn trang JSP
├── model/        Các đối tượng dữ liệu như Book và BorrowedBook
├── repository/   Hợp đồng và phần đọc/ghi dữ liệu
└── service/      Quy tắc nghiệp vụ mượn và trả sách

src/main/resources/db/       Schema và dữ liệu khởi tạo cho cơ sở dữ liệu
src/main/webapp/WEB-INF/views/ Các trang JSP; chỉ Servlet mới forward tới đây
src/main/webapp/css/          Giao diện CSS
```

Database được tạo ở `~/.ducanh-library/library.db` khi ứng dụng kết nối lần đầu.
`DatabaseConfig` bật khóa ngoại cho từng connection, sau đó chạy schema và dữ liệu mẫu từ `src/main/resources/db/`.
Có thể thử mượn sách bằng mã `DG001` hoặc `DG002`; mã không có trong bảng `readers` sẽ không mượn được.
