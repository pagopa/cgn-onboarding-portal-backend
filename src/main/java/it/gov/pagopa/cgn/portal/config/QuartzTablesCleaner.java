package it.gov.pagopa.cgn.portal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuartzTablesCleaner implements ApplicationListener<ApplicationStartedEvent> {

    private final JdbcTemplate jdbcTemplate;

    public QuartzTablesCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        cleanQuartzTables();
    }

    private void cleanQuartzTables() {
        String[] tables = {
                "QRTZ_FIRED_TRIGGERS",
                "QRTZ_PAUSED_TRIGGER_GRPS",
                "QRTZ_SCHEDULER_STATE",
                "QRTZ_LOCKS",
                "QRTZ_SIMPLE_TRIGGERS",
                "QRTZ_CRON_TRIGGERS",
                "QRTZ_SIMPROP_TRIGGERS",
                "QRTZ_BLOB_TRIGGERS",
                "QRTZ_TRIGGERS",
                "QRTZ_JOB_DETAILS"
        };

        log.info("Quartz tables start cleaning...");

        for (String table : tables) {
            try {
                String delete = "DELETE FROM " + table;
                jdbcTemplate.update(delete);
                log.info("{} OK", delete);
            } catch (Exception e) {
                log.info("Quartz cleanup: couldn't delete from table {} - {}", table, e.getMessage());
            }
        }

        log.info("Quartz tables cleaned successfully.");
    }
}

