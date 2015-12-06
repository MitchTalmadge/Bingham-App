package com.aptitekk.binghamapp.Utilities;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import com.aptitekk.binghamapp.MainActivity;
import com.rengwuxian.materialedittext.MaterialEditText;

public class PINEditText extends MaterialEditText {

    private PINListener pinListener;
    private PINEditText[] parentArray;
    private int index;
    private boolean invalid;

    private final int originalUnderlineColor = getUnderlineColor();

    public PINEditText(Context context) {
        super(context);
    }

    public PINEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PINEditText(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
    }

    public void setPINListener(PINListener pinListener, PINEditText[] parentArray, int index) {
        this.pinListener = pinListener;
        this.parentArray = parentArray;
        this.index = index;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
        if(invalid)
            setUnderlineColor(Color.RED);
        else
            setUnderlineColor(this.originalUnderlineColor);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new PINInputConnection(super.onCreateInputConnection(outAttrs),
                true, this);
    }

    private class PINInputConnection extends InputConnectionWrapper {

        private final PINEditText editText;

        public PINInputConnection(InputConnection target, boolean mutable, PINEditText editText) {
            super(target, mutable);

            this.editText = editText;
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            MainActivity.logVerbose("Key Pressed! " + event.getAction() + " .. " + event.getKeyCode());
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                if (length() == 0) {
                    if (pinListener != null)
                        pinListener.onPINPrevious(editText, parentArray, index);
                }
            }
            return super.sendKeyEvent(event);
        }

    }

    public interface PINListener {

        void onPINPrevious(PINEditText currentPINEditText, PINEditText[] parentArray, int index);

    }
}
