package net.thirtythreeforty.pikyak;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.RegistrationRequestEvent;

public class SignInDialogFragment extends DialogFragment {
    public interface Callbacks {
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
    };

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;

    private Callbacks mCallbacks = sDummyCallbacks;

    public static SignInDialogFragment newInstance() {
        return new SignInDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity())
                .setTitle(R.string.title_sign_in)
                .setPositiveButton(R.string.action_sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String username = mUsernameEditText.getText().toString();
                        final String password = mPasswordEditText.getText().toString();
                        BusProvider.getBus().post(new RegistrationRequestEvent(
                                new AuthorizationRetriever() {
                                    @Override
                                    public String getUsername() {
                                        return username;
                                    }

                                    @Override
                                    public String getPassword() {
                                        return password;
                                    }
                                }));
                    }
                })
                .setNegativeButton(R.string.action_cancel, null);

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_sign_in, null);
        builder.setView(view);

        View mEditTexts = view.findViewById(R.id.editTexts);
        mUsernameEditText = (EditText)mEditTexts.findViewById(R.id.usernameEditText);
        mPasswordEditText = (EditText)mEditTexts.findViewById(R.id.passwordEditText);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }
}
