package instatiator.dailykittybot2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.data.RunReportCollation;

@Dao
public interface RunReportCollationDao {

    @Transaction
    @Query("SELECT * FROM runreport WHERE username LIKE :username ORDER BY started DESC")
    List<RunReportCollation> loadAllByDetails(String username);

    @Transaction
    @Query("SELECT * FROM runreport WHERE username LIKE :username AND subreddit LIKE :subreddit ORDER BY started DESC")
    List<RunReportCollation> loadAllByDetails(String username, String subreddit);

    @Transaction
    @Query("SELECT * FROM runreport WHERE username LIKE :username AND subreddit LIKE :subreddit AND ruleUuid LIKE :rule ORDER BY started DESC")
    List<RunReportCollation> loadAllByDetails(String username, String subreddit, UUID rule);

    @Transaction
    @Query("SELECT * FROM runreport WHERE username LIKE :username AND subreddit LIKE :subreddit AND ruleUuid LIKE :rule ORDER BY started DESC LIMIT 1")
    RunReportCollation lastRunReport(String username, String subreddit, UUID rule);

}
