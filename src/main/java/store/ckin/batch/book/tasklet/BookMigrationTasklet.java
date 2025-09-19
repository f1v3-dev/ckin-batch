package store.ckin.batch.book.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import store.ckin.batch.book.service.BookMigrationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookMigrationTasklet implements Tasklet {

    private final BookMigrationService bookMigrationService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("=== Book Migration Tasklet Start ===");

        try {
            int migratedCount = bookMigrationService.migrateApprovedBooks();

            stepContribution.getStepExecution().getJobExecution()
                    .getExecutionContext().putInt("migratedCount", migratedCount);

            log.info("=== Book Migration Tasklet Completed: {} books migrated ===", migratedCount);
            return RepeatStatus.FINISHED;

        } catch (Exception e) {
            log.error("Book Migration Tasklet Failed", e);
            throw e;
        }
    }
}
