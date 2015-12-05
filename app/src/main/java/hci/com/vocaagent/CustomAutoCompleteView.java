package hci.com.vocaagent;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class CustomAutoCompleteView extends AutoCompleteTextView{
    public CustomAutoCompleteView (Context context) {
        super(context);
    }
    public CustomAutoCompleteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public CustomAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        String filterText = "";
        super.performFiltering(filterText, keyCode);
    }

    @Override
    protected void replaceText(CharSequence text) {
        super.replaceText(text);
    }
}
