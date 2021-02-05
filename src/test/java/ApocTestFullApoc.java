import apoc.ApocConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Works with Neo4j 4.0
 * Doesn't work with 4.1 and 4.2
 */
public class ApocTestFullApoc {
    private static Neo4j neo4j;
    private static Driver driver;

    @BeforeAll
    static void initializeNeo4j() throws URISyntaxException {
        Path pluginDirContainingApocJar = new File(
                ApocConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParentFile().toPath();

        neo4j = Neo4jBuilders
                .newInProcessBuilder()
                .withDisabledServer()
                .withFixture("CREATE (p1:Person)-[:knows]->(p2:Person)-[:knows]->(p3:Person)")
                .withConfig(GraphDatabaseSettings.plugin_dir, pluginDirContainingApocJar)
                .withConfig(GraphDatabaseSettings.procedure_unrestricted, List.of("apoc.*"))
                .build();
        driver = GraphDatabase.driver(neo4j.boltURI(), AuthTokens.none());
    }

    @AfterAll
    static void stopNeo4j() {
        driver.close();
        neo4j.close();
    }

    @Test
    public void testApoc(){
        String query = "MATCH path=()-[:knows*2]->()\n" +
                "RETURN apoc.coll.toSet(nodes(path)) AS nodesSet";
        List<Object> nodesSet = driver.session()
                .beginTransaction()
                .run(query)
                .single()
                .get("nodesSet")
                .asList();
        assertEquals(3, nodesSet.size());
    }
}