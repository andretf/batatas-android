package com.andretrindade.batatas.helpers;


import com.andretrindade.batatas.persistence.ListItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheManager {
    private HashMap<String, List<String>> cache;

    public CacheManager() {
        cache = Utils.readProductsFromCsv();
    }

    public void addProducts(String eanCode, String name) {
        Utils.addItemToHash(cache, eanCode, name);
        Utils.saveProductsToCsv(cache);
    }

    public void addProductsList(Collection<ListItem> products) {
        for (ListItem item : products) {
            Utils.addItemToHash(cache, item.getEan_code(), item.getName());
        }
        Utils.saveProductsToCsv(cache);
    }

    public List<ListItem> getAllProducts() {
        List<ListItem> result = new ArrayList<>();

        if (cache != null) {
            for (Map.Entry<String, List<String>> entry : cache.entrySet()) {
                String eanCode = entry.getKey();
                for (String productName : entry.getValue()) {
                    ListItem item = new ListItem(productName, 1, eanCode);
                    result.add(item);
                }
            }
        }

        return result;
    }
}
