package instatiator.dailykittybot2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Recommendation;

@Dao
public interface OutcomeDao {
    @Query("SELECT * FROM outcome")
    LiveData<List<Outcome>> getAll();

    @Query("SELECT * FROM outcome WHERE ruleUuid LIKE (:ruleUuid) ORDER BY ordering")
    public LiveData<List<Outcome>> loadAllByRule(UUID ruleUuid);

    @Query("SELECT * FROM outcome WHERE uuid LIKE (:outcome) LIMIT 1")
    LiveData<Outcome> get(UUID outcome);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertAll(Outcome... outcomes);

    @Update
    public void updateAll(Outcome... outcomes);

    @Delete
    public void delete(Outcome outcome);
}