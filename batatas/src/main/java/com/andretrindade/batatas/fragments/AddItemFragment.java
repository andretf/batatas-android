package com.andretrindade.batatas.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.andretrindade.batatas.QRBarcodeReadActivity;
import com.andretrindade.batatas.R;
import com.andretrindade.batatas.helpers.Finder;
import com.andretrindade.batatas.listadapters.AutoCompleteAdapter;
import com.andretrindade.batatas.persistence.ListItem;

import java.util.Collections;
import java.util.List;

public class AddItemFragment extends DialogFragment {
    private static final int PICK_FIND_PRODUCTS = 2;
    private OnAddItemDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.fragment_add_item, null);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(AddItemFragment.this.getDialog());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(AddItemFragment.this.getDialog());
                    }
                });

        Button scanButton = (Button) view.findViewById(R.id.scan_new_item_barcode);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), QRBarcodeReadActivity.class), PICK_FIND_PRODUCTS);
            }
        });

        AutoCompleteAdapter adapter = new AutoCompleteAdapter(getActivity());
        final AutoCompleteTextView new_item_autocomplete = (AutoCompleteTextView) view.findViewById(R.id.new_item_name);
        new_item_autocomplete.setAdapter(adapter);
        new_item_autocomplete.setThreshold(1);
        new_item_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                EditText new_item_ean_code = (EditText) view.findViewById(R.id.new_item_ean_code);
                EditText new_item_amount = (EditText) view.findViewById(R.id.new_item_amount);
                ListItem item = (ListItem) arg0.getAdapter().getItem(arg2);

                new_item_autocomplete.setText(item.getName());
                new_item_ean_code.setText(item.getEan_code());
                new_item_amount.setText("1");
            }
        });

        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FIND_PRODUCTS) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                addItemFromBarcode(result);
            }
        }
    } //onActivityResult

    private void addItemFromBarcode(String eanCode) {
        Dialog ctx = getDialog();
        Finder finder = new Finder(((ShoppingListDetailsFragment) getFragmentManager().findFragmentByTag("Fragment.ShoppingListDetailsFragment")).batatas);
        List<String> eanCodeList = Collections.singletonList(eanCode);

        List<String> matchingProducts = finder.FindByEanCodes(eanCodeList).get(eanCode);
        if (matchingProducts != null && matchingProducts.size() > 0) {
            ((EditText) ctx.findViewById(R.id.new_item_amount)).setText("1");
            ((EditText) ctx.findViewById(R.id.new_item_ean_code)).setText(eanCode);
            ((EditText) ctx.findViewById(R.id.new_item_name)).setText(matchingProducts.get(0));
        } else {
            Toast.makeText(getActivity().getBaseContext(), getString(R.string.err_find_product), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (OnAddItemDialogListener) activity;
    }

    public interface OnAddItemDialogListener {
        void onDialogPositiveClick(Dialog dialog);

        void onDialogNegativeClick(Dialog dialog);
    }
}
