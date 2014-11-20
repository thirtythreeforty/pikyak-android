package net.thirtythreeforty.pikyak.ui.fragments.headless;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.auth.AccountAuthenticator;
import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService.RunWithOptionalAuthorizationRequestEvent;
import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService.RunnableWithAuthorization;

import java.util.HashMap;
import java.util.Random;

public class AuthorizationGetterFragment extends Fragment
{
    private static final String TAG = "AccountChooserFragment";

    private HashMap<Integer, RunnableWithAuthorization> intToFunctionMap;
    private static Random random = new Random();

    public static AuthorizationGetterFragment newInstance() {
        return new AuthorizationGetterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        intToFunctionMap = new HashMap<>();
    }

    public void withDefaultAuthorization(RunnableWithAuthorization runnable) {
        String defaultAccountName = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("pref_default_account", "");
        if(defaultAccountName.isEmpty()) {
            Intent intent = AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    new String[]{AccountAuthenticator.ACCOUNT_TYPE},
                    false,
                    null,
                    AccountAuthenticator.AUTHTOKEN_TYPE,
                    null,
                    null
            );

            int key = random.nextInt(Integer.MAX_VALUE - 1) + 1; // Exclude 0
            intToFunctionMap.put(key, runnable);
            startActivityForResult(intent, key);
        } else {
            doPost(runnable, defaultAccountName, AccountAuthenticator.ACCOUNT_TYPE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (intToFunctionMap.containsKey(requestCode)) {
            RunnableWithAuthorization runnable = intToFunctionMap.get(requestCode);
            intToFunctionMap.remove(requestCode);

            if(resultCode == Activity.RESULT_OK) {
                doPost(runnable,
                        data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME),
                        data.getExtras().getString(AccountManager.KEY_ACCOUNT_TYPE));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void doPost(RunnableWithAuthorization runnable, String accountName, String accountType) {
        BusProvider.getBus().post(new RunWithOptionalAuthorizationRequestEvent(
                runnable,
                accountName,
                accountType
        ));
    }
}
