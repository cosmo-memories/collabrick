package nz.ac.canterbury.seng302.homehelper.config.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled jobs for cleaning up old LiveUpdate data.
 */
@Component
public class LiveUpdateConfig {

    private static final Logger logger = LoggerFactory.getLogger(LiveUpdateConfig.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Save only the last 20 rows per renovation (excluding invitation-related notifications).
     * Runs automatically every 12hrs.
     * SQL by ChatGPT.
     */
//    @Scheduled(fixedRate = 43200000)   // Runs every 12 hours
//    public void cleanOldLiveUpdates() {
//        String sql = """
//            DELETE FROM LIVE_UPDATE
//            WHERE id IN (
//                SELECT * FROM (
//                    SELECT id
//                    FROM (
//                        SELECT id,
//                               ROW_NUMBER() OVER (
//                                   PARTITION BY renovation_id
//                                   ORDER BY timestamp DESC, id DESC
//                               ) AS rn
//                        FROM LIVE_UPDATE
//                        WHERE activity_type NOT IN ('INVITE_ACCEPTED', 'INVITE_DECLINED')
//                    ) AS ranked
//                    WHERE rn > 20
//                ) AS to_delete
//            );
//        """;
//
//        logger.info("Cleaning old LiveUpdates");
//        jdbcTemplate.execute(sql);
//    }
//
//    /**
//     * Save only the last 20 invitation-related updates per renovation.
//     * Runs automatically every 12hrs.
//     */
//    @Scheduled(fixedRate = 43200000)   // Runs every 12 hours
//    public void cleanOldInviteUpdates() {
//        String sql = """
//            DELETE FROM LIVE_UPDATE
//            WHERE id IN (
//                SELECT * FROM (
//                    SELECT id
//                    FROM (
//                        SELECT id,
//                               ROW_NUMBER() OVER (
//                                   PARTITION BY renovation_id
//                                   ORDER BY timestamp DESC, id DESC
//                               ) AS rn
//                        FROM LIVE_UPDATE
//                        WHERE activity_type IN ('INVITE_ACCEPTED', 'INVITE_DECLINED')
//                    ) AS ranked
//                    WHERE rn > 20
//                ) AS to_delete
//            );
//        """;
//
//        logger.info("Cleaning old invitation LiveUpdates");
//        jdbcTemplate.execute(sql);
//    }

}
