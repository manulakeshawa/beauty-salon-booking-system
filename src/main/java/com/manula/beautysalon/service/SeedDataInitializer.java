package com.manula.beautysalon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Component
public class SeedDataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SeedDataInitializer.class);

    // data.sql is demo/fresh-database data, not a source of truth for a live salon database.
    // The seed marker prevents deleted or edited live records from being recreated on restart.
    private static final String SEED_NAME = "demo-data-v1";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final boolean seedEnabled;

    public SeedDataInitializer(
            JdbcTemplate jdbcTemplate,
            DataSource dataSource,
            @Value("${app.seed.enabled:true}") boolean seedEnabled
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            logger.info("Demo seed data is disabled. Skipping data.sql.");
            return;
        }

        ensureSeedHistoryTableExists();

        if (hasSeedAlreadyRun()) {
            logger.info("Demo seed data has already been applied. Skipping data.sql.");
            return;
        }

        if (!isApplicationDataEmpty()) {
            // If operators already have data, record the seed as complete without importing
            // demo rows. This avoids overwriting intentional MySQL changes with sample data.
            markSeedAsRun();
            logger.info("Existing application data found. Marked demo seed as complete without running data.sql.");
            return;
        }

        runSeedScript();
        markSeedAsRun();
        logger.info("Demo seed data applied from data.sql.");
    }

    private void ensureSeedHistoryTableExists() {
        // This table records that the demo seed has run once, so data.sql is not repeatedly
        // re-applied every time the application starts.
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_seed_history (
                    seed_name VARCHAR(100) NOT NULL PRIMARY KEY,
                    seeded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private boolean hasSeedAlreadyRun() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_seed_history WHERE seed_name = ?",
                Integer.class,
                SEED_NAME
        );
        return count != null && count > 0;
    }

    private boolean isApplicationDataEmpty() {
        return countRows("customers") == 0
                && countRows("employees") == 0
                && countRows("stylists") == 0
                && countRows("salon_services") == 0
                && countRows("appointments") == 0
                && countRows("reviews") == 0;
    }

    private long countRows(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `" + tableName + "`", Long.class);
        return count == null ? 0 : count;
    }

    private void runSeedScript() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("data.sql"));
        populator.execute(dataSource);
    }

    private void markSeedAsRun() {
        jdbcTemplate.update(
                "INSERT IGNORE INTO app_seed_history (seed_name) VALUES (?)",
                SEED_NAME
        );
    }
}
