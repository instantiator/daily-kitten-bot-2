package instantiator.dailykittybot2.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

@Entity(tableName = "runreport",
        indices = {
            @Index("username"),
            @Index("subreddit"),
            @Index("ruleUuid"),
            @Index("lastConsideredItemDate") },
        foreignKeys = {
            @ForeignKey(
                entity = Rule.class,
                parentColumns = "uuid",
                childColumns = "ruleUuid",
                onDelete = NO_ACTION) })
public class RunReport {

    @PrimaryKey
    @NonNull
    public UUID uuid;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "subreddit")
    public String subreddit;

    @ColumnInfo(name = "ruleUuid")
    public UUID ruleUuid;

    @ColumnInfo(name = "started")
    public Date started;

    @ColumnInfo(name = "finished")
    public Date finished;

    @ColumnInfo(name = "lastConsideredItemDate")
    public Date lastConsideredItemDate;

}
