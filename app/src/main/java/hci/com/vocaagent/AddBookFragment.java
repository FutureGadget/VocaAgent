package hci.com.vocaagent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class AddBookFragment extends DialogFragment {
    private Button addButton;
    private EditText bookTitle;
    public static final String EXTRA_TITLE = "hci.com.vocaagent.title";

    private void sendResult(int resultCode, String title) {
        if (getTargetFragment() == null)
            return;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TITLE, title);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_book, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        bookTitle = (EditText) v.findViewById(R.id.edit_text_add_book);
        addButton = (Button) v.findViewById(R.id.button_add_book);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_OK, bookTitle.getText().toString());
                dismiss();
            }
        });
        return v;
    }
}
