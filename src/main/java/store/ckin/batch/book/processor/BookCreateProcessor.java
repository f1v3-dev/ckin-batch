package store.ckin.batch.book.processor;

import org.springframework.batch.item.ItemProcessor;
import store.ckin.batch.book.dto.BookDto;
import store.ckin.batch.book.dto.PendingBookDto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * PendingBookDto를 BookDto로 변환하는 ItemProcessor
 *
 * @author Seungjo, Jeong
 */
public class BookCreateProcessor implements ItemProcessor<PendingBookDto, BookDto> {

    @Override
    public BookDto process(PendingBookDto pendingBook) throws Exception {
        return BookDto.builder()
                .pendingBookId(pendingBook.getId()) // 상태 업데이트용 ID 보존
                .isbn(pendingBook.getIsbn())
                .title(pendingBook.getTitle())
                .description(pendingBook.getDescription())
                .publisher(pendingBook.getPublisher())
                .publicationDate(pendingBook.getPublishedDate())
                .packaging(false) // 기본값
                .state("ON_SALE") // 기본값
                .stock(pendingBook.getStock())
                .regularPrice(pendingBook.getRegularPrice())
                .discountRate(pendingBook.getDiscountRate())
                .salePrice(pendingBook.getSalePrice())
                .reviewRate("0") // 기본값
                .modificationTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();
    }
}
