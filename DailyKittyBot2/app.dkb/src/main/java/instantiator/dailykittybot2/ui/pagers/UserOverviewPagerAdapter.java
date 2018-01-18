package instantiator.dailykittybot2.ui.pagers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.ui.fragments.UserRecommendationsFragment;
import instantiator.dailykittybot2.ui.fragments.UserRulesFragment;

public class UserOverviewPagerAdapter extends FragmentPagerAdapter {

    private String username;
    private Context context;

    public UserOverviewPagerAdapter(Context context, FragmentManager fm, String username) {
        super(fm);
        this.context = context;
        this.username = username;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.tab_rules);
            case 1:
                return context.getString(R.string.tab_recommendations);
            default:
                throw new IllegalStateException("No fragment at index: " + position);
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UserRulesFragment.create();

            case 1:
                return UserRecommendationsFragment.create();

            default:
                throw new IllegalStateException("No fragment at index: " + position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
