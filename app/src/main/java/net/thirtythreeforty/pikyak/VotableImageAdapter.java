package net.thirtythreeforty.pikyak;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import net.thirtythreeforty.pikyak.networking.model.ImageModel;

import java.util.ArrayList;

abstract public class VotableImageAdapter extends ArrayAdapter<ImageModel> {
    static final String TAG = "VotableImageAdapter";

    final LayoutInflater mInflater;

    public interface Callbacks {
        public void onRefreshCompleted(boolean success);
    }

    protected Callbacks mCallbacks = null;

    public VotableImageAdapter(Context context) {
        super(context, R.layout.image_item, new ArrayList<ImageModel>());

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Dummies for testing
        ImageModel post = new ImageModel();
        post.image = "http://www.google.com/images/srpr/logo11w.png";
        add(post);
        post = new ImageModel();
        post.image = "http://funnycat-pictures.com/wp-content/uploads/2014/10/funny-cat-photos.jpg";
        add(post);

        // Again, for testing.
        Picasso.with(getContext()).setIndicatorsEnabled(true);
    }

    public void setCallbacks(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    abstract public void reload();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final VotableImage view = (convertView instanceof VotableImage)
                ? (VotableImage)convertView
                : new VotableImage(getContext());
        final ImageModel post = getItem(position);

        view.setScore(position); // Testing only, obviously

        // Picasso doesn't like loading an empty image
        if(!post.image.isEmpty()) {
            final ImageView imageView = view.getImage();
            imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                // Wait until layout to load image, TODO load asynchronously?
                @Override
                public void onGlobalLayout() {
                    // Ensure we call this only once
                    imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    Transformation t = new KeepRatioTransformation(imageView.getWidth());
                    Picasso.with(getContext())
                            .load(post.image)
                            .transform(t)
                            .error(R.drawable.ic_action_refresh)
                            .into(imageView);
                }
            });

        }

        return view;
    }
}
