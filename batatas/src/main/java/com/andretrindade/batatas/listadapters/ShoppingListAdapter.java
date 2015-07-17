package com.andretrindade.batatas.listadapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andretrindade.batatas.R;
import com.andretrindade.batatas.persistence.ShoppingList;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ShoppingListAdapter extends ArrayAdapter<ShoppingList> {

    private final Activity context;
    private final int resource;
    private final List<ShoppingList> shoppingLists;
    private final List<Integer> selection;

    public ShoppingListAdapter(Activity context, List<ShoppingList> shoppingLists) {
        super(context, R.layout.layout_list_overview_item, shoppingLists);

        this.context = context;
        this.resource = R.layout.layout_list_overview_item;
        this.shoppingLists = shoppingLists;
        this.selection = new ArrayList<>();
    }

    @Override
    public View getView(final int position, View rowView, ViewGroup parent) {
        ShoppingList shoppingList = shoppingLists.get(position);

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(resource, null);
            rowView.setTag(new ViewHolder(rowView));
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.listName.setText(shoppingList.getName());
        holder.listSummary.setText(shoppingList.getSummary());
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

    public List<ShoppingList> getSelectedShoppingLists() {
        List<ShoppingList> result = new ArrayList<>();
        for (Integer integer : selection) {
            result.add(shoppingLists.get(integer));
        }
        return result;
    }

    public ShoppingList getShoppingList(int position) {
        return shoppingLists.get(position);
    }

    class ViewHolder {
        @InjectView(R.id.list_name)
        TextView listName;

        @InjectView(R.id.list_summary)
        TextView listSummary;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
