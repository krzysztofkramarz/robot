package edition.academy.seventh.service;

import edition.academy.seventh.database.model.BookDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 * Manages providers that have been registered by <code>registerPromotionProvider</code> method.
 * Provides API to run all registered {@link PromotionProvider} and stores results of their work.
 *
 * @author Kamil Rojek
 */
@Service
public class PromotionProviderManager {

  private List<BookDto> scrappedBooks;
  private List<PromotionProvider> providers;
  private Phaser phaser = new Phaser(1);

  /** Constructs <code>PromotionProviderManager</code> with non providers registered. */
  public PromotionProviderManager() {
    this.scrappedBooks = new CopyOnWriteArrayList<>();
    this.providers = new ArrayList<>();
  }

  /**
   * Constructs <code>PromotionProviderManager</code> with registered {@link PromotionProvider
   * promotion providers}.
   *
   * @param promotionProvider {@link PromotionProvider promotion provider} to be registered.
   */
  @Autowired
  public PromotionProviderManager(List<PromotionProvider> promotionProvider) {
    this.scrappedBooks = new CopyOnWriteArrayList<>();
    this.providers = promotionProvider;
  }

  /**
   * Adds given List of {@link PromotionProvider promotion providers} to registered promotion
   * providers.
   *
   * @param promotionProviders List of {@link PromotionProvider promotion providers} to be
   *     registered.
   */
  public void registerPromotionProvider(List<PromotionProvider> promotionProviders) {
    promotionProviders.forEach(this::registerPromotionProvider);
  }

  /**
   * Adds single {@link PromotionProvider promotion provider} to registered.
   *
   * @param promotionProvider {@link PromotionProvider promotion provider} to be registered.
   */
  private void registerPromotionProvider(PromotionProvider promotionProvider) {
    providers.add(promotionProvider);
  }

  /**
   * Provides results of single providers' run.
   *
   * @return List of scrapped {@link BookDto books}
   * @throws ProvidersNotFoundException when there are no registered {@link PromotionProvider
   *     promotion providers}.
   */
  public List<BookDto> getScrappedBooks() throws ProvidersNotFoundException {
    if (providers.isEmpty()) throw new ProvidersNotFoundException("No providers detected");

    scrappedBooks.clear();
    runProviders();
    return scrappedBooks;
  }

  private void runProviders() {
    ExecutorService executorService = Executors.newFixedThreadPool(providers.size());
    providers.forEach(
        provider -> {
          phaser.register();
          executorService.submit(runProvider(provider));
        });
    phaser.arriveAndAwaitAdvance();
  }

  private Runnable runProvider(PromotionProvider provider) {
    return () -> {
      List<BookDto> promotions = provider.getPromotions();
      storeBooks(promotions);
      promotions.clear();
    };
  }

  private void storeBooks(List<BookDto> booksOnPromotion) {
    scrappedBooks.addAll(booksOnPromotion);
    phaser.arriveAndDeregister();
  }
}
