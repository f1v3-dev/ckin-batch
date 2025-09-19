package store.ckin.batch.book.writer;

import org.springframework.batch.item.database.JdbcBatchItemWriter;
import store.ckin.batch.book.dto.BookDto;

import javax.sql.DataSource;

/**
 * PendingBook 상태를 MIGRATED로 업데이트하는 전용 Writer
 *
 * @author Seungjo, Jeong
 */
public class PendingBookStatusUpdateWriter extends JdbcBatchItemWriter<BookDto> {

    private static final String UPDATE_PENDING_BOOK_SQL =
            "UPDATE PendingBook SET status = 'MIGRATED' WHERE id = ?";

    public PendingBookStatusUpdateWriter(DataSource dataSource) {
        setDataSource(dataSource);
        setSql(UPDATE_PENDING_BOOK_SQL);

        setItemPreparedStatementSetter((book, ps) -> {
            ps.setLong(1, book.getPendingBookId());
        });

        setAssertUpdates(false);
    }
}
