package edition.academy.seventh.service;

import edition.academy.seventh.database.connector.ConnectorFactory;
import edition.academy.seventh.database.connector.ConnectorProvider;
import edition.academy.seventh.security.model.request.RegisterForm;
import edition.academy.seventh.security.service.AuthenticationService;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Set;

import static edition.academy.seventh.database.connector.DatabaseType.*;

/** @author Patryk Kucharski */
@Service
public class DatabaseInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);
  private AuthenticationService authenticationService;
  private EntityManager entityManager;
  private ConnectorProvider connectorProvider;

  @Autowired
  public DatabaseInitializer(
      AuthenticationService authenticationService) {
    connectorProvider = ConnectorFactory.of(POSTGRESQL);
    this.authenticationService = authenticationService;
  }
  /* TODO: 30.07.1 Nie usuwać - do poprawy w następnym sprincie. Przy każdym uruchomieniu aplikacji, program próbuje dodać role
  // TODO: i konto administratora. Po pierwszym uruchomieniu one już tam są, przez co wyrzuca nam wyjątek.
  // TODO: chwilowo rozwiązane tak, że dodaliśmy ich raz i zakomentowaliśmy kod, który próbuje to robić przy każdym następnym uruchomienu.
   */
  //

  //  /**
  //   * Populates database with {@link edition.academy.seventh.security.model.RoleName roles}
  //   * and adds hardcoded ROLE_ADMIN users.
  //   */
  //  @PostConstruct
  //  public void populateDatabase() {
  //    addUserRolesToDatabase();
  //    addAdminsToDatabase();
  //  }

  private void addAdminsToDatabase() {
    LOGGER.info("Adding admins");
    authenticationService.createNewAccount(
        new RegisterForm(
            "pan@pawel.com", "pan_pawel",
                  Set.of("admin"), "tajnehaslo"));
  }

  private void addUserRolesToDatabase() {
    entityManager = connectorProvider.getEntityManager();
    LOGGER.info("Adding user roles");
    entityManager.getTransaction().begin();
    Query nativeQuery = entityManager.createNativeQuery(
            "INSERT INTO role(name) VALUES('ROLE_ADMIN');\n"
                    + "INSERT INTO role(name) VALUES('ROLE_USER');");
    nativeQuery.executeUpdate();
    entityManager.getTransaction().commit();
    entityManager.close();
  }
}
