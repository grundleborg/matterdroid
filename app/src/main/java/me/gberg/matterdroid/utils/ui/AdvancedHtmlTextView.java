package me.gberg.matterdroid.utils.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class AdvancedHtmlTextView extends HtmlTextView {

    public AdvancedHtmlTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AdvancedHtmlTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvancedHtmlTextView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean res = super.onTouchEvent(event);
        return res;
    }
}
