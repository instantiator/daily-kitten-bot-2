package instatiator.dailykittybot2.data;

import instatiator.dailykittybot2.BotApp;
import instatiator.dailykittybot2.R;

public enum ConditionType {
    NeverMatch(
            R.string.condition_type_nothing,
            R.string.condition_type_nothing_hint,
            false),

    IfTitleContainsString(
            R.string.condition_type_title_contains_string,
            R.string.condition_type_title_contains_string_hint,
            true),

    IfTitleContainsWordsFrom(
            R.string.condition_type_title_contains_words,
            R.string.condition_type_title_contains_words_hint,
            true),

    IfIsExactLink(
            R.string.condition_type_is_exact_link,
            R.string.condition_type_is_exact_link_hint,
            true),

    IfIsLinkForAnyDomainsOf(
            R.string.condition_type_is_link_with_domains,
            R.string.condition_type_is_link_with_domains_hint,
            true),

    IfAnyCommentContainsString(
            R.string.condition_type_any_comment_contains_string,
            R.string.condition_type_any_comment_contains_string_hint,
            true),

    IfNoCommentContainsString(
            R.string.condition_type_no_comment_contains_string,
            R.string.condition_type_no_comment_contains_string_hint,
            true);

    private ConditionType(int description_resource, int hint_resource, boolean specifics) {
        this.description = description_resource;
        this.hint = hint_resource;
        this.requires_specifics = specifics;
    }

    private int description;
    private int hint;
    private boolean requires_specifics;

    public int getDescription() { return description; }

    public int getHint() { return hint; }

    public boolean requiresSpecifics() { return requires_specifics; }

    @Override
    public String toString() { return BotApp.appContext.getString(description); }
}
