package me.gberg.matterdroid.utils.ui;

import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;

import timber.log.Timber;

public class HtmlTextViewLinkMovementMethod extends AbstractHtmlTextViewLinkMovementMethod {
    public final void onLinkClicked(final TextView view, final String url) {
        Timber.v("Link Clicked: " + url);

        // If the URL is vaguely sane, fire an intent to load it.
        if (url != null && url.length() > 0) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            view.getContext().startActivity(i);
        }
    }
}
