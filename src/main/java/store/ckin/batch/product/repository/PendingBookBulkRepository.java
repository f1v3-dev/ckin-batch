package store.ckin.batch.product.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import store.ckin.batch.common.BatchConstants;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PendingBook을 Book 테이블로 bulk 이관하는 Repository
 * Native SQL과 JDBC를 활용한 효율적인 배치 처리
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PendingBookBulkRepository {

    private final DataSource dataSource;

    /**
     * 승인된 PendingBook들을 Book 테이블로 bulk 이관
     * LIMIT/OFFSET을 사용한 페이징 방식으로 메모리 효율적 처리
     *
     * @return 이관된 도서 수
     */
    public int migrateApprovedBooksInBatches() {
        int totalMigrated = 0;
        int offset = 0;
        boolean hasMoreData = true;

        log.info("PendingBook -> Book 이관 작업 시작 (배치 크기: {})", BatchConstants.DEFAULT_BATCH_SIZE);

        String selectSql = "SELECT id, isbn, title, description, publisher, published_date, stock, regular_price, discount_rate, sale_price " +
                "FROM PendingBook WHERE status = 'APPROVED' ORDER BY id ASC LIMIT ? OFFSET ?";

        String insertSql = "INSERT INTO Book (book_isbn, book_title, book_description, book_publisher, book_publication_date, " +
                "book_packaging, book_state, book_stock, book_regular_price, book_discount_rate, book_sale_price, " +
                "book_review_rate, modification_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String statusUpdateSql = "UPDATE PendingBook SET status = 'MIGRATED' WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement selectPstmt = connection.prepareStatement(selectSql);
             PreparedStatement bookPstmt = connection.prepareStatement(insertSql);
             PreparedStatement statusPstmt = connection.prepareStatement(statusUpdateSql)) {

            connection.setAutoCommit(false);

            while (hasMoreData) {
                try {
                    selectPstmt.setInt(1, BatchConstants.DEFAULT_BATCH_SIZE);
                    selectPstmt.setInt(2, offset);

                    List<PendingBookData> pendingBooks = new ArrayList<>();
                    try (ResultSet rs = selectPstmt.executeQuery()) {
                        while (rs.next()) {
                            pendingBooks.add(new PendingBookData(
                                    rs.getLong("id"),
                                    rs.getString("isbn"),
                                    rs.getString("title"),
                                    rs.getString("description"),
                                    rs.getString("publisher"),
                                    rs.getDate("published_date"),
                                    rs.getInt("stock"),
                                    rs.getInt("regular_price"),
                                    rs.getInt("discount_rate"),
                                    rs.getInt("sale_price")
                            ));
                        }
                    }

                    if (pendingBooks.isEmpty()) {
                        hasMoreData = false;
                        break;
                    }

                    // Book 테이블로 bulk insert
                    prepareBatchInsert(bookPstmt, pendingBooks);
                    int[] insertResults = bookPstmt.executeBatch();
                    bookPstmt.clearBatch();

                    // PendingBook 상태를 MIGRATED로 업데이트
                    updatePendingBookStatus(statusPstmt, pendingBooks);
                    statusPstmt.executeBatch();
                    statusPstmt.clearBatch();

                    // 배치별 커밋
                    connection.commit();

                    totalMigrated += insertResults.length;
                    offset += BatchConstants.DEFAULT_BATCH_SIZE;

                    log.debug("배치 처리 완료 (offset: {}): {}개 이관", offset - BatchConstants.DEFAULT_BATCH_SIZE, insertResults.length);

                    // 마지막 배치 체크
                    if (pendingBooks.size() < BatchConstants.DEFAULT_BATCH_SIZE) {
                        hasMoreData = false;
                    }

                } catch (SQLException e) {
                    log.error("배치 처리 실패 (offset: {}), 롤백: {}", offset, e.getMessage(), e);
                    connection.rollback();
                    offset += BatchConstants.DEFAULT_BATCH_SIZE; // 실패한 배치는 건너뛰고 계속
                }
            }

        } catch (SQLException e) {
            log.error("Connection 생성 실패", e);
            throw new RuntimeException("데이터베이스 연결 실패", e);
        }

        log.info("PendingBook → Book 이관 작업 완료: 총 {}개 이관", totalMigrated);
        return totalMigrated;
    }

    /**
     * PreparedStatement에 PendingBook 데이터를 배치로 추가
     */
    private void prepareBatchInsert(PreparedStatement pstmt, List<PendingBookData> pendingBooks) throws SQLException {
        Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now());

        for (PendingBookData book : pendingBooks) {
            pstmt.setString(1, book.isbn);
            pstmt.setString(2, book.title);
            pstmt.setString(3, book.description);
            pstmt.setString(4, book.publisher);
            pstmt.setDate(5, book.publishedDate);
            pstmt.setBoolean(6, false); // book_packaging 기본값
            pstmt.setString(7, "ON_SALE"); // book_state 기본값
            pstmt.setInt(8, book.stock);
            pstmt.setInt(9, book.regularPrice);
            pstmt.setInt(10, book.discountRate);
            pstmt.setInt(11, book.salePrice);
            pstmt.setString(12, "0"); // book_review_rate 기본값
            pstmt.setTimestamp(13, currentTime);

            pstmt.addBatch();
        }
    }

    /**
     * PendingBook들의 상태를 MIGRATED로 업데이트하여 중복 처리 방지
     */
    private void updatePendingBookStatus(PreparedStatement pstmt, List<PendingBookData> pendingBooks) throws SQLException {
        for (PendingBookData book : pendingBooks) {
            pstmt.setLong(1, book.id);
            pstmt.addBatch();
        }
    }

    /**
     * 네이티브 쿼리 결과를 담을 간단한 데이터 클래스
     */
    private static class PendingBookData {
        final Long id;
        final String isbn;
        final String title;
        final String description;
        final String publisher;
        final Date publishedDate;
        final Integer stock;
        final Integer regularPrice;
        final Integer discountRate;
        final Integer salePrice;

        PendingBookData(Long id, String isbn, String title, String description, String publisher,
                        Date publishedDate, Integer stock, Integer regularPrice, Integer discountRate, Integer salePrice) {
            this.id = id;
            this.isbn = isbn;
            this.title = title;
            this.description = description;
            this.publisher = publisher;
            this.publishedDate = publishedDate;
            this.stock = stock;
            this.regularPrice = regularPrice;
            this.discountRate = discountRate;
            this.salePrice = salePrice;
        }
    }
}
