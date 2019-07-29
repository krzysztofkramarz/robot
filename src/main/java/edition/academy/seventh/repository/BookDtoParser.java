package edition.academy.seventh.repository;

import edition.academy.seventh.database.model.BookDto;
import edition.academy.seventh.database.model.BookstoreBookDto;
import edition.academy.seventh.database.model.PriceAtTheMomentDto;
import edition.academy.seventh.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses {@link BookDto} into database model and save or update changes, and vice versa.
 *
 * @author Agnieszka Trzewik
 */
@Service
class BookDtoParser {

  private BookRepository bookRepository;

  @Autowired
  @Lazy
  BookDtoParser(BookRepository repository) {
    this.bookRepository = repository;
  }

  void setRepository(BookRepository repository) {
    this.bookRepository = repository;
  }

  /**
   * Parses given {@link BookstoreBook} into a DTO.
   *
   * @param bookstoreBook to parse
   * @return {@link BookstoreBookDto } with price history
   */
  BookstoreBookDto parseBookstoreBookIntoBookstoreBookDto(BookstoreBook bookstoreBook) {

    List<PriceAtTheMoment> priceHistories = bookstoreBook.getPriceHistories();
    List<PriceAtTheMomentDto> priceAtTheMomentDtos =
        priceHistories.stream()
            .map(
                priceHistory ->
                    new PriceAtTheMomentDto(
                        priceHistory.getRetailPrice(),
                        priceHistory.getPromotionalPrice(),
                        priceHistory.getCurrency(),
                        priceHistory.getDate()))
            .collect(Collectors.toList());
    return new BookstoreBookDto(
        bookstoreBook.getBook().getBookId().getTitle(),
        bookstoreBook.getBook().getSubtitle(),
        bookstoreBook.getBook().getBookId().getAuthor(),
        bookstoreBook.getImageLink(),
        bookstoreBook.getHyperlink(),
        bookstoreBook.getBookstore().getName(),
        priceAtTheMomentDtos);
  }

  /**
   * Creates all required records. Tries to find specific ones in a database, if they do exist
   * updates them, otherwise creates new ones.
   *
   * @param bookDto to be parsed into model.
   */
  void parseBookDtoIntoModel(BookDto bookDto) {
    Book book = createBook(bookDto);
    Bookstore bookstore = createBookstore(bookDto);
    BookstoreBook bookstoreBook = createBookstoreBook(bookDto, book, bookstore);
    PriceAtTheMoment priceAtTheMoment = createPriceAtTheMoment(bookDto, bookstoreBook);
    bookstoreBook.getPriceHistories().add(priceAtTheMoment);

    EntitiesInDatabase entitiesInDatabase = findEntitiesInDatabase(bookstoreBook);

    saveOrUpdateModel(bookstore, bookstoreBook, book, entitiesInDatabase);
  }

  /**
   * Creates new instance of metadata {@link EntitiesInDatabase} that holds information about {@link
   * BookstoreBook}, {@link Book}, {@link Bookstore}.
   *
   * @param bookstoreBook based on it, BookstoreBook entity values are searched in database.
   * @return {@link EntitiesInDatabase} representing all values found in database.
   */
  private EntitiesInDatabase findEntitiesInDatabase(BookstoreBook bookstoreBook) {

    Book bookAlreadyInDatabase = bookRepository.getBookById(bookstoreBook.getBook().getBookId());

    Bookstore bookstoreAlreadyInDatabase =
        bookRepository.getBookstoreById(bookstoreBook.getBookstore().getName());

    BookstoreBook bookstoreBookAlreadyInDatabase =
        bookRepository.getBookstoreBookById(bookstoreBook.getHyperlink());

    return new EntitiesInDatabase(
        bookAlreadyInDatabase, bookstoreAlreadyInDatabase, bookstoreBookAlreadyInDatabase);
  }

  private PriceAtTheMoment createPriceAtTheMoment(BookDto bookDto, BookstoreBook bookstoreBook) {

    String currency = findCurrency(String.valueOf(bookDto.getRetailPrice()));
    BigDecimal retailPrice =
        establishRetailPrice(bookDto.getRetailPrice(), bookDto.getPromotionalPrice());
    BigDecimal promotionalPrice =
        establishPromotionalPrice(retailPrice, bookDto.getPromotionalPrice());

    return new PriceAtTheMoment(
        bookstoreBook, retailPrice, promotionalPrice, currency, LocalDateTime.now());
  }

  private BigDecimal parseStringPriceIntoBigDecimal(String price) {
    price = price.replace(",", ".");
    price = price.replaceAll("[^0-9.]", "");
    return new BigDecimal(price);
  }

  private BookstoreBook createBookstoreBook(BookDto bookDto, Book book, Bookstore bookstore) {
    return new BookstoreBook(bookDto.getHref(), bookDto.getImageLink(), bookstore, book);
  }

  private Bookstore createBookstore(BookDto bookDto) {
    return new Bookstore(bookDto.getBookstore());
  }

  private Book createBook(BookDto bookDto) {
    BookId bookId = new BookId(bookDto.getTitle(), bookDto.getAuthors());
    return new Book(bookId, bookDto.getSubtitle());
  }

  private void saveOrUpdateModel(
      Bookstore bookstore, BookstoreBook bookstoreBook, Book book, EntitiesInDatabase entities) {

    bookRepository.saveOrUpdateBook(book, entities.book);
    bookRepository.saveOrUpdateBookstore(bookstore, entities.bookstore);
    bookRepository.saveOrUpdateBookstoreBook(bookstoreBook, entities.bookstoreBook);
  }

  private String findCurrency(String price) {
    if (price.contains("zł")) {
      return "zł";
    } else {
      return "$";
    }
  }

  private BigDecimal establishPromotionalPrice(BigDecimal retailPrice, String promotionalPriceDto) {
    return promotionalPriceDto.isEmpty()
        ? retailPrice
        : parseStringPriceIntoBigDecimal(promotionalPriceDto);
  }

  private BigDecimal establishRetailPrice(String retailPriceDto, String promotionalPriceDto) {
    return retailPriceDto.isEmpty()
        ? establishPromotionalPrice(null, promotionalPriceDto)
        : parseStringPriceIntoBigDecimal(retailPriceDto);
  }

  private class EntitiesInDatabase {

    private final Book book;
    private final Bookstore bookstore;
    private final BookstoreBook bookstoreBook;

    EntitiesInDatabase(Book book, Bookstore bookstore, BookstoreBook bookstoreBook) {

      this.book = book;
      this.bookstore = bookstore;
      this.bookstoreBook = bookstoreBook;
    }
  }
}
