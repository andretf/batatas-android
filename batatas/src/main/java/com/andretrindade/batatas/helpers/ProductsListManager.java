package com.andretrindade.batatas.helpers;

import com.andretrindade.batatas.persistence.BatatasClientProvider;
import com.andretrindade.batatas.persistence.ListItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProductsListManager {

    @Inject
    public BatatasClientProvider.BatatasClient batatas;
    private HashMap<String, ListItem> items;

    public ProductsListManager(BatatasClientProvider.BatatasClient _batatas) {
        batatas = _batatas;
    }

    public HashMap<String, ListItem> getItems() {
        return items;
    }

    public void SetListFromQRCode(String result) throws IOException {
        HashMap<String, ListItem> boughtProducts = new HashMap<>();

        if (result != null) {
            // Replace to access directly inner iframe page with relevant content
            String HTTP_SEFAZ_WEBSITE = "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx";
            String HTTP_SEFAZ_IFRAME = "https://www.sefaz.rs.gov.br/ASP/AAE_ROOT/NFE/SAT-WEB-NFE-NFC_QRCODE_1.asp";
            final String url = result.replace(HTTP_SEFAZ_WEBSITE, HTTP_SEFAZ_IFRAME);

            try {
                Document doc = Jsoup.connect(url).get();
                Elements products = doc.select("[id^=Item]");

                for (Element product : products) {
                    String eanCode = product.child(0).text();
                    String name = product.child(1).text();
                    float quantity = getLocaleInt(product.child(2).text());
                    float floatAmount = getLocaleInt(product.child(4).text());

                    ListItem item = new ListItem(name, quantity, eanCode);
                    if (!boughtProducts.containsKey(eanCode)) {
                        boughtProducts.put(eanCode, item);
                    }
                }

                //int a = 1; /// for debug proposes
            } catch (IOException e) {
                e.printStackTrace();
                String exc_get_bill_products = "Falha ao obter produtos da nota fiscal";
                throw new IOException(exc_get_bill_products);
            }
        }

        items = boughtProducts;
    }


    public void UpdateCache() {
        (new CacheManager()).addProductsList(items.values());
    }


    // Helping application...
    // TODO: add a config user setting for allowing this
    // TODO: This should run silently (empty callbacks), but maybe create a log
    public void ImproveOnlineDatabase() {
        batatas.updateProductsDb(items.values(), new Callback<Void>() {
            @Override
            public void success(Void v, Response response) {
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    private float getLocaleInt(String value) {
        value = value.replace('.', ' ').replace(',', '.');
        return Float.parseFloat(value);
    }

}
