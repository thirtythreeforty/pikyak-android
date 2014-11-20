package net.thirtythreeforty.pikyak.ui.fragments;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.auth.AccountAuthenticator;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements OnPreferenceClickListener {

    private static final int REQUEST_ACCOUNT = 1;

    EditTextPreference defaultAccountPreference;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        defaultAccountPreference = (EditTextPreference)getPreferenceScreen().findPreference("pref_default_account");
        defaultAccountPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ACCOUNT && resultCode == Activity.RESULT_OK) {
            String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
            defaultAccountPreference.setText(accountName);
            defaultAccountPreference.setSummary(accountName);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.equals(defaultAccountPreference)) {
            String defaultAccountName = defaultAccountPreference.getText();
            Account defaultAccount;
            if(defaultAccountName == null || defaultAccountName.isEmpty()) defaultAccount = null;
            else defaultAccount = new Account(defaultAccountName, AccountAuthenticator.ACCOUNT_TYPE);

            Intent intent = AccountManager.newChooseAccountIntent(
                    defaultAccount,
                    null,
                    new String[]{AccountAuthenticator.ACCOUNT_TYPE},
                    true,
                    null,
                    AccountAuthenticator.AUTHTOKEN_TYPE,
                    null,
                    null
            );
            startActivityForResult(intent, REQUEST_ACCOUNT);
        }
        return false;
    }
}
