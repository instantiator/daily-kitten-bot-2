package instatiator.dailykittybot2.data;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Recommendation;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.db.entities.RunReport;

public class RunReportCollation {

    @Embedded
    public RunReport report;

    @Relation(parentColumn = "uuid", entityColumn = "runReportUuid")
    public List<Recommendation> recommendtions;

}
