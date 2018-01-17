package instantiator.dailykittybot2.ui.pagers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.ui.fragments.EditRuleConditionsFragment;
import instantiator.dailykittybot2.ui.fragments.EditRuleDetailFragment;
import instantiator.dailykittybot2.ui.fragments.EditRuleOutcomesFragment;

public class EditRulePagerAdapter extends FragmentPagerAdapter {

    private Context context;

    public EditRulePagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

//    public void set_rule_id(UUID uuid) {
//        rule_id = uuid;
//    }

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
                return EditRuleDetailFragment.create();

            case 1:
                return EditRuleConditionsFragment.create();

            case 2:
                return EditRuleOutcomesFragment.create();

            default:
                throw new IllegalStateException("No fragment at index: " + position);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
