package net.thirtythreeforty.pikyak;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import net.thirtythreeforty.pikyak.networking.model.ConversationListModel;
import net.thirtythreeforty.pikyak.networking.model.ConversationListModel.ConversationPreviewModel;

public class ConversationPreviewAdapter extends ArrayAdapter<ConversationPreviewModel> {
    final LayoutInflater mInflater;

    // Not sure which of these constructors will be useful at this point.
    ConversationPreviewAdapter(Context context, ConversationListModel conversationList) {
        super(context, R.layout.image_item, conversationList.conversations);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    ConversationPreviewAdapter(Context context) {
        this(context, new ConversationListModel());
        ConversationPreviewModel conversationPreview = new ConversationPreviewModel();
        conversationPreview.url = "http://www.google.com/images/srpr/logo11w.png";
        add(conversationPreview);
    }

    public void replaceConversationListModel(ConversationListModel conversationList) {
        clear();
        addConversationListModel(conversationList);
    }

    public void addConversationListModel(ConversationListModel conversationList) {
        addAll(conversationList.conversations);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if(view == null) {
            view = mInflater.inflate(R.layout.image_item, parent, false);
        }

        // https://github.com/MeetMe/TwitchTvClient uses Holders here for speed... Consider this if
        // this section is slow.  Doubt it frankly.

        ConversationPreviewModel conversationPreview = getItem(position);

        ImageView image = (ImageView)view.findViewById(R.id.image);
        Picasso.with(getContext())
                .load(conversationPreview.url)
                .error(R.drawable.ic_action_refresh)
                .into(image);

        return view;
    }
}
