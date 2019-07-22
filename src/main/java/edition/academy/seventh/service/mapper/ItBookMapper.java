package edition.academy.seventh.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import edition.academy.seventh.database.model.DtoBook;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Responsible for mapping JSON objects from ITBook library to {@link DtoBook}.
 *
 * @author Bartosz Kupajski
 */
@Service
public class ItBookMapper {

  private ObjectMapper objectMapper;

  public ItBookMapper() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Maps JSON books file into {@code List<Book>}
   *
   * @param listOfJSONBook list with books' JSONs
   * @return {@code List<Book>}
   * @throws IOException when JSON isn't read properly
   */
  public List<DtoBook> mapListOfJson(List<String> listOfJSONBook) throws IOException {
    List<DtoBook> listOfBooks = new LinkedList<>();
    String nameOfTheBookstore = "ITBookstore";

    for (String bookJSON : listOfJSONBook) {
      DtoBook book = objectMapper.readValue(bookJSON, DtoBook.class);
      book.setBookstore(nameOfTheBookstore);
      book.setPromotionalPrice("");
      listOfBooks.add(book);
    }

    return listOfBooks;
  }
}