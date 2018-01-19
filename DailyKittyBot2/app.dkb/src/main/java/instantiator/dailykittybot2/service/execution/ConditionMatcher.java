package instantiator.dailykittybot2.service.execution;

import android.net.Uri;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;

import instantiator.dailykittybot2.db.entities.Condition;

public class ConditionMatcher {

    RedditClient reddit;

    public ConditionMatcher(RedditClient reddit) {
        this.reddit = reddit;
    }

    public boolean condition_matches(Condition condition, Submission submission) {

        switch (condition.type) {
            case NeverMatch:
                return false;

            case IfTitleContainsString:
                return title_contains(submission, condition.modifier);

            case IfTitleContainsWordsFrom:
                return title_contains_any_word(submission, condition.modifier);

            case IfTextContainsString:
                return text_contains(submission, condition.modifier);

            case IfTextContainsWordsFrom:
                return text_contains_any_word(submission, condition.modifier);

            case IfIsExactLink:
                return is_exact_link(submission, condition.modifier);

            case IfIsLinkForAnyDomainsOf:
                return links_domain_from(submission, condition.modifier);

            case IfAnyCommentContainsString:
                return a_comment_contains(submission, condition.modifier);

            case IfNoCommentContainsString:
                return no_comment_contains(submission, condition.modifier);

            default:
                throw new IllegalArgumentException("Unrecognised condition type.");
        }
    }

    private boolean title_contains(Submission submission, String content) {
        return StringUtils.containsIgnoreCase(submission.getTitle(), content);
    }

    private boolean title_contains_any_word(Submission submission, String wordstring) {
        String[] words = StringUtils.split(wordstring, ',');
        for (String word : words) {
            if (title_contains(submission, word.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean text_contains(Submission submission, String content) {
        return StringUtils.containsIgnoreCase(submission.getSelfText(), content);
    }

    private boolean text_contains_any_word(Submission submission, String wordstring) {
        String[] words = StringUtils.split(wordstring, ',');
        for (String word : words) {
            if (text_contains(submission, word.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean is_exact_link(Submission submission, String link) {
        Uri comparison_uri = Uri.parse(link);
        Uri submission_uri = Uri.parse(submission.getUrl());
        return comparison_uri.compareTo(submission_uri) == 0;
    }

    private boolean links_domain_from(Submission submission, String domainstring) {
        Uri submission_uri = Uri.parse(submission.getUrl());
        String[] domains = StringUtils.split(domainstring, ',');
        for (String domain : domains) {
            Uri domain_uri = Uri.parse(domain);
            if (StringUtils.equalsIgnoreCase(domain_uri.getHost(), submission_uri.getHost())) {
                return true;
            }
        }
        return false;
    }

    private boolean a_comment_contains(Submission submission, String content) {
        RootCommentNode root = reddit.submission(submission.getId()).comments();

        Iterator<CommentNode<PublicContribution<?>>> it = root.walkTree().iterator();
        while (it.hasNext()) {
            PublicContribution<?> comment = it.next().getSubject();
            String text = comment.getBody();
            if (StringUtils.containsIgnoreCase(text, content)) { return true; }
        }

        return false;
    }

    private boolean no_comment_contains(Submission submission, String content) {
        RootCommentNode root = reddit.submission(submission.getId()).comments();

        Iterator<CommentNode<PublicContribution<?>>> it = root.walkTree().iterator();
        while (it.hasNext()) {
            PublicContribution<?> comment = it.next().getSubject();
            String text = comment.getBody();
            if (StringUtils.containsIgnoreCase(text, content)) { return false; }
        }

        return true;
    }

}
