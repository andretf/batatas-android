package com.andretrindade.batatas.listadapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.andretrindade.batatas.helpers.CacheManager;
import com.andretrindade.batatas.persistence.ListItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class AutoCompleteAdapter extends ArrayAdapter<ListItem> implements Filterable {
    private final ArrayList<ListItem> data;
    private ArrayList<ListItem> filtered_data;

    public AutoCompleteAdapter(Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        CacheManager cacheManager = new CacheManager();
        data = new ArrayList<>();
        data.addAll(cacheManager.getAllProducts());
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ListItem getItem(int index) {
        if (index < filtered_data.size()) {
            return filtered_data.get(index);
        }
        return null;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        return createTextViewAsItem(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createTextViewAsItem(position);
    }

    private TextView createTextViewAsItem(int position) {
        TextView label = new TextView(getContext());
        String value = "";

        if (filtered_data != null &&
            filtered_data.size() > 0 &&
            position >= 0 &&
            position < filtered_data.size() - 1)
        {
            value = filtered_data.get(position).getName();
        }

        label.setText(value);
        label.setPadding(25, 25, 25, 25);

        return label;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint == null) {
                    filtered_data = data;
                    filterResults.values = data;
                    filterResults.count = data.size();

                    Collections.sort(data, new Comparator<ListItem>() {
                        @Override
                        public int compare(ListItem item1, ListItem item2) {
                            return item1.getName().compareTo(item2.getName());
                        }
                    });
                }
                else {
                    ArrayList<ListItem> suggestions = new ArrayList<>();

                    if (data.size() > 0){
                        for (ListItem item : data) {
                            for(String namePart : Arrays.asList(item.getName().toLowerCase(Locale.ENGLISH).split("[ /-]"))){
                                if (namePart.startsWith(constraint.toString().toLowerCase(Locale.ENGLISH))) {
                                    suggestions.add(item);
                                    break;
                                }
                            }
                        }
                    }

                    final String filterText = constraint.toString().toLowerCase(Locale.ENGLISH);
                    Collections.sort(suggestions, new Comparator<ListItem>() {
                        @Override
                        public int compare(ListItem item1, ListItem item2) {
                            int index1 = -1, index2 = -1;
                            int i = -1;

                            for(String namePart : Arrays.asList(item1.getName().toLowerCase(Locale.ENGLISH).split("[ /-]"))){
                                i++;
                                if (namePart.startsWith(filterText)) {
                                    index1 = i;
                                    break;
                                }
                            }

                            i = -1;
                            for(String namePart : Arrays.asList(item2.getName().toLowerCase(Locale.ENGLISH).split("[ /-]"))){
                                i++;
                                if (namePart.startsWith(filterText)) {
                                    index2 = i;
                                    break;
                                }
                            }

                            return index1 - index2;
                        }
                    });

                    filtered_data = suggestions;
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                }
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();

                if (results != null && results.count > 0) {
                    ArrayList<ListItem> newValues = (ArrayList<ListItem>) results.values;
                    addAll(newValues);
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}

