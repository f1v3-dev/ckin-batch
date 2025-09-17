package store.ckin.batch.book.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.ckin.batch.book.repository.PendingBookBulkRepository;

/**
 * 도서 이관 배치 처리를 위한 코어 서비스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookMigrationService {

    private final PendingBookBulkRepository pendingBookBulkRepository;

    /**
     * 승인된 PendingBook들을 Book 테이블로 이관하는 메��� 메서드
     *
     * @return 이관된 도서 수
     */
    public int migrateApprovedBooks() {
        log.info("=== 도서 이관 배치 작업 시작 ===");

        try {
            int migratedCount = pendingBookBulkRepository.migrateApprovedBooksInBatches();
            log.info("=== 도서 이관 배치 작업 완료: {}개 이관 ===", migratedCount);
            return migratedCount;

        } catch (Exception e) {
            log.error("도서 이관 배치 작업 실패", e);
            throw new RuntimeException("도서 이관 배치 작업 실패", e);
        }
    }
}
