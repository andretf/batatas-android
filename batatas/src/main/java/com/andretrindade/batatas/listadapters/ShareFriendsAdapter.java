package com.andretrindade.batatas.listadapters;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.andretrindade.batatas.R;
import com.andretrindade.batatas.persistence.BatatasClientProvider;
import com.andretrindade.batatas.persistence.DaggerInjector;
import com.andretrindade.batatas.persistence.User;
import com.andretrindade.batatas.view.TextAwesome;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShareFriendsAdapter extends ArrayAdapter<User> {

    private final Activity context;
    private final int resource;
    private final List<User> items;

    @Inject
    public BatatasClientProvider.BatatasClient batatas;

    public ShareFriendsAdapter(Activity context, List<User> items) {
        super(context, R.layout.layout_list_share_friends_item, items);

        this.context = context;
        this.resource = R.layout.layout_list_share_friends_item;
        this.items = items;

        DaggerInjector.bootstrap(this);
    }

    @Override
    public View getView(final int position, View rowView, ViewGroup parent) {

        final User item = items.get(position);

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(resource, null);
            rowView.setTag(new ViewHolder(rowView, item));
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.itemName.setText(item.name);

        if (item.IsShared) {
            holder.itemChecked.setText(R.string.fa_share_alt_square);
        } else {
            holder.itemChecked.setText(R.string.fa_square_o);
        }

        return rowView;
    }

    class ViewHolder {

        private final User item;

        @InjectView(R.id.share_friend_check_btn)
        TextAwesome itemChecked;

        @InjectView(R.id.share_friend_name)
        TextView itemName;

        public ViewHolder(View view, User item) {
            this.item = item;
            ButterKnife.inject(this, view);
        }

        @OnClick(R.id.share_friend_check_btn)
        public void onButtonClick() {
            if (item.IsShared) {
                batatas.unshareListFriend(item.list_id, item.id, new Callback<Void>() {
                    @Override
                    public void success(Void v, Response response) {
                        item.IsShared = false;
                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if ((getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                            Log.e("unshareListFriend err", error.getMessage());
                        }
                        Toast.makeText(getContext(), getContext().getString(R.string.err_unshare_friend), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                batatas.shareListFriend(item.list_id, item, new Callback<String>() {
                    @Override
                    public void success(String users, Response response) {
                        item.IsShared = true;
                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if ((getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                            Log.e("shareListFriend err", error.getMessage());
                        }
                        Toast.makeText(getContext(), getContext().getString(R.string.err_share_friend), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

}
