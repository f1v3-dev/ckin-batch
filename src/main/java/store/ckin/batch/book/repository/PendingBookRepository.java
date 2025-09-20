package store.ckin.batch.book.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import store.ckin.batch.book.dto.PendingBookDto;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PendingBook 테이블 접근을 위한 Repository
 * 모든 메서드가 동일한 Connection을 사용하여 트랜잭션 일관성 보장
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PendingBookRepository {

    private final DataSource dataSource;

    /**
     * No Offset 방식으로 APPROVED 상태의 PendingBook 조회
     * 동일한 트랜잭션 내에서 실행하기 위해 Connection을 파라미터로 받음
     */
    public List<PendingBookDto> findApprovedBooksAfterId(Connection connection, Long lastId, int chunkSize) throws SQLException {
        String sql = "SELECT id, isbn, title, description, publisher, published_date, " +
                "stock, regular_price, discount_rate, sale_price " +
                "FROM PendingBook " +
                "WHERE status = 'APPROVED' AND id > ? " +
                "ORDER BY id ASC LIMIT ?";

        List<PendingBookDto> results = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, lastId != null ? lastId : 0L);
            ps.setInt(2, chunkSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PendingBookDto dto = PendingBookDto.builder()
                            .id(rs.getLong("id"))
                            .isbn(rs.getString("isbn"))
                            .title(rs.getString("title"))
                            .description(rs.getString("description"))
                            .publisher(rs.getString("publisher"))
                            .publishedDate(rs.getDate("published_date"))
                            .stock(rs.getInt("stock"))
                            .regularPrice(rs.getInt("regular_price"))
                            .discountRate(rs.getInt("discount_rate"))
                            .salePrice(rs.getInt("sale_price"))
                            .build();
                    results.add(dto);
                }
            }
        }

        log.debug("PendingBook No Offset 조회 완료 - lastId: {}, chunkSize: {}, 조회건수: {}",
                lastId, chunkSize, results.size());
        return results;
    }

    /**
     * 처리된 PendingBook들의 상태를 MIGRATED로 일괄 업데이트
     * 동일한 트랜잭션 내에서 실행하기 위해 Connection을 파라미터로 받음
     */
    public int batchUpdateStatusToMigrated(Connection connection, List<Long> processedIds) throws SQLException {
        String sql = "UPDATE PendingBook SET status = 'MIGRATED', updated_at = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            log.info("PendingBook 상태 MIGRATED 업데이트 시작 - 건수: {}", processedIds.size());

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            for (Long id : processedIds) {
                ps.setTimestamp(1, now);
                ps.setLong(2, id);
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            log.info("PendingBook 상태 MIGRATED 업데이트 완료 - 처리건수: {}", results.length);

            return results.length;
        }
    }

    /**
     * 최소 ID 조회 (No Offset 시작점 확인용)
     * 이 메서드는 시작 전 한 번만 호출되므로 별도 Connection 사용
     */
    public Long findMinApprovedBookId() throws SQLException {
        String sql = "SELECT MIN(id) FROM PendingBook WHERE status = 'APPROVED'";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return null;
        }
    }
}
