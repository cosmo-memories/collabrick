package nz.ac.canterbury.seng302.homehelper.repository.activity;

import nz.ac.canterbury.seng302.homehelper.entity.activity.LiveUpdate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for interacting with stored LiveActivity data.
 */
@Repository
public interface ActivityRepository extends CrudRepository<LiveUpdate, Long> {

    @Query(value = "SELECT lu.* FROM live_update lu " +
            "INNER JOIN renovation r ON lu.renovation_id = r.id " +
            "INNER JOIN renovation_member rm ON r.id = rm.renovation_id " +
            "WHERE rm.user_id = :userId " +
            "AND (rm.role = 0 " +  // 0 = OWNER ordinal
            "     OR (rm.role != 0 " +
            "         AND lu.activity_type NOT IN ('INVITE_ACCEPTED', 'INVITE_DECLINED'))) " +
            "ORDER BY lu.timestamp DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<LiveUpdate> findLast10UpdatesForUser(@Param("userId") Long userId);
}
