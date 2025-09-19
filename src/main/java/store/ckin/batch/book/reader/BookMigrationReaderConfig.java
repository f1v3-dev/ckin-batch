package store.ckin.batch.book.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * PendingBookItemReader 설정
 *
 * @author Seungjo, Jeong
 */
@Configuration
@RequiredArgsConstructor
public class BookMigrationReaderConfig {

    private final DataSource dataSource;

    @Bean
    @StepScope
    public PendingBookItemReader pendingBookItemReader() {
        return new PendingBookItemReader(dataSource);
    }
}
