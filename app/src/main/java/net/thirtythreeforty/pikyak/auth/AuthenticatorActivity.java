package net.thirtythreeforty.pikyak.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.APIErrorEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.RegistrationRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.RegistrationResultEvent;


public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public static final String KEY_IS_NEW_ACCOUNT = "isNewAccount";
    private boolean mIsNewAccount;

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);

        mIsNewAccount = getIntent().getBooleanExtra(KEY_IS_NEW_ACCOUNT, false);

        mPasswordEditText = (EditText)findViewById(R.id.passwordEditText);
        mUsernameEditText = (EditText)findViewById(R.id.usernameEditText);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    public void finishLogin(View v) {
        // Perform the actual registration here.  Disable actionables.
        v.setEnabled(false);
        mUsernameEditText.setFocusable(false);
        mPasswordEditText.setFocusable(false);

        final String username = mUsernameEditText.getText().toString();
        final String authorization = PikyakAPIService.computeAuthorization(
                username,
                mPasswordEditText.getText().toString());
        BusProvider.getBus().post(new RegistrationRequestEvent(
                new AuthorizationRetriever() {
                    @Override
                    public String getUsername() {
                        return username;
                    }

                    @Override
                    public String getAuthorization() {
                        return authorization;
                    }
                }));
    }

    @Subscribe
    public void onRegistrationResultEvent(RegistrationResultEvent resultEvent) {
        // Finish the original Authenticator request.
        final String username = mUsernameEditText.getText().toString();
        final String password = mPasswordEditText.getText().toString();

        final Account account = new Account(username, AccountAuthenticator.ACCOUNT_TYPE);
        if (mIsNewAccount) {
            AccountManager.get(this).addAccountExplicitly(account, password, null);
        } else {
            AccountManager.get(this).setPassword(account, password);
        }
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
        intent.putExtra(
                AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Subscribe
    public void onAPIError(APIErrorEvent errorEvent) {
        if(errorEvent.requestEvent instanceof RegistrationRequestEvent) {
            findViewById(R.id.signin_button).setEnabled(true);
            mUsernameEditText.setFocusable(true);
            mPasswordEditText.setFocusable(false);
        }
    }

    public void cancel(View v) {
        finish();
    }
}
