package store.ckin.batch.book.reader;

import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import store.ckin.batch.book.dto.PendingBookDto;
import store.ckin.batch.common.BatchConstants;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * APPROVED 상태의 PendingBook을 청크 단위로 읽어오는 ItemReader
 *
 * @author Seungjo, Jeong
 */
public class PendingBookItemReader extends JdbcPagingItemReader<PendingBookDto> {

    public PendingBookItemReader(DataSource dataSource) {
        setName("pendingBookItemReader");
        setDataSource(dataSource);
        setPageSize(BatchConstants.DEFAULT_CHUNK_SIZE);
        setRowMapper(this::mapRow);


        // MySQL용 쿼리 프로바이더 설정
        MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
        provider.setSelectClause("SELECT id, isbn, title, description, publisher, published_date, stock, regular_price, discount_rate, sale_price");
        provider.setFromClause("FROM PendingBook");
        provider.setWhereClause("WHERE status = 'APPROVED'");

        // 정렬 기준 설정
        provider.setSortKeys(orderById());
        setQueryProvider(provider);
    }

    private Map<String, Order> orderById() {
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        return sortKeys;
    }

    /**
     * ResultSet을 PendingBookDto로 매핑
     */
    private PendingBookDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PendingBookDto.builder()
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
    }
}
