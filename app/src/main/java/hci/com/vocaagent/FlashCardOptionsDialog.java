package hci.com.vocaagent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

public class FlashCardOptionsDialog extends DialogFragment {
    private static final String ARG_WORDS = "words";
    private ArrayList<Word> mWords;
    public static FlashCardOptionsDialog createDialogFragment(ArrayList<Word> list){
        FlashCardOptionsDialog dialog = new FlashCardOptionsDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_WORDS, list);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWords = getArguments().getParcelableArrayList(ARG_WORDS);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setItems(R.array.flash_options_arr, dialogListener)
                .create();
    }

    private DialogInterface.OnClickListener dialogListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (dialog == getDialog()) {
                        Intent intent = null;
                        switch (which) {
                            case 0:
                                intent = FlashCardPagerActivity.newIntent(getActivity(),
                                        FlashCardFragment.MEMORIZE_MODE, mWords);
                                break;
                            case 1:
                                intent = FlashCardPagerActivity.newIntent(getActivity(),
                                        FlashCardFragment.TEST_MODE, mWords);
                                break;
                        }
                        startActivity(intent);
                    }
                }
            };
}
