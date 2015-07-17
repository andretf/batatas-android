package com.andretrindade.batatas.persistence;

import java.util.List;


public class User {

    public final String id;
    public final String name;
    public Long list_id;
    public List<Long> lists_ids;
    public Boolean IsShared;

    public User(String _id, String _name) {
        id = _id;
        name = _name;
    }
}
