package hci.com.vocaagent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import hci.com.vocaagent.R;

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
        bookTitle = (EditText) v.findViewById(R.id.edit_text_add_book);
        addButton = (Button) v.findViewById(R.id.button_add_book);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_OK, bookTitle.getText().toString());
                dismiss();
            }
        });

        // 추가 부분
        getDialog().setTitle("단어장 추가");

        // show soft keyboard automatically
        bookTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        return v;
    }
}
