package com.andretrindade.batatas;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.andretrindade.batatas.fragments.MainFragment;

import static com.andretrindade.batatas.fragments.MainFragment.MAIN_FRAGMENT_TAG;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment(), MAIN_FRAGMENT_TAG)
                    .commit();
        }
    }
}
