package instantiator.dailykittybot2.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "enaction",
        foreignKeys = @ForeignKey(entity = Recommendation.class,
                parentColumns = "uuid",
                childColumns = "recommendationUuid",
                onDelete = CASCADE),
        indices = { @Index("recommendationUuid") })
public class Enaction {

    @PrimaryKey
    @NonNull
    public UUID uuid;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "recommendationUuid")
    public UUID recommendationUuid;

    @ColumnInfo(name = "success")
    public boolean success;

    @ColumnInfo(name = "errors")
    public List<String> errors;

    @ColumnInfo(name = "started")
    public Date started;

    @ColumnInfo(name = "completed")
    public Date completed;

    @ColumnInfo(name = "description_short")
    public String description_short;

    @ColumnInfo(name = "description_long")
    public String description_long;

}
