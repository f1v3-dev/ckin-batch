package store.ckin.batch.book.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * PendingBook 데이터를 담는 DTO
 *
 * @author Seungjo, Jeong
 */
@Getter
@Builder
public class PendingBookDto {

    private final Long id;
    private final String isbn;
    private final String title;
    private final String description;
    private final String publisher;
    private final Date publishedDate;
    private final Integer stock;
    private final Integer regularPrice;
    private final Integer discountRate;
    private final Integer salePrice;
}
