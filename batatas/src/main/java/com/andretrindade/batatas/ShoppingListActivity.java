package com.andretrindade.batatas;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;

import com.andretrindade.batatas.fragments.AddItemFragment;
import com.andretrindade.batatas.fragments.ShoppingListDetailsFragment;
import com.andretrindade.batatas.persistence.ListItem;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.andretrindade.batatas.fragments.ShoppingListDetailsFragment.LIST_DETAIL_FRAGMENT_TAG;

public class ShoppingListActivity extends ActionBarActivity implements AddItemFragment.OnAddItemDialogListener {
    @InjectView(R.id.new_item_ean_code)
    public EditText eanCode;

    @InjectView(R.id.new_item_name)
    public EditText itemName;

    @InjectView(R.id.new_item_amount)
    public EditText amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ShoppingListDetailsFragment(), LIST_DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onDialogPositiveClick(Dialog dialog) {
        ButterKnife.inject(this, dialog);

        String _eanCode = eanCode.getText().toString();
        String _itemName = itemName.getText().toString();
        int _amount = Integer.parseInt(amount.getText().toString());

        ListItem listItem = new ListItem(_itemName, _amount, _eanCode);

        ShoppingListDetailsFragment listDetailsFragment =
                (ShoppingListDetailsFragment) getFragmentManager().findFragmentByTag(LIST_DETAIL_FRAGMENT_TAG);

        listDetailsFragment.addItemToList(listItem);
    }

    @Override
    public void onDialogNegativeClick(Dialog dialog) {
        dialog.cancel();
    }
}
