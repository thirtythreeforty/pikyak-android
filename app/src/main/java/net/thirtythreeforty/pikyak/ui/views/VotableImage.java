package net.thirtythreeforty.pikyak.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
    private final Button mUpvoteButton;
    private final Button mDownvoteButton;
    private final ImageView mImage;
    private Callbacks mCallbacks;

    public static interface Callbacks {
        void onUpvote(VotableImage view);
        void onDownvote(VotableImage view);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onUpvote(VotableImage view) {}

        @Override
        public void onDownvote(VotableImage view) {}
    };

    public VotableImage(Context context, Callbacks callbacks) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_item, this, true);

        mScoreTextView = (TextView)findViewById(R.id.scoreTextView);
        mUpvoteButton = (Button)findViewById(R.id.upvoteButton);
        mDownvoteButton = (Button)findViewById(R.id.downvoteButton);
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

    @Override
    public void onClick(View v) {
        if(v == mUpvoteButton) {
            mCallbacks.onUpvote(this);
        } else if(v == mDownvoteButton) {
            mCallbacks.onDownvote(this);
        }
    }
}
