package com.andretrindade.batatas.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShoppingList implements Serializable {

    private final String name;
    private final List<ListItem> items = new ArrayList<>();
    private Long id;

    public ShoppingList(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void addItem(ListItem item) {
        item.setListId(id);
        items.add(item);
    }

    public void addAllItems(Collection<ListItem> _items) {
        items.addAll(_items);
    }

    public List<ListItem> allItems() {
        return items;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        if (items.isEmpty()) {
            return "Ainda não há produtos.";
        } else {
            return shortListSummary();
        }
    }

    private String shortListSummary() {
        String summary = String.format("%s produtos: ", items.size());

        for (ListItem item : items) {
            summary += String.format("%s, ", item.getName());
            if (summary.length() > 50){
                break;
            }
        }
        if (summary.length() > 2) summary = summary.substring(0, summary.length()-2);
        if (summary.length() > 48) summary+= "..";

        summary += ".";
        return summary;
    }
}
