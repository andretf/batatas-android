package com.andretrindade.batatas.fragments;

import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.andretrindade.batatas.QRBarcodeReadActivity;
import com.andretrindade.batatas.R;
import com.andretrindade.batatas.ShareFriendsActivity;
import com.andretrindade.batatas.helpers.ProductsListManager;
import com.andretrindade.batatas.listadapters.ListDetailsAdapter;
import com.andretrindade.batatas.persistence.BatatasClientProvider;
import com.andretrindade.batatas.persistence.DaggerInjector;
import com.andretrindade.batatas.persistence.ListItem;
import com.andretrindade.batatas.persistence.ShoppingList;
import com.andretrindade.batatas.persistence.UserMemory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShoppingListDetailsFragment extends ListFragment {

    public static final String LIST_DETAIL_FRAGMENT_TAG = "Fragment.ShoppingListDetailsFragment";
    private static final int PICK_REQUEST_QR_ACTIVITY = 1;
    private static final int PICK_REQUEST_BARCODE_ACTIVITY = 3;

    @Inject
    public BatatasClientProvider.BatatasClient batatas;

    @Inject
    public UserMemory userMemory;

    private ListDetailsAdapter listDetailsAdapter;


    //**************************************************************************************************
    @Override
    public void onResume() {
        super.onResume();
        refreshLists();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DaggerInjector.bootstrap(this);
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.shopping_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.shopping_list_menu_new_item:
                DialogFragment newFragment = new AddItemFragment();
                newFragment.show(getFragmentManager(), "addItem");
                return true;

            case R.id.shopping_list_menu_share:
                startActivity(new Intent(getActivity(), ShareFriendsActivity.class));
                return true;

            case R.id.shopping_list_menu_reload:
                refreshLists();
                return true;

            case R.id.shopping_list_check_bill:
                startActivityForResult(new Intent(getActivity(), QRBarcodeReadActivity.class), PICK_REQUEST_QR_ACTIVITY);
                return true;

            case R.id.shopping_list_check_product:
                startActivityForResult(new Intent(getActivity(), QRBarcodeReadActivity.class), PICK_REQUEST_BARCODE_ACTIVITY);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_REQUEST_QR_ACTIVITY) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                updateCheckList(result);
            }
        }
        if (requestCode == PICK_REQUEST_BARCODE_ACTIVITY) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                listDetailsAdapter.buyItem_byEanCode(result);
                listDetailsAdapter.notifyDataSetChanged();
            }
        }
    }


//**************************************************************************************************

    public void addItemToList(ListItem listItem) {
        listItem.setListId(getCurrentListId());

        batatas.addItem(getCurrentListId(), listItem, new Callback<ListItem>() {
            @Override
            public void success(ListItem listItem, Response response) {
                refreshLists();
                //Toast.makeText(getActivity(), "Item added with success!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), getString(R.string.err_add_item), Toast.LENGTH_LONG).show();
            }
        });

        listDetailsAdapter.notifyDataSetChanged();
    }

//**************************************************************************************************

    private Long getCurrentListId() {
        try {
            return userMemory.getCurrentList().getId();
        } catch (Exception ex) {
            return userMemory.getCurrentList().getId();
        }
    }

    private void refreshLists() {
        if (isResumed()) {
            // Show spinner when refreshing
            setListShown(false);
        }

        batatas.getList(getCurrentListId(), new Callback<ShoppingList>() {
            @Override
            public void success(ShoppingList shoppingList, Response response) {
                listDetailsAdapter = new ListDetailsAdapter(
                        getActivity(),
                        shoppingList.allItems());

                setListAdapter(listDetailsAdapter);
                setupListView(getListView(), listDetailsAdapter);
                setListShown(true);
            }

            @Override
            public void failure(RetrofitError error) {
                setListShown(true);
                Toast.makeText(getActivity(), getString(R.string.err_fetch_list), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupListView(final ListView listDetails, final ListDetailsAdapter adapter) {
        listDetails.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listDetails.setMultiChoiceModeListener(new MyMultiChoiceModeListener(adapter));
        listDetails.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                        listDetails.setItemChecked(position, !adapter.isPositionChecked(position));
                        return false;
                    }
                });
    }

    private void updateCheckList(String result) {
        final String url = result;
        final String[] msg = {"", ""};
        @SuppressWarnings("unchecked")
        final HashMap<String, ListItem>[] items = new HashMap[]{new HashMap<>()};

        final Thread thread = new Thread() {
            public void run() {
                ProductsListManager productsManager = new ProductsListManager(batatas);
                try {
                    productsManager.SetListFromQRCode(url);

                    if (productsManager.getItems().size() > 0) {
                        items[0] = productsManager.getItems();
                        productsManager.UpdateCache();
                        productsManager.ImproveOnlineDatabase();
                    } else {
                        msg[0] = getString(R.string.err_no_matching_items);
                    }
                } catch (IOException e) {
                    msg[0] = e.getMessage();
                }
            }
        };
        thread.start();
        try {
            thread.join();
            if (!msg[0].equals("")) {
                Toast.makeText(getActivity(), msg[0], Toast.LENGTH_LONG).show();
            }
            else {
                listDetailsAdapter.buyItems(items[0]);
            }
            Thread.sleep(1000);
            refreshLists();
        } catch (Exception e) {
        }
    }


//**************************************************************************************************

    private class MyMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
        private final ListDetailsAdapter adapter;


        public MyMultiChoiceModeListener(ListDetailsAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            adapter.clearSelection();
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            getActivity().getMenuInflater().inflate(R.menu.contextual_listdetails_actionbar, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.item_delete:
                    deleteList(actionMode, adapter.getSelectedListItems());
                    return true;
            }
            return false;
        }

        private void deleteList(final ActionMode actionMode, List<ListItem> items) {
            for (ListItem item : items) {
                batatas.deleteItem(getCurrentListId(), item.getId(), new Callback<Void>() {
                    @Override
                    public void success(Void v, Response response) {
                        Toast.makeText(getActivity(), getString(R.string.suc_deleted_item), Toast.LENGTH_LONG).show();

                        adapter.clearSelection();
                        actionMode.finish();
                        refreshLists();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getActivity(), getString(R.string.suc_deleted_item), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            if (checked) {
                adapter.addSelection(position);
            } else {
                adapter.removeSelection(position);
            }

            mode.setTitle(adapter.countSelection() + getString(R.string._selected));
        }
    }

}
