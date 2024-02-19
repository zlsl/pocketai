package ru.zlsl.pocketai.pagers;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import ru.zlsl.pocketai.App;
import ru.zlsl.pocketai.R;


public class MainPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragments;

    public MainPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments = fragments;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return App.getContext().getResources().getString(R.string.tab_dialogs);
            case 1:
                return App.getContext().getResources().getString(R.string.tab_characters);
            case 2:
                return App.getContext().getResources().getString(R.string.tab_templates);
            case 3:
                return App.getContext().getResources().getString(R.string.tab_model_parameters);
            case 4:
                return App.getContext().getResources().getString(R.string.tab_settings);
            default:
                return "";
        }
    }
}