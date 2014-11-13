package net.thirtythreeforty.pikyak.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.thirtythreeforty.pikyak.R;

/**
 * A {@link FrameLayout} that provides an image to render into and vote counter.
 * The embedded {@link ImageView} can be retrieved with the getImage() method.
 */
public class VotableImage extends FrameLayout implements OnClickListener {

    private final TextView mScoreTextView;
    private final CheckBox mUpvoteButton;
    private final CheckBox mDownvoteButton;
    private final ImageView mImage;
    private Callbacks mCallbacks;

    public static interface Callbacks {
        void onVote(VotableImage view, int score);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onVote(VotableImage view, int score) {}
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

        mUpvoteButton.setOnClickListener(this);
        mDownvoteButton.setOnClickListener(this);

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
}
