package store.ckin.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store.ckin.batch.product.service.BookMigrationService;

/**
 * 도서 이관 배치 처리를 위한 스케줄러
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookScheduler {

    private final BookMigrationService bookMigrationService;

    /**
     * 매일 새벽 2시에 승인된 PendingBook들을 Book 테이블로 이관
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void migrateApprovedBooks() {
        log.info("도서 이관 스케줄러 실행 시작");

        try {
            int migratedCount = bookMigrationService.migrateApprovedBooks();
            log.info("도서 이관 스케줄러 실행 완료: {}개 처리", migratedCount);

        } catch (Exception e) {
            log.error("도서 이관 스케줄러 실행 실패", e);
            // TODO: 실패 시 알림 로직 추가 고려
        }
    }
}
