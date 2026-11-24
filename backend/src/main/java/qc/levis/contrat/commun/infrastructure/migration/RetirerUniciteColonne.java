package qc.levis.contrat.commun.infrastructure.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Retire la contrainte d'unicité portant sur une seule colonne d'une table. Les contraintes déclarées en ligne dans
 * {@code createTable} reçoivent un nom choisi par le moteur ({@code clause_code_key} sous PostgreSQL,
 * {@code CONSTRAINT_76} sous H2) : on le retrouve dans {@code information_schema}, commun aux deux.
 */
public class RetirerUniciteColonne implements CustomTaskChange {

    private static final String CONTRAINTES = """
            SELECT tc.constraint_name FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage k
              ON k.constraint_name = tc.constraint_name AND k.table_name = tc.table_name
             AND k.table_schema = tc.table_schema
            WHERE tc.constraint_type = 'UNIQUE' AND LOWER(tc.table_name) = ?
              AND LOWER(tc.table_schema) = LOWER(CURRENT_SCHEMA)
            GROUP BY tc.constraint_name
            HAVING COUNT(*) = 1 AND MAX(LOWER(k.column_name)) = ?""";

    private String table;
    private String colonne;

    // Accesseurs complets : Liquibase n'injecte un paramètre que dans une propriété JavaBean lisible et modifiable.
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    public String getColonne() { return colonne; }
    public void setColonne(String colonne) { this.colonne = colonne; }

    @Override
    public void execute(Database database) throws CustomChangeException {
        // Le nom de table est concaténé à l'ordre SQL : il doit être un identifiant simple. Vérifié ici, et non dans
        // validate(), que Liquibase appelle avant d'avoir fourni les paramètres.
        if (!identifiant(table) || !identifiant(colonne)) {
            throw new CustomChangeException("Identifiants simples attendus : " + table + "." + colonne);
        }
        Connection connexion = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
        try {
            for (String nom : contraintes(connexion)) {
                try (Statement s = connexion.createStatement()) {
                    s.execute("ALTER TABLE " + table + " DROP CONSTRAINT \"" + nom.replace("\"", "") + "\"");
                }
            }
        } catch (SQLException e) {
            throw new CustomChangeException("Unicité de " + table + "." + colonne + " non retirée", e);
        }
    }

    private List<String> contraintes(Connection connexion) throws SQLException {
        List<String> noms = new ArrayList<>();
        try (PreparedStatement p = connexion.prepareStatement(CONTRAINTES)) {
            p.setString(1, table.toLowerCase(Locale.ROOT));
            p.setString(2, colonne.toLowerCase(Locale.ROOT));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) noms.add(r.getString(1));
            }
        }
        return noms;
    }

    private static boolean identifiant(String nom) {
        return nom != null && nom.matches("[a-z_]+");
    }

    @Override
    public ValidationErrors validate(Database database) { return new ValidationErrors(); }

    @Override
    public String getConfirmationMessage() { return "Unicité de " + table + "." + colonne + " retirée"; }

    @Override
    public void setUp() { /* aucune ressource à préparer */ }

    @Override
    public void setFileOpener(ResourceAccessor accessor) { /* aucun fichier lu */ }
}
