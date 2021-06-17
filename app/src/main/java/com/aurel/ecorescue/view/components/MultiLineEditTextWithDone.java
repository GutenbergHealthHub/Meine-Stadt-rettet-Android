package com.aurel.ecorescue.view.components;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

public class MultiLineEditTextWithDone extends androidx.appcompat.widget.AppCompatEditText {
    public MultiLineEditTextWithDone(Context context) {
        super(context);
        this.init();
    }

    public MultiLineEditTextWithDone(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public MultiLineEditTextWithDone(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init() {
        this.setImeOptions(EditorInfo.IME_ACTION_DONE);
        this.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }
}
