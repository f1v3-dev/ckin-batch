package store.ckin.batch.book.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store.ckin.batch.book.dto.BookDto;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * Book INSERT와 PendingBook UPDATE를 순차적으로 실행하는 CompositeItemWriter 설정
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BookMigrationWriterConfig {

    private final DataSource dataSource;

    /**
     * Book 테이블 INSERT용 Writer Bean
     */
    @Bean
    @StepScope
    public BookInsertItemWriter bookInsertItemWriter() {
        return new BookInsertItemWriter(dataSource);
    }

    /**
     * PendingBook 상태 UPDATE용 Writer Bean
     */
    @Bean
    @StepScope
    public PendingBookStatusUpdateWriter pendingBookStatusUpdateWriter() {
        return new PendingBookStatusUpdateWriter(dataSource);
    }

    /**
     * CompositeItemWriter를 사용하여 두 Writer를 순차적으로 실행
     * 1. Book 테이블에 INSERT
     * 2. PendingBook 상태를 MIGRATED로 UPDATE
     */
    @Bean
    public CompositeItemWriter<BookDto> compositeBookMigrationWriter(
            BookInsertItemWriter bookInsertItemWriter,
            PendingBookStatusUpdateWriter pendingBookStatusUpdateWriter
    ) {
        CompositeItemWriter<BookDto> compositeWriter = new CompositeItemWriter<>();

        compositeWriter.setDelegates(Arrays.asList(
                bookInsertItemWriter,
                pendingBookStatusUpdateWriter
        ));

        return compositeWriter;
    }
}
