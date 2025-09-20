package store.ckin.batch.book.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import store.ckin.batch.book.dto.PendingBookDto;
import store.ckin.batch.book.repository.PendingBookRepository;
import store.ckin.batch.common.BatchConstants;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * No Offset 방식으로 메모리 안전하게 PendingBook을 Book으로 마이그레이션하는 Tasklet
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookMigrationTasklet implements Tasklet {

    private final DataSource dataSource;
    private final PendingBookRepository pendingBookRepository;

    private static final int CHUNK_SIZE = BatchConstants.BOOK_MIGRATION_CHUNK_SIZE;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("OptimizedBookMigrationTasklet 시작 (CHUNK_SIZE: {})", CHUNK_SIZE);

        long startTime = System.currentTimeMillis();
        int totalProcessed = 0;
        List<Long> allProcessedIds = new ArrayList<>(); // 모든 처리된 ID 수집

        // check start point (min ID)
        Long minId = pendingBookRepository.findMinApprovedBookId();
        if (minId == null) {
            log.info("처리할 데이터가 없습니다.");
            return RepeatStatus.FINISHED;
        }

        log.info("처리 시작 ID: {}", minId);

        // 하나의 Connection으로 전체 작업 수행 (PreparedStatement 캐싱 효과 극대화)
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            // PreparedStatement 재사용을 위해 미리 생성
            try (PreparedStatement insertStmt = prepareInsertStatement(connection)) {

                Long lastProcessedId = 0L;  // No Offset 시작점
                boolean hasMoreData = true;
                int chunkNumber = 1;

                // No Offset 방식으로 청크 단위 처리
                while (hasMoreData) {
                    ChunkResult chunkResult = processChunk(connection, insertStmt, lastProcessedId, contribution);

                    totalProcessed += chunkResult.processedCount;
                    allProcessedIds.addAll(chunkResult.processedIds);
                    lastProcessedId = chunkResult.lastId;
                    hasMoreData = chunkResult.hasMoreData;

                    log.info("청크 {} 처리 완료 - 마지막 ID: {}, 처리 건수: {}, 누적: {} 건",
                            chunkNumber++, lastProcessedId, chunkResult.processedCount, totalProcessed);
                }

                // 모든 Book INSERT가 완료된 후 PendingBook 상태를 MIGRATED로 업데이트
                if (!allProcessedIds.isEmpty()) {
                    int updatedCount = pendingBookRepository.batchUpdateStatusToMigrated(connection, allProcessedIds);
                    log.info("전체 PendingBook 상태 업데이트 완료: {} 건", updatedCount);
                }

                connection.commit();
                log.info("전체 트랜잭션 커밋 완료");

            } catch (SQLException e) {
                connection.rollback();
                log.error("트랜잭션 롤백 실행", e);
                throw e;
            }

        } catch (SQLException e) {
            log.error("배치 마이그레이션 실패", e);
            throw new RuntimeException("배치 마이그레이션 실패", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("OptimizedBookMigrationTasklet 완료: {} 건 처리, 소요시간 {}ms",
                totalProcessed, (endTime - startTime));

        return RepeatStatus.FINISHED;
    }

    private PreparedStatement prepareInsertStatement(Connection connection) throws SQLException {
        String insertSql = "INSERT INTO Book (book_isbn, book_title, book_description, book_publisher, " +
                "book_publication_date, book_packaging, book_state, book_stock, " +
                "book_regular_price, book_discount_rate, book_sale_price, " +
                "book_review_rate, modification_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return connection.prepareStatement(insertSql);
    }

    private ChunkResult processChunk(Connection connection, PreparedStatement insertStmt,
                                     Long lastId, StepContribution contribution) throws SQLException {

        List<PendingBookDto> chunkData = pendingBookRepository.findApprovedBooksAfterId(connection, lastId, CHUNK_SIZE);

        if (chunkData.isEmpty()) {
            return new ChunkResult(0, lastId, false, new ArrayList<>());
        }

        log.debug("청크 데이터 조회 완료 - lastId: {}, 조회건수: {}", lastId, chunkData.size());

        // Book INSERT 배치 처리
        List<Long> processedIds = new ArrayList<>();
        Long maxIdInChunk = lastId;

        for (PendingBookDto pendingBook : chunkData) {
            setInsertParameters(insertStmt, pendingBook);
            insertStmt.addBatch();

            processedIds.add(pendingBook.getId());
            maxIdInChunk = Math.max(maxIdInChunk, pendingBook.getId());
        }

        // Book INSERT 실행 (rewriteBatchedStatements로 최적화)
        int[] insertResults = insertStmt.executeBatch();
        insertStmt.clearBatch();

        contribution.incrementWriteCount(insertResults.length);

        // 다음 청크가 있는지 확인
        boolean hasMoreData = chunkData.size() == CHUNK_SIZE;

        return new ChunkResult(insertResults.length, maxIdInChunk, hasMoreData, processedIds);
    }

    private void setInsertParameters(PreparedStatement pstmt, PendingBookDto pendingBook) throws SQLException {
        pstmt.setString(1, pendingBook.getIsbn());
        pstmt.setString(2, pendingBook.getTitle());
        pstmt.setString(3, pendingBook.getDescription());
        pstmt.setString(4, pendingBook.getPublisher());
        pstmt.setDate(5, new Date(pendingBook.getPublishedDate().getTime()));
        pstmt.setBoolean(6, false);
        pstmt.setString(7, "ON_SALE");
        pstmt.setInt(8, pendingBook.getStock());
        pstmt.setInt(9, pendingBook.getRegularPrice());
        pstmt.setInt(10, pendingBook.getDiscountRate());
        pstmt.setInt(11, pendingBook.getSalePrice());
        pstmt.setString(12, "0");
        pstmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
    }

    // 청크 처리 결과를 담는 클래스
    private static class ChunkResult {
        final int processedCount;
        final Long lastId;
        final boolean hasMoreData;
        final List<Long> processedIds;

        ChunkResult(int processedCount, Long lastId, boolean hasMoreData, List<Long> processedIds) {
            this.processedCount = processedCount;
            this.lastId = lastId;
            this.hasMoreData = hasMoreData;
            this.processedIds = processedIds;
        }
    }
}
