package qc.levis.contrat.commun.infrastructure.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Contrat de la migration 073 : même effet sous H2 (tests) et sous PostgreSQL 15 (production, Testcontainers). Une
 * contrainte déclarée en ligne, au nom choisi par le moteur, est retirée ; les autres contraintes restent.
 */
@Testcontainers(disabledWithoutDocker = true)
class RetirerUniciteColonneTest {

    private static final GenericContainer<?> POSTGRES = new GenericContainer<>("postgres:15-alpine")
            .withEnv("POSTGRES_PASSWORD", "essai").withExposedPorts(5432)
            .waitingFor(Wait.forLogMessage(".*ready to accept connections.*\\n", 2)).withStartupTimeout(Duration.ofMinutes(2));

    @BeforeAll
    static void demarrer() { POSTGRES.start(); }

    @AfterAll
    static void arreter() { POSTGRES.stop(); }

    private static Connection h2() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:migration" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
    }

    private static Connection postgres() throws SQLException {
        String url = "jdbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432) + "/postgres";
        Connection c = DriverManager.getConnection(url, "postgres", "essai");
        try (Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS clause");
        }
        return c;
    }

    @Test
    void retire_l_unicite_du_code_sous_h2_comme_sous_postgresql() throws Exception {
        for (Connection c : List.of(h2(), postgres())) {
            try (c) {
                preparer(c);
                retirer(c, "clause", "code");
                try (Statement s = c.createStatement()) {
                    s.execute("INSERT INTO clause (id, tenant_id, code, titre) VALUES (2, 2, 'GEN-001', 'Autre organisation')");
                    // La contrainte sur titre, d'une autre colonne, n'est pas touchée.
                    assertThatThrownBy(() -> s.execute("INSERT INTO clause (id, tenant_id, code, titre) VALUES (3, 3, 'X', 'Unique')"))
                            .isInstanceOf(SQLException.class);
                }
            }
        }
    }

    @Test
    void sans_contrainte_a_retirer_la_migration_ne_fait_rien() throws Exception {
        try (Connection c = h2()) {
            try (Statement s = c.createStatement()) {
                s.execute("CREATE TABLE clause (id BIGINT PRIMARY KEY, code VARCHAR(20))");
            }
            retirer(c, "clause", "code");
            retirer(c, "clause", "code");
        }
    }

    @Test
    void une_erreur_sql_est_rapportee_comme_echec_de_migration() throws Exception {
        Connection c = h2();
        Database base = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
        c.close();
        RetirerUniciteColonne change = change("clause", "code");
        assertThatThrownBy(() -> change.execute(base)).isInstanceOf(CustomChangeException.class)
                .hasMessageContaining("clause.code");
    }

    @Test
    void refuse_un_nom_de_table_ou_de_colonne_qui_n_est_pas_un_identifiant_simple() throws Exception {
        try (Connection c = h2()) {
            preparer(c);
            for (String[] mauvais : new String[][] {{"clause; DROP TABLE x", "code"}, {"clause", null}, {null, "code"}, {"clause", "Code"}}) {
                assertThatThrownBy(() -> retirer(c, mauvais[0], mauvais[1])).isInstanceOf(CustomChangeException.class)
                        .hasMessageContaining("Identifiants simples");
            }
            // Rien n'a été retiré : le code reste unique.
            try (Statement s = c.createStatement()) {
                assertThatThrownBy(() -> s.execute("INSERT INTO clause (id, tenant_id, code) VALUES (9, 9, 'GEN-001')"))
                        .isInstanceOf(SQLException.class);
            }
        }
        assertThat(change("clause", "code").validate(null).hasErrors()).isFalse();
        assertThat(change("clause", "code")).extracting(RetirerUniciteColonne::getTable, RetirerUniciteColonne::getColonne)
                .containsExactly("clause", "code");
        assertThat(change("clause", "code").getConfirmationMessage()).isEqualTo("Unicité de clause.code retirée");
    }

    private static void preparer(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute("CREATE TABLE clause (id BIGINT PRIMARY KEY, tenant_id BIGINT, code VARCHAR(20) NOT NULL UNIQUE,"
                    + " titre VARCHAR(40) UNIQUE)");
            s.execute("INSERT INTO clause (id, tenant_id, code, titre) VALUES (1, 1, 'GEN-001', 'Unique')");
        }
    }

    private static void retirer(Connection c, String table, String colonne) throws Exception {
        Database base = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
        RetirerUniciteColonne change = change(table, colonne);
        change.setUp();
        change.setFileOpener(null);
        change.execute(base);
    }

    private static RetirerUniciteColonne change(String table, String colonne) {
        RetirerUniciteColonne change = new RetirerUniciteColonne();
        change.setTable(table);
        change.setColonne(colonne);
        return change;
    }
}
