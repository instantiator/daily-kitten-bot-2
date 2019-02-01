package instantiator.dailykittybot2.data;

import instantiator.dailykittybot2.BotApp;
import instantiator.dailykittybot2.R;

public enum ConditionType {
    NeverMatch(
            R.string.condition_type_nothing,
            R.string.condition_type_nothing_hint,
            false,
            false),

    IfTitleContainsString(
            R.string.condition_type_title_contains_string,
            R.string.condition_type_title_contains_string_hint,
            true,
            true),

    IfTitleContainsWordsFrom(
            R.string.condition_type_title_contains_words,
            R.string.condition_type_title_contains_words_hint,
            true,
            true),

    IfTextContainsString(
            R.string.condition_type_text_contains_string,
            R.string.condition_type_text_contains_string_hint,
            true,
            true),

    IfTextContainsWordsFrom(
            R.string.condition_type_text_contains_words,
            R.string.condition_type_text_contains_words_hint,
            true,
            true),

    IfIsExactLink(
            R.string.condition_type_is_exact_link,
            R.string.condition_type_is_exact_link_hint,
            true,
            true),

    IfIsLinkForAnyDomainsOf(
            R.string.condition_type_is_link_with_domains,
            R.string.condition_type_is_link_with_domains_hint,
            true,
            true),

    IfTextContainsLink(
            R.string.condition_type_text_contains_exact_link,
            R.string.condition_type_text_contains_exact_link_hint,
            true,
            true),

    IfTextContainsLinkForAnyDomainsOf(
            R.string.condition_type_text_contains_link_with_domains,
            R.string.condition_type_text_contains_link_with_domains_hint,
            true,
            true),

    IfAnyCommentContainsString(
            R.string.condition_type_any_comment_contains_string,
            R.string.condition_type_any_comment_contains_string_hint,
            true,
            false),

    IfNoCommentContainsString(
            R.string.condition_type_no_comment_contains_string,
            R.string.condition_type_no_comment_contains_string_hint,
            true,
            false),

    IfAnyCommentContainsWordsFrom(
            R.string.condition_type_any_comment_contains_words,
            R.string.condition_type_any_comment_contains_words_hint,
            true,
            false),

    IfNoCommentContainsWordsFrom(
            R.string.condition_type_no_comment_contains_words,
            R.string.condition_type_no_comment_contains_words_hint,
            true,
            false),

    IfAnyCommentContainsLink(
            R.string.condition_type_any_comment_contains_exact_link,
            R.string.condition_type_any_comment_contains_exact_link_hint,
            true,
            false),

    IfAnyCommentContainsListForAnyDomainOf(
            R.string.condition_type_any_comment_contains_link_with_domains,
            R.string.condition_type_any_comment_contains_link_with_domains_hint,
            true,
            false),

    IfNoCommentContainsLink(
            R.string.condition_type_no_comment_contains_exact_link,
            R.string.condition_type_no_comment_contains_exact_link_hint,
            true,
            false),

    IfNoCommentContainsLinkForAnyDomainOf(
            R.string.condition_type_no_comment_contains_link_with_domains,
            R.string.condition_type_no_comment_contains_link_with_domains_hint,
            true,
            false);

    private ConditionType(int description_resource, int hint_resource, boolean specifics, boolean post_filter) {
        this.description = description_resource;
        this.hint = hint_resource;
        this.requires_specifics = specifics;
        this.post_filter = post_filter;
    }

    private int description;
    private int hint;
    private boolean requires_specifics;
    private boolean post_filter;

    public int getDescription() { return description; }

    public int getHint() { return hint; }

    public boolean requiresSpecifics() { return requires_specifics; }

    public boolean isPostFilter() { return post_filter; }

    @Override
    public String toString() { return BotApp.appContext.getString(description); }
}
