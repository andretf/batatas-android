package com.andretrindade.batatas.helpers;

import android.text.TextUtils;

import com.andretrindade.batatas.persistence.BatatasClientProvider;
import com.andretrindade.batatas.persistence.Product;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;


public class Finder {
    @Inject
    public BatatasClientProvider.BatatasClient batatas;

    public Finder(BatatasClientProvider.BatatasClient _batatas) {
        batatas = _batatas;
    }

    // Return products array for each matching eanCode
    public HashMap<String, List<String>> FindByEanCodes(List<String> values) {
        HashMap<String, List<String>> cache = Utils.readProductsFromCsv();
        final HashMap<String, List<String>> result = new HashMap<>();
        final List<String> eanCodes = values;

        // Find in cache
        List<String> matchProductsNames;
        for (String eanCode : eanCodes) {
            if ((matchProductsNames = cache.get(eanCode)) != null) {
                Utils.addItemToHash(result, eanCode, matchProductsNames);
                eanCodes.remove(eanCode);
            }
        }

        // Find in online database
        if (result.size() < eanCodes.size()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Product> matchingProducts = batatas.getProducts_byEanCodes(TextUtils.join(",", eanCodes.toArray()));

                        // Schedule to remove because it's possible N products : 1 eanCode
                        // and we want to avoid getting just first occurrence

                        for (Product product : matchingProducts) {
                            if (eanCodes.contains(product.getEanCode()) && product.getEanCode() != null) {
                                Utils.addItemToHash(result, product.getEanCode(), product.getName());
                                eanCodes.set(eanCodes.indexOf(product.getEanCode()), null);
                                (new CacheManager()).addProducts(product.getEanCode(), product.getName());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }

        return result;
    }

// --Commented out by Inspection START (5/24/15 4:08 AM):
//    public List<ListItem> FindByName(String[] values) {
//        cache = Utils.readProductsFromCsv();
//
//        // TODO: Use simmetrics to match by names
//
//        return new ArrayList<ListItem>();
//    }
// --Commented out by Inspection STOP (5/24/15 4:08 AM)
}
