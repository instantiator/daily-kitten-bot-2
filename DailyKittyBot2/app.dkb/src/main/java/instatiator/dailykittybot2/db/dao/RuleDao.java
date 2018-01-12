package instatiator.dailykittybot2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.db.entities.Rule;

@Dao
public interface RuleDao {
    @Query("SELECT * FROM rule ORDER BY rulename")
    LiveData<List<Rule>> getAll();

    @Query("SELECT * FROM rule WHERE uuid IN (:userIds) ORDER BY rulename")
    LiveData<List<Rule>> loadAllByIds(UUID[] userIds);

    @Query("SELECT * FROM rule WHERE uuid LIKE (:rule) LIMIT 1")
    LiveData<Rule> get(UUID rule);

    @Query("SELECT * FROM rule WHERE username LIKE :username ORDER BY rulename")
    LiveData<List<Rule>> loadAllByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Rule... rules);

    @Update
    void updateAll(Rule... rules);

    @Delete
    void delete(Rule user);
}