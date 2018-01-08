package instatiator.dailykittybot2.ui.pagers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.UUID;

import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.ui.fragments.EditRuleDetailFragment;
import instatiator.dailykittybot2.ui.fragments.UserRulesFragment;

public class EditRulePagerAdapter extends FragmentPagerAdapter {

    private UUID rule_id;
    private Context context;

    public EditRulePagerAdapter(Context context, FragmentManager fm, UUID rule_id) {
        super(fm);
        this.context = context;
        this.rule_id = rule_id;
    }

    public void set_rule_id(UUID uuid) {
        rule_id = uuid;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.tab_rule_details);
            case 1:
                return context.getString(R.string.tab_rule_conditions);
            case 2:
                return context.getString(R.string.tab_rule_outcomes);
            default:
                throw new IllegalStateException("No fragment at index: " + position);
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return EditRuleDetailFragment.create(rule_id); // TODO

            case 1:
                return EditRuleDetailFragment.create(rule_id); // TODO

            case 2:
                return EditRuleDetailFragment.create(rule_id); // TODO

            default:
                throw new IllegalStateException("No fragment at index: " + position);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
