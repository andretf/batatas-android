package com.andretrindade.batatas;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.andretrindade.batatas.fragments.ShareFriendsFragment;

import static com.andretrindade.batatas.fragments.ShareFriendsFragment.SHARE_FRIENDS_FRAGMENT_TAG;


public class ShareFriendsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_friends);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ShareFriendsFragment(), SHARE_FRIENDS_FRAGMENT_TAG)
                    .commit();
        }
    }
}
