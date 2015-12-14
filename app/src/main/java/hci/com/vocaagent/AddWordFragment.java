package hci.com.vocaagent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;


public class AddWordFragment extends DialogFragment {
    private Button mAddButton;
    private ImageButton mClearButton;
    private CustomAutoCompleteView mInputWord;
    private ArrayAdapter<String> mArrayAdapter; // auto complete text view adapter
    public static final String EXTRA_WORD = "hci.com.vocaagent.word";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_word, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().setGravity(Gravity.TOP | Gravity.CENTER);
        mInputWord = (CustomAutoCompleteView) v.findViewById(R.id.edit_text_add_word);

        // insert button settings
        mAddButton = (Button) v.findViewById(R.id.dialog_add_word_button_insert);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wordStr = mInputWord.getText().toString().split("::")[0];
                sendResult(Activity.RESULT_OK, wordStr);
                dismiss();
            }
        });

        // clear button settings
        mClearButton = (ImageButton) v.findViewById(R.id.dialog_add_word_button_clear);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputWord.setText("");
            }
        });

        // auto complete text view settings
        mArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                AutoCompleteDictionary.getAutoCompleteStrings(getActivity(),
                        mInputWord.getText().toString()));
        mInputWord.setAdapter(mArrayAdapter);

        mInputWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence userInput, int start, int before, int count) {
                String[] autoStrings = AutoCompleteDictionary.getAutoCompleteStrings(getActivity(), userInput.toString());
                AddWordFragment fragment = (AddWordFragment) getActivity().getSupportFragmentManager().findFragmentByTag("add_word");
                fragment.updateAutoCompleteAdapter(autoStrings);
            }

            @Override
            public void afterTextChanged(Editable userInput) {
                // Intentionally left blank
            }
        });

        mInputWord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mInputWord.setText(mInputWord.getText().toString().split("::")[0]);
            }
        });

        // 타이틀
        getDialog().setTitle("단어 추가");
        return v;
    }

    public void updateAutoCompleteAdapter(String[] autoStrings) {
        mArrayAdapter.notifyDataSetChanged();
        mArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_autocomplete, autoStrings);
        mInputWord.setAdapter(mArrayAdapter);
    }

    private void sendResult(int resultCode, String wordStr) {
        if (getTargetFragment() == null)
            return;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_WORD, wordStr);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
