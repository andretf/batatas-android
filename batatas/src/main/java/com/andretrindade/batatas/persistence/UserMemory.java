package com.andretrindade.batatas.persistence;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserMemory {

    private static final String SHARED_PREFERENCES = "com.andretrindade.batatas.shoppingLists";
    public String UserId;
    public List<User> FriendsList;
    @Inject
    BatatasClientProvider.BatatasClient batatas;
    private ShoppingList currentList;

    UserMemory() {
        DaggerInjector.bootstrap(this);
    }

    public ShoppingList getCurrentList() {
        return currentList;
    }

    public void setCurrentList(ShoppingList currentList) {
        this.currentList = currentList;
    }
}
