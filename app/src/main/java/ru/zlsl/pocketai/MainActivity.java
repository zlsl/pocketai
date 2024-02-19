package ru.zlsl.pocketai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

import ru.zlsl.pocketai.databinding.ActivityMainBinding;
import ru.zlsl.pocketai.pagers.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbar);

        binding.myTabLayout.addTab(binding.myTabLayout.newTab().setText(getResources().getString(R.string.tab_dialogs)));
        binding.myTabLayout.addTab(binding.myTabLayout.newTab().setText(getResources().getString(R.string.tab_characters)));
        binding.myTabLayout.addTab(binding.myTabLayout.newTab().setText(getResources().getString(R.string.tab_templates)));
        binding.myTabLayout.addTab(binding.myTabLayout.newTab().setText(getResources().getString(R.string.tab_model_parameters)));
        binding.myTabLayout.addTab(binding.myTabLayout.newTab().setText(getResources().getString(R.string.tab_settings)));

        List<Fragment> fragments = new Vector<>();
        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment_chats = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentChats.class.getName());
        fragments.add(fragment_chats);

        Fragment fragment_characters = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentCharacters.class.getName());
        fragments.add(fragment_characters);

        Fragment fragment_templates = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentTemplates.class.getName());
        fragments.add(fragment_templates);

        Fragment fragment_parameters = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentParameters.class.getName());
        fragments.add(fragment_parameters);

        Fragment fragment_settings = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentSettings.class.getName());
        fragments.add(fragment_settings);

        binding.myViewPager.setOffscreenPageLimit(10);
        binding.myViewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), fragments));
        binding.myTabLayout.setupWithViewPager(binding.myViewPager);
        binding.myTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        binding.fab.setOnClickListener(v -> {
            try {
                switch (binding.myTabLayout.getSelectedTabPosition()) {
                    case 1:
//                        Intent intent = new Intent(App.getContext(), EditCharacterActivity.class);
//                        startActivity(intent);
                        break;
                    case 2:
                        Intent intent2 = new Intent(App.getContext(), EditTemplateActivity.class);
                        startActivity(intent2);
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.myTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if ((tab.getPosition() == 1) || (tab.getPosition() == 2)) {
                    binding.fab.show();
                } else {
                    binding.fab.hide();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.myViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if ((position == 1) || (position == 2)) {
                    binding.fab.show();
                } else {
                    binding.fab.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.fab.hide();
        InitTemplates();
    }

    private void InitTemplates() {
        InitTemplate("test.pai");
        InitTemplate("test_en.pai");
    }

    public void ShowChats() {
        binding.myTabLayout.selectTab(binding.myTabLayout.getTabAt(0));
    }

    private void InitTemplate(String filename) {
        if (getBaseContext().getFileStreamPath(filename).exists()) {
            Log.i("Template exists", filename);
        }
        try {
            FileWriter out = new FileWriter(new File(App.getContext().getFilesDir(), filename));
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(filename), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                out.write(line + "\n");
            }
            reader.close();
            out.close();
            Log.i("Template init", filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


}