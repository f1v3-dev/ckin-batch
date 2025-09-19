package store.ckin.batch.book.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BookCreateProcessor 설정
 *
 * @author Seungjo, Jeong
 */
@Configuration
@RequiredArgsConstructor
public class BookMigrationProcessorConfig {

    @Bean
    @StepScope
    public BookCreateProcessor bookCreateProcessor() {
        return new BookCreateProcessor();
    }
}
