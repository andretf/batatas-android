package com.andretrindade.batatas.persistence;

import java.io.Serializable;

public class ListItem implements Serializable {

    private final String name;
    private final float amount;
    private Long id;
    private Boolean bought;
    private String ean_code;

    private Long listId;

    public ListItem(String _name, float _amount, String _eanCode) {
        this.name = _name;
        this.amount = _amount;
        this.ean_code = _eanCode;
        this.bought = false;
    }

    public Long getId() {
        return id;
    }

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public String getName() {
        return name;
    }

    public float getAmount() {
        return amount;
    }

    public String getEan_code() {
        return ean_code;
    }

    public Boolean isBought() {
        return bought;
    }

    public void buy() {
        bought = true;
    }

    public void unbuy() {
        bought = false;
    }
}
