package instantiator.dailykittybot2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;
import java.util.UUID;

import instantiator.dailykittybot2.db.entities.Condition;

@Dao
public interface ConditionDao {
    @Query("SELECT * FROM condition")
    LiveData<List<Condition>> getAll();

    @Query("SELECT * FROM condition WHERE ruleUuid LIKE (:ruleUuid) ORDER BY ordering DESC")
    public LiveData<List<Condition>> loadAllByRule(UUID ruleUuid);

    @Query("SELECT MAX(ordering) FROM condition WHERE ruleUuid LIKE (:ruleUuid)")
    public int getMaxOrderingForRule(UUID ruleUuid);

    @Query("SELECT * FROM condition WHERE uuid LIKE (:condition) LIMIT 1")
    LiveData<Condition> get(UUID condition);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertAll(Condition... conditions);

    @Update
    public void updateAll(Condition... conditions);

    @Delete
    public void delete(Condition condition);
}