package com.example;

import io.github.fludakit.sqlinit.DbMigrator;
import io.github.fludakit.sqlinit.SqlInitConfig;
import io.github.fludakit.sqlinit.resource.ResourceResolverRegistry;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class SqlInitStandaloneExampleTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.0"))
            .withServices(LocalStackContainer.Service.S3);

    static S3Client s3Client;

    @BeforeAll
    static void setUpS3() throws Exception {
        s3Client = S3Client.builder()
                .endpointOverride(localstack.getEndpoint())
                .region(Region.of(localstack.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                localstack.getAccessKey(),
                                localstack.getSecretKey())))
                .forcePathStyle(true)
                .build();

        // Create bucket
        s3Client.createBucket(CreateBucketRequest.builder().bucket("migrations-bucket").build());

        // Upload migration file to S3
        Path migrationFile = Path.of("src/main/resources/db/migration/V20240101__create_engineers.sql");
        String sqlContent = Files.readString(migrationFile);
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket("migrations-bucket")
                        .key("sql/V20240101__create_engineers.sql")
                        .build(),
                RequestBody.fromString(sqlContent));
    }

    @Test
    void testMigrationWithS3AndCustomVersionStrategy() throws Exception {
        // Create H2 DataSource
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // Configure SQL init with custom version strategy and S3 location
        SqlInitConfig config = SqlInitConfig.builder()
                .scriptLocations(List.of("s3://migrations-bucket/sql/"))
                .versionStrategy(new DateVersionStrategy())
                .build();

        // Register S3 resource resolver
        ResourceResolverRegistry resolverRegistry = new ResourceResolverRegistry();
        resolverRegistry.register("s3", new S3ResourceResolver(s3Client));

        // Run migrations
        DbMigrator migrator = new DbMigrator(dataSource, config, resolverRegistry);
        migrator.migrate();

        // Verify the table was created
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM engineers");
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));

            // Insert and verify
            stmt.execute("INSERT INTO engineers (name) VALUES ('Ada Lovelace')");
            rs = stmt.executeQuery("SELECT name FROM engineers");
            assertTrue(rs.next());
            assertEquals("Ada Lovelace", rs.getString("name"));
        }
    }

    @Test
    void testDateVersionStrategy() {
        DateVersionStrategy strategy = new DateVersionStrategy();

        // Valid dates
        assertEquals("20240101", strategy.parse("20240101"));
        assertEquals("20241231", strategy.parse("20241231"));

        // Comparison
        assertTrue(strategy.compare("20240101", "20241231") < 0);
        assertTrue(strategy.compare("20241231", "20240101") > 0);
        assertEquals(0, strategy.compare("20240101", "20240101"));
    }
}
