# Ứng dụng mượn và trả sách

Ứng dụng web Java cho thư viện, dùng Jakarta Servlet nhận yêu cầu và JSP hiển thị giao diện. Dữ liệu sách, độc giả và lượt mượn được lưu bằng SQLite.

## Chức năng

- Xem danh sách sách và tình trạng còn sách hoặc đang được mượn.
- Mượn một cuốn còn sẵn bằng mã độc giả; thời hạn mượn là 14 ngày.
- Tra cứu sách đang mượn, hạn trả, sách sắp đến hạn hoặc đã quá hạn.
- Trả sách và cập nhật trạng thái lượt mượn.
- Kiểm tra mã độc giả, sách tồn tại và không cho mượn cuốn đang có lượt mượn chưa trả.

## Công nghệ

- Java 21 và Maven
- Jakarta Servlet 6.1, JSP và JSTL
- SQLite JDBC
- JUnit Jupiter

## Yêu cầu

- JDK 21
- Maven
- Servlet container hỗ trợ Jakarta Servlet 6.1, ví dụ Tomcat 11

## Build và kiểm thử

```bash
mvn test
mvn clean package
```

File WAR được tạo tại:

```text
target/servlet-jsp-lib-1.0-SNAPSHOT.war
```

## Chạy trên Tomcat

Chép file WAR vào thư mục `webapps` của Tomcat rồi khởi động server:

```bash
cp target/servlet-jsp-lib-1.0-SNAPSHOT.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/catalina.sh" run
```

Sau khi Tomcat khởi động, mở các trang:

- Mượn sách: `http://localhost:8080/servlet-jsp-lib/borrow`
- Tra cứu hoặc trả sách: `http://localhost:8080/servlet-jsp-lib/return`

Có thể thử với mã độc giả mẫu `DG001` hoặc `DG002`. Dùng đúng context path nếu đổi tên file WAR.

## Cơ sở dữ liệu

- Database SQLite được tạo lần đầu tại `~/.ducanh-library/library.db`.
- Schema và dữ liệu mẫu được nạp từ `src/main/resources/db/schema.sql` và `src/main/resources/db/seed.sql`.
- Có thể đổi thư mục database bằng JVM property `ducanh.library.database.directory`.
- Khi chạy test, Maven dùng database riêng trong `target/test-database`.

## Cấu trúc chính

```text
src/main/java/example/
├── config/       Cấu hình SQLite và khởi tạo schema
├── controller/   BorrowServlet và ReturnServlet
├── model/        Book và BorrowedBook
├── repository/   Truy vấn SQLite bằng JDBC
└── service/      Quy tắc nghiệp vụ mượn và trả sách

src/main/resources/db/        Schema và dữ liệu mẫu
src/main/webapp/WEB-INF/views/ Các trang JSP
src/main/webapp/css/          Giao diện CSS
```
