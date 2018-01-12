package instatiator.dailykittybot2.validation;

import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static instatiator.dailykittybot2.util.TextHelper.create_list_html;

public class ValidationResult {

    public boolean validates;
    public List<String> errors;
    public List<String> warnings;

    public ValidationResult(boolean ok, List<String> errors, List<String> warnings) {
        this.validates = ok;
        this.errors = errors == null ? new LinkedList<>() : errors;
        this.warnings = warnings == null ? new LinkedList<>() : warnings;
    }

    public void updateUI(CardView card_errors, TextView text_errors, CardView card_warnings, TextView text_warnings) {
        if (errors.size() > 0) {
            card_errors.setVisibility(VISIBLE);
            Spanned errorSpan = Html.fromHtml(create_list_html(errors));
            text_errors.setText(errorSpan, TextView.BufferType.SPANNABLE);
        } else {
            card_errors.setVisibility(GONE);
        }

        if (warnings.size() > 0) {
            card_warnings.setVisibility(VISIBLE);
            Spanned warningSpan = Html.fromHtml(create_list_html(warnings));
            text_warnings.setText(warningSpan, TextView.BufferType.SPANNABLE);
        } else {
            card_warnings.setVisibility(GONE);
        }
   }
}
