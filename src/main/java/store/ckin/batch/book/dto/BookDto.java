package store.ckin.batch.book.dto;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Book 테이블에 저장될 데이터를 담는 DTO
 *
 * @author Seungjo, Jeong
 */
@Getter
@Builder
public class BookDto {

    private final Long pendingBookId; // 상태 업데이트용
    private final String isbn;
    private final String title;
    private final String description;
    private final String publisher;
    private final Date publicationDate;
    private final Boolean packaging;
    private final String state;
    private final Integer stock;
    private final Integer regularPrice;
    private final Integer discountRate;
    private final Integer salePrice;
    private final String reviewRate;
    private final Timestamp modificationTime;
}
