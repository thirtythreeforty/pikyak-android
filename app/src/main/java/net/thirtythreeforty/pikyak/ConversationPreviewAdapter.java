package net.thirtythreeforty.pikyak;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.thirtythreeforty.pikyak.networking.PikyakAPIFactory;
import net.thirtythreeforty.pikyak.networking.PikyakServerAPI;
import net.thirtythreeforty.pikyak.networking.model.ConversationListModel;
import net.thirtythreeforty.pikyak.networking.model.ConversationListModel.ConversationPreviewModel;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConversationPreviewAdapter extends ArrayAdapter<ConversationPreviewModel> {
    static final String TAG = "ConversationPreviewAdapter";

    final LayoutInflater mInflater;

    ConversationPreviewAdapter(Context context) {
        super(context, R.layout.image_item, new ArrayList<ConversationPreviewModel>());

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Dummies for testing
        ConversationPreviewModel conversationPreview = new ConversationPreviewModel();
        conversationPreview.url = "http://www.google.com/images/srpr/logo11w.png";
        add(conversationPreview);
        conversationPreview = new ConversationPreviewModel();
        conversationPreview.url = "http://funnycat-pictures.com/wp-content/uploads/2014/10/funny-cat-photos.jpg";
        add(conversationPreview);

        // Again, for testing.
        Picasso.with(getContext()).setIndicatorsEnabled(true);
    }

    public void reloadConversationList() {
        PikyakAPIFactory.getAPI().getConversationList(
                0,
                PikyakServerAPI.SORT_METHOD_HOT,
                "",
                new Callback<ConversationListModel>() {
                    @Override
                    public void success(ConversationListModel conversationList, Response response) {
                        replaceConversationList(conversationList);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(
                                getContext(),
                                "Downloading the conversation list failed! Why: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                        Log.e(TAG, "Download failed!", error.getCause());
                    }
                });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VotableImage view;

        if(convertView instanceof VotableImage) {
            view = (VotableImage)convertView;
        } else {
            view = new VotableImage(getContext());
        }

        ConversationPreviewModel conversationPreview = getItem(position);
        view.setScore(position);

        Picasso.with(getContext())
                .load(conversationPreview.url)
                .error(R.drawable.ic_action_refresh)
                .into(view.getImage());

        return view;
    }

    private void replaceConversationList(ConversationListModel conversationList) {
        clear();
        addConversationList(conversationList);
    }

    private void addConversationList(ConversationListModel conversationList) {
        addAll(conversationList.conversations);
        notifyDataSetChanged();
    }
}
