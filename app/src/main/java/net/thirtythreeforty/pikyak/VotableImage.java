package net.thirtythreeforty.pikyak;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class VotableImage extends FrameLayout implements OnClickListener {

    private final TextView mScoreTextView;
    private final Button mUpvoteButton;
    private final Button mDownvoteButton;
    private final ImageView mImage;

    public VotableImage(Context context) {
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
    }

    public ImageView getImage() {
        return mImage;
    }

    public void setScore(int score) {
        mScoreTextView.setText(Integer.toString(score));
    }

    @Override
    public void onClick(View v) {
        if(v == mUpvoteButton) {
            // TODO
        } else if(v == mDownvoteButton) {
            // TODO
        }
    }
}
