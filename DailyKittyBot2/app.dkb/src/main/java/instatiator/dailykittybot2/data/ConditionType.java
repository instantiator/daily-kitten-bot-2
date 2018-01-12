package instatiator.dailykittybot2.data;

import instatiator.dailykittybot2.R;

public enum ConditionType {
    NothingSelected(R.string.condition_type_nothing),
    TitleContainsString(R.string.condition_type_title_contains_string),
    TitleContainsWords(R.string.condition_type_title_contains_words),
    IsExactLink(R.string.condition_type_is_exact_link),
    IsLinkWithDomains(R.string.condition_type_is_link_with_domains);

    private ConditionType(int description_resource) {
        this.description = description_resource;
    }

    private int description;

    public int getDescription() { return description; }

    @Override
    public String toString() { return name(); }
}
