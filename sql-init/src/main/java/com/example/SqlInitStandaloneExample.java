package com.example;

import io.github.fludakit.sqlinit.DbMigrator;
import io.github.fludakit.sqlinit.SqlInitConfig;
import io.github.fludakit.sqlinit.resource.ResourceResolverRegistry;
import org.h2.jdbcx.JdbcDataSource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.util.List;
import javax.sql.DataSource;

/**
 * Standalone example demonstrating:
 * 1. Custom date-based version strategy (V20240101, V20241231)
 * 2. S3 resource resolver for loading migrations from AWS S3
 * 3. Plain Java SE usage without CDI
 */
public class SqlInitStandaloneExample {

    public static void main(String[] args) throws Exception {
        // Create H2 DataSource
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:standalone;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // Configure SQL init with custom version strategy
        SqlInitConfig config = SqlInitConfig.builder()
                .scriptLocations(List.of("s3://migrations-bucket/sql/"))
                .versionStrategy(new DateVersionStrategy())
                .build();

        // Create S3 client (for LocalStack in tests)
        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create("http://localhost:4566"))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .forcePathStyle(true)
                .build();

        // Register S3 resource resolver
        ResourceResolverRegistry resolverRegistry = new ResourceResolverRegistry();
        resolverRegistry.register("s3", new S3ResourceResolver(s3Client));

        // Run migrations
        DbMigrator migrator = new DbMigrator(dataSource, config, resolverRegistry);
        migrator.migrate();

        System.out.println("Database migration completed successfully!");
    }
}
