package store.ckin.batch.book.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store.ckin.batch.book.tasklet.BookMigrationTasklet;

@Configuration
@RequiredArgsConstructor
public class BookMigrationJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job bookMigrationJob(Step bookMigrationStep) {
        return jobBuilderFactory.get("bookMigrationJob")
                .start(bookMigrationStep)
                .build();
    }

    @Bean
    public Step bookMigrationStep(BookMigrationTasklet bookMigrationTasklet) {
        return stepBuilderFactory.get("bookMigrationStep")
                .tasklet(bookMigrationTasklet)
                .build();
    }
}
