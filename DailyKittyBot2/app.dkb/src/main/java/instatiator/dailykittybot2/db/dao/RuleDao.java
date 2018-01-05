package instatiator.dailykittybot2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import instatiator.dailykittybot2.db.entities.Rule;

@Dao
public interface RuleDao {
    @Query("SELECT * FROM rule")
    LiveData<List<Rule>> getAll();

    @Query("SELECT * FROM rule WHERE uuid IN (:userIds)")
    LiveData<List<Rule>> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM rule WHERE username LIKE :username ORDER BY username")
    LiveData<List<Rule>> loadAllByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Rule... rules);

    @Update
    void updateAll(Rule... rules);

    @Delete
    void delete(Rule user);
}