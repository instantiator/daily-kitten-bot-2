package instatiator.dailykittybot2.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

@Entity(tableName = "rule", indices = { @Index("username") })
public class Rule {
    @PrimaryKey
    @NonNull
    public UUID uuid;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "rulename")
    public String rulename;

}
