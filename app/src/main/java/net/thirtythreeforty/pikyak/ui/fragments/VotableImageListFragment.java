package net.thirtythreeforty.pikyak.ui.fragments;

import android.os.Bundle;

import net.thirtythreeforty.pikyak.ui.adapters.VotableImageAdapter;
import net.thirtythreeforty.pikyak.ui.fragments.headless.AuthorizationGetterFragment;
import net.thirtythreeforty.pikyak.ui.views.VotableImage;

abstract class VotableImageListFragment extends OttoFragment implements VotableImageAdapter.Callbacks {
    protected AuthorizationGetterFragment mAuthorizationGetterFragment;
    private static final String AUTHGETTER_TAG = "authGetter";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            mAuthorizationGetterFragment = AuthorizationGetterFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .add(mAuthorizationGetterFragment, AUTHGETTER_TAG)
                    .commit();
        } else {
            mAuthorizationGetterFragment = (AuthorizationGetterFragment)getFragmentManager()
                    .findFragmentByTag(AUTHGETTER_TAG);
        }
    }

    @Override
    public void onImageVote(VotableImage view, int user_score) {
        mAuthorizationGetterFragment.withAuthorization(new DoVote(
                view.getImageModel().id,
                user_score
        ));
    }
}
