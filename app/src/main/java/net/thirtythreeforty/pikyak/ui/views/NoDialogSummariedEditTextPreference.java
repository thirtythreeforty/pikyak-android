package net.thirtythreeforty.pikyak.ui.views;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

/** Really hacky class that prevents the dialog in an EditTextPreference from appearing.  Please fix! */
public class NoDialogSummariedEditTextPreference extends SummariedEditTextPreference {
    public NoDialogSummariedEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NoDialogSummariedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoDialogSummariedEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }

    @Override
    protected void onClick() {
    }
}
