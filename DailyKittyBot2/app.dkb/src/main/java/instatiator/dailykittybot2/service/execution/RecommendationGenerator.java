package instatiator.dailykittybot2.service.execution;

import net.dean.jraw.models.Submission;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.data.TargetType;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Recommendation;

public class RecommendationGenerator {

    public List<Recommendation> generate_recommendations_for(RuleTriplet rule, Submission submission) {
        List<Recommendation> recommendations = new LinkedList<>();

        for (Outcome outcome : rule.outcomes) {
            Recommendation recommendation = new Recommendation();
            recommendation.uuid = UUID.randomUUID();
            recommendation.created = new Date();
            recommendation.ruleUuid = rule.rule.uuid;
            recommendation.ruleName = rule.rule.rulename;
            recommendation.outcomeUuid = outcome.uuid;
            recommendation.username = rule.rule.username;
            recommendation.type = outcome.type;
            recommendation.modifier = outcome.modifier;
            recommendation.targetSubreddit = submission.getSubreddit();

            // assume we are acting on a post
            // in future versions, it will be possible to act on comments too
            recommendation.targetType = TargetType.Post;
            recommendation.targetSubmissionId = submission.getId();
            recommendation.targetSummary = summarise_post(submission);

            recommendations.add(recommendation);
        }

        return recommendations;
    }

    private String summarise_post(Submission submission) {
        return submission.getTitle();
    }


}
