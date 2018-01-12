package instatiator.dailykittybot2.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.data.OutcomeType;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

@Entity(tableName = "result", indices = { @Index("username"), @Index("ran"), @Index("ruleUuid"), @Index("recommendationUuid") },
        foreignKeys = {
            @ForeignKey(entity = Rule.class,
                parentColumns = "uuid",
                childColumns = "ruleUuid",
                onDelete = NO_ACTION),
            @ForeignKey(entity = Recommendation.class,
                parentColumns = "uuid",
                childColumns = "recommendationUuid",
                onDelete = NO_ACTION),
            })
public class Result {
    @PrimaryKey
    @NonNull
    public UUID uuid;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "ruleUuid")
    public UUID ruleUuid;

    @ColumnInfo(name = "recommendationUuid")
    public UUID recommendationUuid;

    @ColumnInfo(name = "ran")
    public Date ran;

    @ColumnInfo(name = "ruleName")
    public String ruleName;

    @ColumnInfo(name = "conditionSummaries")
    public List<String> conditionSummaries;

    @ColumnInfo(name = "outcomeType")
    public OutcomeType outcomeType;

    @ColumnInfo(name = "outcomeModifier")
    public String outcomeModifier;

    @ColumnInfo(name = "success")
    public boolean success;

    @ColumnInfo(name = "reports")
    public List<String> reports;

    @ColumnInfo(name = "issues")
    public List<String> issues;
}
