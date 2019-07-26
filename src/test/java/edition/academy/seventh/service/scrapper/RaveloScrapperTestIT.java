package edition.academy.seventh.service.scrapper;

import edition.academy.seventh.database.model.BookDto;
import edition.academy.seventh.service.PromotionProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

@Test
public class RaveloScrapperTestIT {

    @Test
    public void should_scrapAtLeastOneBook_forGivenUrl() {
        // Given
        PromotionProvider promotionScrapping = new ScrapperConfiguration().raveloPromotionProvider();

        // When
        List<BookDto> books = promotionScrapping.getPromotions();

        // Then
        assertTrue(books.size() > 0);
    }
}