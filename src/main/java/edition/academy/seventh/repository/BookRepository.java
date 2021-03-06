package edition.academy.seventh.repository;

import edition.academy.seventh.database.connector.ConnectorFactory;
import edition.academy.seventh.database.connector.ConnectorProvider;
import edition.academy.seventh.database.model.BookDto;
import edition.academy.seventh.database.model.BookstoreBookDto;
import edition.academy.seventh.model.Book;
import edition.academy.seventh.model.BookId;
import edition.academy.seventh.model.Bookstore;
import edition.academy.seventh.model.BookstoreBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

import static edition.academy.seventh.database.connector.DatabaseType.*;
import static edition.academy.seventh.repository.ModelParserIntoBookDtos.parseBookstoreBooksIntoBookDtos;

/**
 * Allows to persists and retrieve data about books from the database. This information is
 * transferred through the application as {@link BookDto}.
 *
 * @author Agnieszka Trzewik
 */
@Repository
public class BookRepository {
  private static final Logger logger = LoggerFactory.getLogger(BookRepository.class);
  private EntityManager entityManager;
  private ConnectorProvider connectorProvider;
  private BookDtoParser bookDtoParser;

  @Autowired
  public BookRepository(BookDtoParser bookDtoParser) {
    connectorProvider = ConnectorFactory.of(POSTGRESQL);
    this.bookDtoParser = bookDtoParser;
  }

  /**
   * Adds books records to the database.
   *
   * @param bookDtos {@code List<BookDto>} to be added
   */
  public void addBooksToDatabase(List<BookDto> bookDtos) {
    entityManager = connectorProvider.getEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    transaction.begin();
    bookDtos.forEach(this::addBookToDatabase);
    logger.info("Saving " + bookDtos.size() + " books in database");
    transaction.commit();
    connectorProvider.close();
  }

  /**
   * Retrieves all books with latest price information from database.
   *
   * @return {@code List<BookDto>}
   */
  public List<BookDto> getLatestBooksFromDatabase() {

    entityManager = connectorProvider.getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<BookstoreBook> query = criteriaBuilder.createQuery(BookstoreBook.class);

    Root<BookstoreBook> from = query.from(BookstoreBook.class);
    query.select(from);
    List<BookstoreBook> bookstoreBookList = entityManager.createQuery(query).getResultList();

    logger.info("Called getBooksFromDatabase(), returning " + bookstoreBookList.size() + " books");
    entityManager.close();

    return parseBookstoreBooksIntoBookDtos(bookstoreBookList);
  }

  /**
   * Retrieves specific {@link BookstoreBook} from the database based on the book's hyperlink. If
   * there href does not exist, then it return null.
   *
   * @param href link of the searched book.
   * @return {@link BookstoreBook} found by id, or null if href does not exist.
   */
  public BookstoreBookDto getBookstoreBookDtoByHref(String href) {

    entityManager = connectorProvider.getEntityManager();

    BookstoreBook bookstoreBook = entityManager.find(BookstoreBook.class, href);

    if (bookstoreBook == null) {
      logger.info("Cannot find book with href " + href);
      return null;
    }

    logger.info("Called getBookstoreBookDtoByHref()");

    entityManager.close();

    return bookDtoParser.parseBookstoreBookIntoBookstoreBookDto(bookstoreBook);
  }

  Book getBookById(BookId bookId) {
    return entityManager.find(Book.class, bookId);
  }

  Bookstore getBookstoreById(String bookstoreId) {
    return entityManager.find(Bookstore.class, bookstoreId);
  }

  BookstoreBook getBookstoreBookById(String bookstoreBookId) {
    return entityManager.find(BookstoreBook.class, bookstoreBookId);
  }

  void setConnectorProvider(ConnectorProvider connectorProvider) {
    this.connectorProvider = connectorProvider;
  }

  /**
   * Updates new {@link BookstoreBook} with existing values from database, or persist whole entity.
   *
   * @param bookstoreBook new to save
   * @param bookstoreBookAlreadyInDatabase existing in database
   */
  void saveOrUpdateBookstoreBook(
      BookstoreBook bookstoreBook, BookstoreBook bookstoreBookAlreadyInDatabase) {
    if (bookstoreBookAlreadyInDatabase != null) {
      bookstoreBook.setHyperlink(bookstoreBookAlreadyInDatabase.getHyperlink());
      entityManager.merge(bookstoreBook);
    } else {
      entityManager.persist(entityManager.merge(bookstoreBook));
    }
  }

  /**
   * Updates new {@link Bookstore} with existing values from database, or persist whole entity.
   *
   * @param bookstore new to save
   * @param bookstoreAlreadyInDatabase existing in database
   */
  void saveOrUpdateBookstore(Bookstore bookstore, Bookstore bookstoreAlreadyInDatabase) {
    if (bookstoreAlreadyInDatabase != null) {
      bookstore.setName(bookstoreAlreadyInDatabase.getName());
      entityManager.merge(bookstore);
    } else {
      entityManager.persist(bookstore);
    }
  }

  /**
   * Updates new {@link Book} with existing values from database, or persist whole entity.
   *
   * @param book new to save
   * @param bookAlreadyInDatabase existing in database
   */
  void saveOrUpdateBook(Book book, Book bookAlreadyInDatabase) {
    if (bookAlreadyInDatabase != null) {
      book.setBookId(bookAlreadyInDatabase.getBookId());
      entityManager.merge(book);
    } else {
      entityManager.persist(book);
    }
  }

  private void addBookToDatabase(BookDto bookDto) {
    bookDtoParser.parseBookDtoIntoModel(bookDto);
  }
}
