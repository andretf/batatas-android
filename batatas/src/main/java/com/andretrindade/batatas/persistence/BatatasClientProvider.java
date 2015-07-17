package com.andretrindade.batatas.persistence;

import com.andretrindade.batatas.listadapters.ListDetailsAdapter;
import com.andretrindade.batatas.listadapters.ShareFriendsAdapter;

import java.util.Collection;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

@Module(injects = {
        UserMemory.class,
        ListDetailsAdapter.class,
        ShareFriendsAdapter.class
},
        library = true)
public class BatatasClientProvider {
    private static final String BATATAS_STATING_ENDPOINT = "http://batatas-alfa.herokuapp.com";

    @Provides
    BatatasClient provideBatatasStaging() {
        final RestAdapter restAdapter = new RestAdapter.Builder().setClient(new OkClient()).setEndpoint(BATATAS_STATING_ENDPOINT).build();
        return restAdapter.create(BatatasClient.class);
    }

    public interface BatatasClient {
        @GET("/lists/user/{user_id}")
        void getUserLists(@Path("user_id") String userId,
                          Callback<List<ShoppingList>> callback);

        @POST("/lists/user/{user_id}")
        void addUserList(@Path("user_id") String userId,
                         @Body ShoppingList list,
                         Callback<ShoppingList> callback);

        @GET("/lists/{list_id}")
        void getList(@Path("list_id") Long listId,
                     Callback<ShoppingList> callback);

        @DELETE("/lists/{list_id}")
        void deleteList(@Path("list_id") Long listId,
                        Callback<Void> callback);


        //***** SHARING ***************************************

        @GET("/lists/{list_id}/users")
        void getSharedFriendsIdList(@Path("list_id") Long listId,
                                    Callback<List<String>> callback);

        @POST("/lists/{list_id}/user")
        void shareListFriend(@Path("list_id") Long listId,
                             @Body User user,
                             Callback<String> callback);

        @DELETE("/lists/{list_id}/user/{user_id}")
        void unshareListFriend(@Path("list_id") Long listId,
                               @Path("user_id") String userId,
                               Callback<Void> callback);


        //***** END SHARING ***********************************


        @POST("/lists/{list_id}/items")
        void addItem(@Path("list_id") Long listId,
                     @Body ListItem items,
                     Callback<ListItem> callback);

        @DELETE("/lists/{list_id}/items/{item_id}")
        void deleteItem(@Path("list_id") Long listId,
                        @Path("item_id") Long itemId,
                        Callback<Void> callback);


        @POST("/lists/{list_id}/items/{item_id}/bought")
        void buyItem(@Path("list_id") Long listId,
                     @Path("item_id") Long itemId,
                     Callback<ListItem> callback);

        @DELETE("/lists/{list_id}/items/{item_id}/bought")
        void unbuyItem(@Path("list_id") Long listId,
                       @Path("item_id") Long itemId,
                       Callback<ListItem> callback);


        @POST("/users/create")
        User createUser(@Body User user);


        @POST("/products")
        void updateProductsDb(@Body Collection<ListItem> items,
                              Callback<Void> callback);

        @GET("/products/eancodes/{eancodes}")
        List<Product> getProducts_byEanCodes(@Path("eancodes") String eanCodes);
    }
}
