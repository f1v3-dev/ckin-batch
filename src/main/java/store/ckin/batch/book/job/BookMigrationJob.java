package store.ckin.batch.book.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store.ckin.batch.book.dto.BookDto;
import store.ckin.batch.book.dto.PendingBookDto;
import store.ckin.batch.book.processor.BookCreateProcessor;
import store.ckin.batch.book.reader.PendingBookItemReader;
import store.ckin.batch.common.BatchConstants;

@Configuration
@RequiredArgsConstructor
public class BookMigrationJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    /**
     * 새로운 Chunk-oriented 방식의 Job (CompositeItemWriter 사용)
     */
    @Bean
    public Job pendingBookMigrationJob(Step bookMigrationChunkStep) {
        return jobBuilderFactory.get("pendingBookMigrationJob")
                .start(bookMigrationChunkStep)
                .build();
    }

    /**
     * 새로운 Chunk-oriented Step (CompositeItemWriter 사용)
     */
    @Bean
    public Step bookMigrationChunkStep(
            PendingBookItemReader reader,
            BookCreateProcessor processor,
            CompositeItemWriter<BookDto> compositeBookMigrationWriter) {

        return stepBuilderFactory.get("bookMigrationChunkStep")
                .<PendingBookDto, BookDto>chunk(BatchConstants.BOOK_MIGRATION_CHUNK_SIZE)
                .reader(reader)
                .processor(processor)
                .writer(compositeBookMigrationWriter)
                .build();
    }
}
