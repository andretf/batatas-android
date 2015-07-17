package com.andretrindade.batatas.persistence;

import java.io.Serializable;

public class Product implements Serializable {

    private Integer id;
    private String name;
    private String ean_code;

    public String getName() {
        return name;
    }

    public String getEanCode() {
        return ean_code;
    }

}
