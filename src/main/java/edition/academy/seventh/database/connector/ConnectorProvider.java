package edition.academy.seventh.database.connector;

import javax.persistence.EntityManager;

/** @author Kamil Rojek */
public interface ConnectorProvider {
  /**
   * Provides entity manager with specific configuration.
   *
   * @return {@link javax.persistence.EntityManager}
   */
  EntityManager getEntityManager();

  /**
   * Closes {@link javax.persistence.EntityManagerFactory} and all provided {@link
   * javax.persistence.EntityManager entity managers}.
   */
  void close();
}
