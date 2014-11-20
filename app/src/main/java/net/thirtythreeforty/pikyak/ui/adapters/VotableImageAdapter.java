package net.thirtythreeforty.pikyak.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.squareup.picasso.Picasso;

import net.thirtythreeforty.pikyak.BuildConfig;
import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.networking.model.ImageModel;
import net.thirtythreeforty.pikyak.ui.views.VotableImage;

import java.util.ArrayList;

public abstract class VotableImageAdapter extends ArrayAdapter<ImageModel>
        implements VotableImage.Callbacks
{
    static final String TAG = "VotableImageAdapter";

    final LayoutInflater mInflater;

    public interface Callbacks {
        public void onRefreshCompleted(boolean success);
        public void onImageVote(VotableImage view, int user_score);
        public void onImageFlag(VotableImage view, boolean flag);
    }

    protected Callbacks mCallbacks = null;

    public VotableImageAdapter(Context context) {
        super(context, R.layout.image_item, new ArrayList<ImageModel>());

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(BuildConfig.DEBUG) {
            Picasso.with(getContext()).setIndicatorsEnabled(true);
            Picasso.with(getContext()).setLoggingEnabled(true);
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    abstract public void reload();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageModel post = getItem(position);

        VotableImage view;
        if(convertView instanceof VotableImage) {
            view = (VotableImage)convertView;
            view.setImageModel(post);
        } else {
            view = new VotableImage(getContext(), post, this);
        }

        return view;
    }

    @Override
    public void onVote(VotableImage view, int score) {
        if(mCallbacks != null) mCallbacks.onImageVote(view, score);
    }

    @Override
    public void onFlag(VotableImage view, boolean flag) {
        if(mCallbacks != null) mCallbacks.onImageFlag(view, flag);
    }
}
