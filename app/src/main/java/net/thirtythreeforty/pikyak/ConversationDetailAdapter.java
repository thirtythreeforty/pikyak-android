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
import net.thirtythreeforty.pikyak.networking.model.ConversationModel;
import net.thirtythreeforty.pikyak.networking.model.ConversationModel.PostModel;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConversationDetailAdapter extends ArrayAdapter<PostModel> {
    static final String TAG = "ConversationPreviewAdapter";

    final LayoutInflater mInflater;
    final int mConversationID;

    ConversationDetailAdapter(Context context, int conversationID) {
        super(context, R.layout.image_item, new ArrayList<PostModel>());

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mConversationID = conversationID;

        // Dummies for testing
        PostModel post = new PostModel();
        post.image = "http://www.google.com/images/srpr/logo11w.png";
        add(post);
        post = new PostModel();
        post.image = "http://funnycat-pictures.com/wp-content/uploads/2014/10/funny-cat-photos.jpg";
        add(post);

        // Again, for testing.
        Picasso.with(getContext()).setIndicatorsEnabled(true);
    }

    public void reloadConversation() {
        PikyakAPIFactory.getAPI().getConversation(
                mConversationID,
                0,
                new Callback<ConversationModel>() {
                    @Override
                    public void success(ConversationModel conversationList, Response response) {
                        replaceConversation(conversationList);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(
                                getContext(),
                                "Downloading the conversation failed! Why: " + error.getMessage(),
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

        PostModel post = getItem(position);

        view.setScore(position); // Testing only, obviously
        if(!post.image.isEmpty()) {
            // Picasso doesn't like loading an empty image
            Picasso.with(getContext())
                    .load(post.image)
                    .error(R.drawable.ic_action_refresh)
                    .into(view.getImage());
        }

        return view;
    }

    private void replaceConversation(ConversationModel conversation) {
        clear();
        addConversation(conversation);
    }

    private void addConversation(ConversationModel conversation) {
        addAll(conversation.posts);
        notifyDataSetChanged();
    }
}
