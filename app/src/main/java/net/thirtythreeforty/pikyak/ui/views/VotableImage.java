package net.thirtythreeforty.pikyak.ui.views;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.networking.model.ImageModel;

/**
 * A {@link FrameLayout} that provides an image to render into and vote counter.
 * The embedded {@link ImageView} can be retrieved with the getImage() method.
 */
public class VotableImage extends FrameLayout implements OnClickListener {

    private final TextView mScoreTextView;
    private final CheckBox mUpvoteButton;
    private final CheckBox mDownvoteButton;
    private final ImageView mImage;
    private ImageModel mImageModel;
    private Callbacks mCallbacks;
    private boolean mIsLaidOut = false;

    public static interface Callbacks {
        void onVote(VotableImage view, int score);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onVote(VotableImage view, int score) {}
    };

    public VotableImage(Context context, ImageModel image, Callbacks callbacks) {
        super(context);

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_item, this, true);

        mScoreTextView = (TextView)findViewById(R.id.scoreTextView);
        mUpvoteButton = (CheckBox)findViewById(R.id.upvoteButton);
        mDownvoteButton = (CheckBox)findViewById(R.id.downvoteButton);
        mImage = (ImageView)findViewById(R.id.image);

        mUpvoteButton.setOnClickListener(this);
        mDownvoteButton.setOnClickListener(this);

        mCallbacks = callbacks;

        setImageModel(image);
    }

    @Override
    public void onClick(View view) {
        int score = 0;
        if(mUpvoteButton.isChecked() || mDownvoteButton.isChecked()) {
            if(view == mUpvoteButton) {
                score = 1;
                mDownvoteButton.setChecked(false);
            } else if(view == mDownvoteButton) {
                score = -1;
                mUpvoteButton.setChecked(false);
            }
        }
        mCallbacks.onVote(this, score);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mIsLaidOut = true;
    }

    @Override
    public boolean isLaidOut() {
        if(VERSION.SDK_INT >= VERSION_CODES.KITKAT) return super.isLaidOut();
        else return mIsLaidOut;
    }

    public ImageModel getImageModel() {
        return mImageModel;
    }

    public void setImageModel(final ImageModel imageModel) {
        mImageModel = imageModel;
        setScore(imageModel.score);
        setUserScore(imageModel.user_score);

        // Load the image
        mImage.setImageResource(android.R.color.transparent);
        final ImageView imageView = mImage;
        if(!imageModel.image.isEmpty()) {
            if(!isLaidOut()) {
                imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    // Wait until layout to load image, TODO load asynchronously?
                    @Override
                    public void onGlobalLayout() {
                        // Ensure we call this only once
                        if(VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
                            imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        doLoad(imageView, imageModel);
                    }
                });
            } else {
                doLoad(imageView, imageModel);
            }
        }
    }

    private void setScore(int score) {
        mScoreTextView.setText(String.format("%d", score));
    }

    private void setUserScore(int userScore) {
        mUpvoteButton.setChecked(userScore > 0);
        mDownvoteButton.setChecked(userScore < 0);
    }

    private void doLoad(ImageView imageView, ImageModel post) {
        Picasso.with(getContext())
                .load(post.image)
                .resize(imageView.getWidth(), 0)
                .error(R.drawable.ic_action_refresh)
                .into(imageView);
    }
}
