package store.ckin.batch.book.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store.ckin.batch.book.tasklet.BookMigrationTasklet;

/**
 * Tasklet 방식의 Book Migration Job
 * 단일 트랜잭션으로 처리하여 PreparedStatement 캐시 효과를 확인
 *
 * @author Seungjo, Jeong
 */
@Configuration
@RequiredArgsConstructor
public class TaskletBookMigrationJob {

    /**
     * Tasklet 방식의 Job
     */
    @Bean
    public Job taskletPendingBookMigrationJob(JobRepository jobRepository, Step taskletBookMigrationStep) {
        return new JobBuilder("taskletPendingBookMigrationJob")
                .repository(jobRepository)
                .start(taskletBookMigrationStep)
                .build();
    }

    /**
     * Tasklet 방식의 Step
     */
    @Bean
    public Step taskletBookMigrationStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         BookMigrationTasklet bookMigrationTasklet) {
        return new StepBuilder("taskletBookMigrationStep")
                .repository(jobRepository)
                .tasklet(bookMigrationTasklet)
                .transactionManager(transactionManager)
                .build();
    }
}
