package com.calendate.calendate.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.calendate.calendate.MainActivity;
import com.calendate.calendate.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

/**
 * Created by Hilay on 06/12/2017.
 */

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        String colorPrimaryDark = "#0288d1";
        String primary_light = "#B3E5FC";

        SliderPage page1 = new SliderPage();
        page1.setBgColor(Color.parseColor(primary_light));
        page1.setDescColor(Color.parseColor(colorPrimaryDark));
        page1.setTitleColor(Color.parseColor(colorPrimaryDark));
        page1.setTitle(getString(R.string.intro_views));
        page1.setDescription(getString(R.string.intro_views_desc));
        page1.setImageDrawable(R.drawable.intro_views);
        addSlide(AppIntroFragment.newInstance(page1));

        SliderPage page2 = new SliderPage();
        page2.setBgColor(Color.parseColor(primary_light));
        page2.setDescColor(Color.parseColor(colorPrimaryDark));
        page2.setTitleColor(Color.parseColor(colorPrimaryDark));
        page2.setTitle(getString(R.string.intro_categories));
        page2.setDescription(getString(R.string.intro_categories_desc));
        page2.setImageDrawable(R.drawable.intro_options);
        addSlide(AppIntroFragment.newInstance(page2));

        SliderPage page3 = new SliderPage();
        page3.setBgColor(Color.parseColor(primary_light));
        page3.setDescColor(Color.parseColor(colorPrimaryDark));
        page3.setTitleColor(Color.parseColor(colorPrimaryDark));
        page3.setTitle(getString(R.string.intro_friends));
        page3.setDescription(getString(R.string.intro_friends_desc));
        page3.setImageDrawable(R.drawable.intro_friend);

        addSlide(AppIntroFragment.newInstance(page3));


        // OPTIONAL METHODS
        // Override bar/separator color.
//        setBarColor(Color.parseColor("#3F51B5"));
        setBarColor(Color.parseColor(colorPrimaryDark));
        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        showSkipButton(true);
        setSkipText(getString(R.string.skip));
        setProgressButtonEnabled(true);

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        goToMain();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        goToMain();
    }

    void goToMain(){
        SharedPreferences prefs = getSharedPreferences("intro", MODE_PRIVATE);
        prefs.edit().putInt("firstTime", 1).apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
