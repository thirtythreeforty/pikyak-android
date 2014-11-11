package net.thirtythreeforty.pikyak.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.thirtythreeforty.pikyak.R;

/**
 * A {@link FrameLayout} that provides an image to render into and vote counter.
 * The embedded {@link ImageView} can be retrieved with the getImage() method.
 */
public class VotableImage extends FrameLayout implements OnCheckedChangeListener {

    private final TextView mScoreTextView;
    private final CheckBox mUpvoteButton;
    private final CheckBox mDownvoteButton;
    private final ImageView mImage;
    private Callbacks mCallbacks;

    public static interface Callbacks {
        void onUpvote(VotableImage view);
        void onDownvote(VotableImage view);
        void onUnvote(VotableImage view);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onUpvote(VotableImage view) {}

        @Override
        public void onDownvote(VotableImage view) {}

        @Override
        public void onUnvote(VotableImage view) {}
    };

    public VotableImage(Context context, Callbacks callbacks) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_item, this, true);

        mScoreTextView = (TextView)findViewById(R.id.scoreTextView);
        mUpvoteButton = (CheckBox)findViewById(R.id.upvoteButton);
        mDownvoteButton = (CheckBox)findViewById(R.id.downvoteButton);
        mImage = (ImageView)findViewById(R.id.image);

        mUpvoteButton.setOnCheckedChangeListener(this);
        mDownvoteButton.setOnCheckedChangeListener(this);

        mCallbacks = callbacks;
    }

    public VotableImage(Context context) {
        this(context, sDummyCallbacks);
    }

    public ImageView getImage() {
        return mImage;
    }

    public void setScore(int score) {
        mScoreTextView.setText(String.format("%d", score));
    }

    public void setUserScore(int userScore) {
        mUpvoteButton.setChecked(userScore > 0);
        mDownvoteButton.setChecked(userScore < 0);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if(!checked) {
            mCallbacks.onUnvote(this);
        } else if(compoundButton == mUpvoteButton) {
            mCallbacks.onUpvote(this);
            mDownvoteButton.setChecked(false);
        } else if(compoundButton == mDownvoteButton) {
            mCallbacks.onDownvote(this);
            mUpvoteButton.setChecked(false);
        }
    }
}
