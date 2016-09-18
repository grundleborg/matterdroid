package me.gberg.matterdroid.utils.ui;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

// Lifted and lightly modified from https://github.com/SufficientlySecure/html-textview/blob/96d1a601c5298d6ceedefd41740f08a7a4da2cd1/HtmlTextView/src/main/java/org/sufficientlysecure/htmltextview/LocalLinkMovementMethod.java
// License Apache v2.
public abstract class AbstractHtmlTextViewLinkMovementMethod extends LinkMovementMethod {
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    URLSpan urlSpan = (URLSpan) link[0];
                    onLinkClicked(widget, urlSpan.getURL());

                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                }

                if (widget instanceof HtmlTextView) {
                    ((HtmlTextView) widget).linkHit = true;
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
                Touch.onTouchEvent(widget, buffer, event);
                return false;
            }
        }
        return Touch.onTouchEvent(widget, buffer, event);
    }

    public abstract void onLinkClicked(final TextView view, final String url);
}
