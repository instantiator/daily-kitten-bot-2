package instantiator.dailykittybot2.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.UUID;

import instantiator.dailykittybot2.db.entities.RunReport;

@Dao
public interface RunReportDao {

    @Transaction
    @Query("SELECT * FROM runreport WHERE username LIKE :username AND subreddit LIKE :subreddit AND ruleUuid LIKE :rule ORDER BY started DESC LIMIT 1")
    RunReport get_last_run_report_for(String username, String subreddit, UUID rule);

    @Insert
    void insertAll(RunReport... reports);

    @Delete
    void delete(RunReport report);

    @Query("DELETE FROM runreport WHERE ruleUuid LIKE :rule")
    void delete_all_for(UUID rule);

    @Query("DELETE FROM runreport WHERE username LIKE :username")
    void delete_all_for(String username);

}
