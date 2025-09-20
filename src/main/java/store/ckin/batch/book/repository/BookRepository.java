package store.ckin.batch.book.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import store.ckin.batch.book.dto.BookDto;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Book 테이블 접근을 위한 Repository
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final DataSource dataSource;

    /**
     * Book 테이블에 배치 INSERT (DataSource + PreparedStatement + Batch 사용)
     */
    public int[] batchInsertBooks(List<BookDto> books) throws SQLException {
        String sql = "INSERT INTO Book (book_isbn, book_title, book_description, book_publisher, book_publication_date, " +
                "book_packaging, book_state, book_stock, book_regular_price, book_discount_rate, book_sale_price, " +
                "book_review_rate, modification_time, pending_book_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            log.info("Book 배치 INSERT PreparedStatement 생성 - 건수: {}", books.size());

            for (BookDto book : books) {
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
                ps.setLong(14, book.getPendingBookId());

                ps.addBatch(); // 배치에 추가
            }

            int[] results = ps.executeBatch(); // 배치 실행
            log.info("Book 배치 INSERT 완료");
            return results;
        }
    }
}
