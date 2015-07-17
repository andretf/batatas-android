package com.andretrindade.batatas.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andretrindade.batatas.R;
import com.andretrindade.batatas.listadapters.ShareFriendsAdapter;
import com.andretrindade.batatas.persistence.BatatasClientProvider;
import com.andretrindade.batatas.persistence.DaggerInjector;
import com.andretrindade.batatas.persistence.User;
import com.andretrindade.batatas.persistence.UserMemory;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShareFriendsFragment extends ListFragment {

    public static final String SHARE_FRIENDS_FRAGMENT_TAG = "Fragment.ShareFriendsFragment";

    @Inject
    public BatatasClientProvider.BatatasClient batatas;

    @Inject
    public UserMemory userMemory;

    private ShareFriendsAdapter shareFriendsAdapter;

    //**********************************************************************************************
    @Override
    public void onResume() {
        super.onResume();
        refreshFriendsList();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DaggerInjector.bootstrap(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    //**********************************************************************************************

    private Long getCurrentListId() {
        return userMemory.getCurrentList().getId();
    }

    private void refreshFriendsList() {
        if (isResumed()) {
            setListShown(false);
        }

        userMemory.FriendsList = getFriendsList();

        batatas.getSharedFriendsIdList(getCurrentListId(), new Callback<List<String>>() {
            @Override
            public void success(List<String> sharedFriends, Response response) {
                sharedFriends.remove(getCurrentListId().toString());
                for (User user : userMemory.FriendsList) {
                    user.IsShared = sharedFriends.contains(user.id);
                    user.list_id = getCurrentListId();
                }

                shareFriendsAdapter = new ShareFriendsAdapter(getActivity(), userMemory.FriendsList);
                setListAdapter(shareFriendsAdapter);
                setListShown(true);
            }

            @Override
            public void failure(RetrofitError error) {
                setListShown(true);
                Toast.makeText(getActivity(), getString(R.string.err_fetch_shared_list), Toast.LENGTH_LONG).show();
            }
        });
    }


    private List<User> getFriendsList() {
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        final List<User> result = new ArrayList<>();

        if (accessToken != null) {
            final GraphRequest.GraphJSONArrayCallback friendsCallback = new GraphRequest.GraphJSONArrayCallback() {
                @Override
                public void onCompleted(JSONArray users, GraphResponse graphResponse) {
                    try {
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject jsonUser = (JSONObject) users.get(i);
                            User user = new User(jsonUser.get("id").toString(),
                                                 jsonUser.get("name").toString());
                            result.add(user);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            final Thread thread = new Thread() {
                public void run() {
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id, name, email");

                    GraphRequest request = new GraphRequest().newMyFriendsRequest(accessToken, friendsCallback);
                    request.setGraphPath("/me/friends");
                    request.setParameters(parameters);
                    request.executeAndWait();
                }
            };
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
