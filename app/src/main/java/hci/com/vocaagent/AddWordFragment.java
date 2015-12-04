package hci.com.vocaagent;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class AddWordFragment extends DialogFragment {
    private Button addButton;
    private Button clearButton;
    private AutoCompleteTextView inputWord;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_word, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        inputWord = (AutoCompleteTextView) v.findViewById(R.id.edit_text_add_word);
        clearButton = (Button) v.findViewById(R.id.dialog_add_word_button_clear);
        addButton = (Button)v.findViewById(R.id.dialog_add_word_button_insert);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputWord.setText("");
            }
        });
        return v;
    }
}
