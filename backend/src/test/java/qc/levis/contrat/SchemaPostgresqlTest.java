package qc.levis.contrat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Le schéma créé par Liquibase (le même en test sous H2 et en production sous PostgreSQL) ne nomme aucune table ni
 * colonne d'un mot réservé de PostgreSQL : Hibernate les écrit sans guillemets, et H2 les accepte là où PostgreSQL refuse
 * la requête. Défaut constaté : la colonne « analyse » de clm_document faisait échouer tout dépôt de document en production.
 */
@SpringBootTest
@TestPropertySource(properties = "app.clm.planificateur.actif=false")
class SchemaPostgresqlTest {

    /** pg_get_keywords() de PostgreSQL 15, catégories R (réservé) et T (réservé, permis comme nom de fonction ou de type). */
    private static final Set<String> RESERVES = Set.of("all", "analyse", "analyze", "and", "any", "array", "as", "asc",
            "asymmetric", "authorization", "binary", "both", "case", "cast", "check", "collate", "collation", "column",
            "concurrently", "constraint", "create", "cross", "current_catalog", "current_date", "current_role", "current_schema",
            "current_time", "current_timestamp", "current_user", "default", "deferrable", "desc", "distinct", "do", "else", "end",
            "except", "false", "fetch", "for", "foreign", "freeze", "from", "full", "grant", "group", "having", "ilike", "in",
            "initially", "inner", "intersect", "into", "is", "isnull", "join", "lateral", "leading", "left", "like", "limit",
            "localtime", "localtimestamp", "natural", "not", "notnull", "null", "offset", "on", "only", "or", "order", "outer",
            "overlaps", "placing", "primary", "references", "returning", "right", "select", "session_user", "similar", "some",
            "symmetric", "table", "tablesample", "then", "to", "trailing", "true", "union", "unique", "user", "using", "variadic",
            "verbose", "when", "where", "window", "with");

    @Autowired JdbcTemplate jdbc;

    @Test
    void aucune_table_ni_colonne_ne_porte_un_mot_reserve_de_postgresql() {
        List<String> fautifs = jdbc.queryForList("""
                SELECT LOWER(TABLE_NAME) || '.' || LOWER(COLUMN_NAME) FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'PUBLIC'""", String.class).stream()
                .filter(n -> RESERVES.contains(n.substring(n.indexOf('.') + 1)) || RESERVES.contains(n.substring(0, n.indexOf('.'))))
                .map(n -> n.toLowerCase(Locale.ROOT)).sorted().toList();
        assertEquals(List.of(), fautifs, "à renommer par une migration Liquibase");
    }
}
