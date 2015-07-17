package com.andretrindade.batatas.persistence;

import com.andretrindade.batatas.fragments.MainFragment;
import com.andretrindade.batatas.fragments.ShareFriendsFragment;
import com.andretrindade.batatas.fragments.ShoppingListDetailsFragment;
import com.andretrindade.batatas.fragments.ShoppingListsOverviewFragment;
import com.andretrindade.batatas.listadapters.ShareFriendsAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(injects = {
        ShoppingListsOverviewFragment.class,
        ShoppingListDetailsFragment.class,
        MainFragment.class,
        ShareFriendsFragment.class,
        ShareFriendsAdapter.class
}, complete = false)
class UserMemoryProvider {

    @Provides
    @Singleton
    UserMemory provideMemory() {
        return new UserMemory();
    }
}
