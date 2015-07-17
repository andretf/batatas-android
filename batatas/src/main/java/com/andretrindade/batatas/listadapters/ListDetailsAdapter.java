package com.andretrindade.batatas.listadapters;

import android.app.Activity;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andretrindade.batatas.R;
import com.andretrindade.batatas.persistence.BatatasClientProvider;
import com.andretrindade.batatas.persistence.DaggerInjector;
import com.andretrindade.batatas.persistence.ListItem;
import com.andretrindade.batatas.view.TextAwesome;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ListDetailsAdapter extends ArrayAdapter<ListItem> {

    private final Activity context;
    private final int resource;
    private final List<ListItem> items;
    private final List<Integer> selection;
    @Inject
    public BatatasClientProvider.BatatasClient batatas;

    public ListDetailsAdapter(Activity context, List<ListItem> items) {
        super(context, R.layout.layout_list_item_item, items);

        this.context = context;
        this.resource = R.layout.layout_list_item_item;
        this.items = items;
        selection = new ArrayList<>();

        DaggerInjector.bootstrap(this);
    }

    @Override
    public View getView(final int position, View rowView, ViewGroup parent) {

        final ListItem item = items.get(position);

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(resource, null);
            rowView.setTag(new ViewHolder(rowView, item));
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.itemName.setText(item.getName());

        if (item.isBought()) {
            holder.itemBought.setText(R.string.fa_check_square);
            holder.itemName.setPaintFlags(holder.itemBought.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.itemBought.setText(R.string.fa_square_o);
            holder.itemName.setPaintFlags(holder.itemBought.getPaintFlags());
        }

        String formattedAmount = "";
        if (item.getAmount() == Math.round(item.getAmount())) {
            formattedAmount = Integer.toString((int) item.getAmount());
        }
        else {
            formattedAmount = new DecimalFormat("0.000").format(item.getAmount()).replace('.', ',');
        }

        holder.itemAmount.setText(formattedAmount);

        return rowView;
    }

    public void clearSelection() {
        selection.clear();
        notifyDataSetChanged();
    }

    public void addSelection(int position) {
        selection.add(position);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        return selection.contains(position);
    }

    public void removeSelection(int position) {
        // without cast remove(int) instead of remove(Object) is chosen
        selection.remove((Integer) position);
        notifyDataSetChanged();
    }

    public int countSelection() {
        return selection.size();
    }

    public List<ListItem> getSelectedListItems() {
        List<ListItem> result = new ArrayList<>();
        for (Integer integer : selection) {
            result.add(items.get(integer));
        }
        return result;
    }

    public void buyItems(HashMap<String, ListItem> products) {
        // transformed into an Int array to permit manipulation in callbacks
        final Callback<ListItem> emptyCallback = new Callback<ListItem>() {
            @Override
            public void success(ListItem listItem, Response response) {
                notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
            }
        };

        for (ListItem item : items) {
            if (item.getEan_code() != null && products.get(item.getEan_code()) != null) {
                // TODO: check if Amount of items [bought on bill] >= [in Shopping list]
                batatas.buyItem(item.getListId(), item.getId(), emptyCallback);
                item.buy();
            }
            else {
                for(ListItem product : products.values()){
                    if (product.getName().contains(item.getName())) {
                        batatas.buyItem(item.getListId(), item.getId(), emptyCallback);
                        item.buy();
                    }
                }
            }
        }
    }

    public void buyItem_byEanCode(String eanCode) {
        final Callback<ListItem> emptyCallback = new Callback<ListItem>() {
            @Override
            public void success(ListItem listItem, Response response) {
                notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
            }
        };

        if (eanCode != null) {
            for (ListItem item : items) {
                if (eanCode.equals(item.getEan_code())) {
                    batatas.buyItem(item.getListId(), item.getId(), emptyCallback);
                    item.buy();
                }
            }
        }
    }

    class ViewHolder {

        private final ListItem item;

        @InjectView(R.id.button_toggle_bought)
        TextAwesome itemBought;

        @InjectView(R.id.item_name)
        TextView itemName;

        @InjectView(R.id.item_amount)
        TextView itemAmount;

        public ViewHolder(View view, ListItem item) {
            this.item = item;
            ButterKnife.inject(this, view);
        }

        @OnClick(R.id.button_toggle_bought)
        public void onItemBoughtClick() {
            final Callback<ListItem> emptyCallback = new Callback<ListItem>() {
                @Override
                public void success(ListItem listItem, Response response) {
                    notifyDataSetChanged();
                }

                @Override
                public void failure(RetrofitError error) {
                }
            };

            if (item.isBought()) {
                batatas.unbuyItem(item.getListId(), item.getId(), emptyCallback);
                item.unbuy();
            } else {
                batatas.buyItem(item.getListId(), item.getId(), emptyCallback);
                item.buy();
            }
        }
    }
}
