package hci.com.vocaagent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class AddWordFragment extends DialogFragment {
    private Button mAddButton;
    private Button mClearButton;
    private CustomAutoCompleteView mInputWord;
    private ArrayAdapter<String> mArrayAdapter; // auto complete text view adapter
    public static final String EXTRA_WORD = "hci.com.vocaagent.word";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_word, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        mInputWord = (CustomAutoCompleteView) v.findViewById(R.id.edit_text_add_word);
        mAddButton = (Button) v.findViewById(R.id.dialog_add_word_button_insert);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wordStr = mInputWord.getText().toString().split("::")[0];
                sendResult(Activity.RESULT_OK, wordStr);
                dismiss();
            }
        });
        mClearButton = (Button) v.findViewById(R.id.dialog_add_word_button_clear);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputWord.setText("");
            }
        });
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
        mArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                AutoCompleteDictionary.getAutoCompleteStrings(getActivity(),
                        mInputWord.getText().toString()));
        mInputWord.setAdapter(mArrayAdapter);
        return v;
    }

    public void updateAutoCompleteAdapter(String[] autoStrings) {
        mArrayAdapter.notifyDataSetChanged();
        mArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, autoStrings);
        mInputWord.setAdapter(mArrayAdapter);
    }

    private void sendResult(int resultCocde, String wordStr) {
        if (getTargetFragment() == null)
            return;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_WORD, wordStr);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCocde, intent);
    }
}
