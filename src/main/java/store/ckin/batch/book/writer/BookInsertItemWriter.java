package store.ckin.batch.book.writer;

import org.springframework.batch.item.database.JdbcBatchItemWriter;
import store.ckin.batch.book.dto.BookDto;

import javax.sql.DataSource;

/**
 * Book 테이블에만 INSERT하는 전용 Writer
 *
 * @author Seungjo, Jeong
 */
public class BookInsertItemWriter extends JdbcBatchItemWriter<BookDto> {

    private static final String INSERT_BOOK_SQL =
            "INSERT INTO Book (book_isbn, book_title, book_description, book_publisher, book_publication_date, " +
                    "book_packaging, book_state, book_stock, book_regular_price, book_discount_rate, book_sale_price, " +
                    "book_review_rate, modification_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public BookInsertItemWriter(DataSource dataSource) {
        setDataSource(dataSource);
        setSql(INSERT_BOOK_SQL);

        setItemPreparedStatementSetter((book, ps) -> {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getDescription());
            ps.setString(4, book.getPublisher());
            ps.setDate(5, new java.sql.Date(book.getPublicationDate().getTime()));
            ps.setBoolean(6, book.getPackaging());
            ps.setString(7, book.getState());
            ps.setInt(8, book.getStock());
            ps.setInt(9, book.getRegularPrice());
            ps.setInt(10, book.getDiscountRate());
            ps.setInt(11, book.getSalePrice());
            ps.setString(12, book.getReviewRate());
            ps.setTimestamp(13, book.getModificationTime());
        });


        setAssertUpdates(false);
    }
}
