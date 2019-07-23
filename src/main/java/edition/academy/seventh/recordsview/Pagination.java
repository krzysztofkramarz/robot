package edition.academy.seventh.recordsview;

import edition.academy.seventh.database.model.BookDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** @author Kamil Rojek */
public class Pagination {
  private List<BookDto> books;
  private Map<Integer, List<BookDto>> paginationMap;

  public Pagination(List<BookDto> books) {
    this.books = books;
  }

  public Map<Integer, List<BookDto>> createPagination(PaginationSize paginationSize) {
    paginationMap = new LinkedHashMap<>();
    int recordsCounter = 0;
    int pageNumber = 1;

    for (BookDto book : books) {
      if (++recordsCounter <= paginationSize.value) {
        updatePaginationMap(pageNumber, book);
        continue;
      }
      recordsCounter = 0;
      pageNumber++;
    }

    return paginationMap;
  }

  private void updatePaginationMap(int pageNumber, BookDto book) {
    if (!paginationMap.containsKey(pageNumber)) {
      paginationMap.put(pageNumber, new ArrayList<>());
    }

    List<BookDto> books = paginationMap.get(pageNumber);
    books.add(book);
    paginationMap.put(pageNumber, books);
  }
}
