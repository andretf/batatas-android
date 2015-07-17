package com.andretrindade.batatas;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;

import com.andretrindade.batatas.fragments.AddListFragment;
import com.andretrindade.batatas.fragments.ShoppingListsOverviewFragment;
import com.andretrindade.batatas.persistence.ShoppingList;
import com.facebook.appevents.AppEventsLogger;

import static com.andretrindade.batatas.fragments.ShoppingListsOverviewFragment.OVERVIEW_FRAGMENT_TAG;

public class ListsOverviewActivity extends ActionBarActivity implements AddListFragment.AddListDialogListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists_overview);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ShoppingListsOverviewFragment(), OVERVIEW_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onDialogPositiveClick(Dialog dialog) {

        EditText listName = (EditText) dialog.findViewById(R.id.new_list_name);
        final ShoppingList shoppingList = new ShoppingList(listName.getText().toString());

        final ShoppingListsOverviewFragment listsOverviewFragment =
                (ShoppingListsOverviewFragment) getFragmentManager().findFragmentByTag(OVERVIEW_FRAGMENT_TAG);

        listsOverviewFragment.addShoppingList(shoppingList);
    }

    @Override
    public void onDialogNegativeClick(Dialog dialog) {
        dialog.cancel();
    }
}
