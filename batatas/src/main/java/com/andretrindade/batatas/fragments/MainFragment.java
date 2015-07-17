package com.andretrindade.batatas.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andretrindade.batatas.ListsOverviewActivity;
import com.andretrindade.batatas.R;
import com.andretrindade.batatas.persistence.BatatasClientProvider;
import com.andretrindade.batatas.persistence.DaggerInjector;
import com.andretrindade.batatas.persistence.User;
import com.andretrindade.batatas.persistence.UserMemory;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class MainFragment extends Fragment {

    public static final String MAIN_FRAGMENT_TAG = "Fragment.MainFragment";

    private final List<String> permissions;
    private CallbackManager mCallbackManager;
    private ProfileTracker profileTracker;

    @Inject
    public BatatasClientProvider.BatatasClient batatas;

    @Inject
    public UserMemory userMemory;

    public MainFragment() {
        permissions = Arrays.asList("user_friends", "public_profile", "email");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                final Profile profile = currentProfile;
                if (profile != null) {
                    final Thread thread = new Thread() {
                        public void run() {
                            createUserFromFacebook(profile);
                            openListsOverview();
                        }
                    };
                    thread.start();
                }
            }
        };

        printKeyHash(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DaggerInjector.bootstrap(this);
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired()) {
            userMemory.UserId = Profile.getCurrentProfile().getId();
            openListsOverview();
        }
        else {
            final TextView text_details = (TextView) view.findViewById(R.id.text_details);
            text_details.setText(getString(R.string.intro_welcome));

            LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);

            loginButton.setFragment(this);
            loginButton.setReadPermissions(permissions);
            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    text_details.setText("");
                }

                @Override
                public void onCancel() {
                    ((TextView) getActivity().findViewById(R.id.text_details)).setText(getString(R.string.info_login_canceled));
                    //showError("Login canceled.");
                    if ((getActivity().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                        Log.v("LoginActivity", "cancel");
                    }
                }

                @Override
                public void onError(FacebookException e) {
                    ((TextView) getActivity().findViewById(R.id.text_details)).setText(e.getMessage());
                    //showError("Failed to login!");
                    if ((getActivity().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                        Log.v("LoginActivity", e.getCause().toString());
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void createUserFromFacebook(Profile profile) {
        User user = new User(profile.getId(), profile.getName());
        userMemory.UserId = batatas.createUser(user).id;
    }

    private void openListsOverview() {
        startActivity(new Intent(getActivity(), ListsOverviewActivity.class));
    }




    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }
}
