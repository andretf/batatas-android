package com.andretrindade.batatas.fragments;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
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
import com.andretrindade.batatas.ShoppingListActivity;
import com.andretrindade.batatas.helpers.CacheManager;
import com.andretrindade.batatas.helpers.ProductsListManager;
import com.andretrindade.batatas.listadapters.ShoppingListAdapter;
import com.andretrindade.batatas.persistence.BatatasClientProvider;
import com.andretrindade.batatas.persistence.DaggerInjector;
import com.andretrindade.batatas.persistence.ShoppingList;
import com.andretrindade.batatas.persistence.UserMemory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShoppingListsOverviewFragment extends ListFragment {

    public static final String OVERVIEW_FRAGMENT_TAG = "Fragment.ShoppingListsOverviewFragment";
    private static final int PICK_REQUEST_QR_ACTIVITY = 4;

    @Inject
    public UserMemory userMemory;

    @Inject
    public BatatasClientProvider.BatatasClient batatas;

    private ShoppingListAdapter shoppingListAdapter;

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
        inflater.inflate(R.menu.lists_overview, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.lists_overview__menu_new_list:
                DialogFragment newFragment = new AddListFragment();
                newFragment.show(getFragmentManager(), "addList");
                return true;

            case R.id.lists_overview_menu_reload:
                refreshLists();
                return true;

            case R.id.lists_add_from_qr_code:
                startActivityForResult(new Intent(getActivity(), QRBarcodeReadActivity.class), PICK_REQUEST_QR_ACTIVITY);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_REQUEST_QR_ACTIVITY) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                createListFromQR(result);
            }
        }
    }

//**************************************************************************************************

    public void addShoppingList(ShoppingList list) {
        batatas.addUserList(userMemory.UserId, list, new Callback<ShoppingList>() {
            @Override
            public void success(ShoppingList newList, Response response) {
                shoppingListAdapter.add(newList);
                userMemory.setCurrentList(newList);
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println(error.toString());
                Toast.makeText(getActivity(), getString(R.string.err_create_list), Toast.LENGTH_LONG).show();
            }
        });

        try {
            //shoppingListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

//**************************************************************************************************

    private void refreshLists() {
        if (isResumed()) {
            // Show spinner when refreshing
            setListShown(false);
        }

        batatas.getUserLists(userMemory.UserId, new Callback<List<ShoppingList>>() {
            @Override
            public void success(List<ShoppingList> shoppingLists, Response response) {
                if (isResumed()) {
                    shoppingListAdapter = new ShoppingListAdapter(getActivity(), shoppingLists);
                    setListAdapter(shoppingListAdapter);
                    setupListView(getListView(), shoppingListAdapter);
                    setListShown(true);
                }

                try {
                    for (ShoppingList shoppingList : shoppingLists) {
                        CacheManager cacheManager = new CacheManager();
                        cacheManager.addProductsList(shoppingList.allItems());
                    }
                } catch (Exception ex) {
                }
            }

            @Override
            public void failure(RetrofitError error) {
                setListShown(true);
                if ((getActivity().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                    Log.e("", error.getMessage());
                }
                Toast.makeText(getActivity(), getString(R.string.err_fetch_lists), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createListFromQR(String result) {
        final String url = result;
        final String[] msg = {""};

        Thread thread = new Thread() {
            public void run() {
                ProductsListManager productsManager = new ProductsListManager(batatas);
                try {
                    productsManager.SetListFromQRCode(url);

                    if (productsManager.getItems().size() > 0) {
                        productsManager.UpdateCache();
                        //productsManager.ImproveOnlineDatabase();

                        @SuppressLint("SimpleDateFormat")
                        String now = (new SimpleDateFormat("dd/MM/yy HH:mm")).format(new Date());

                        ShoppingList shoppingList = new ShoppingList("Compras " + now);
                        shoppingList.addAllItems(productsManager.getItems().values());

                        addShoppingList(shoppingList);
                        refreshLists();
                    } else {
                        msg[0] = getString(R.string.info_no_product_found);
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
            //refreshLists();

        } catch (Exception e) {
        }
    }

    private void setupListView(final ListView listsOverview, final ShoppingListAdapter adapter) {
        listsOverview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listsOverview.setMultiChoiceModeListener(new MyMultiChoiceModeListener(adapter));
        listsOverview.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                        listsOverview.setItemChecked(position, !adapter.isPositionChecked(position));
                        return false;
                    }
                });

        listsOverview.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final ShoppingList shoppingList = shoppingListAdapter.getShoppingList(position);
                        final Intent intent = new Intent(getActivity(), ShoppingListActivity.class);

                        userMemory.setCurrentList(shoppingList);
                        startActivity(intent);
                    }
                }
        );
    }

    private class MyMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
        private final ShoppingListAdapter adapter;

        public MyMultiChoiceModeListener(ShoppingListAdapter adapter) {
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
            getActivity().getMenuInflater().inflate(R.menu.contextual_shoppinglist_actionbar, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.item_delete:
                    deleteList(actionMode, adapter.getSelectedShoppingLists());
                    return true;
            }
            return false;
        }

        private void deleteList(final ActionMode actionMode, List<ShoppingList> shoppingLists) {
            for (ShoppingList shoppingList : shoppingLists) {
                batatas.deleteList(shoppingList.getId(), new Callback<Void>() {
                    @Override
                    public void success(Void v, Response response) {
                        Toast.makeText(getActivity(), getString(R.string.suc_deleted_list), Toast.LENGTH_LONG).show();
                        adapter.clearSelection();
                        actionMode.finish();
                        refreshLists();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getActivity(), getString(R.string.err_deleted), Toast.LENGTH_LONG).show();
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
