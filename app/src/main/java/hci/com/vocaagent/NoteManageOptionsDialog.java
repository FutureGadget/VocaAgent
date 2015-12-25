package hci.com.vocaagent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import hci.com.vocaagent.database.VocaAgentDbSchema;

public class NoteManageOptionsDialog extends android.support.v4.app.DialogFragment {
    private int mBookId;
    private static final String ARG_BOOK_ID = "arg_book_id";

    public static NoteManageOptionsDialog newDialogInstance(int bookId) {
        NoteManageOptionsDialog dialog = new NoteManageOptionsDialog();
        Bundle arg = new Bundle();
        arg.putInt(ARG_BOOK_ID, bookId);
        dialog.setArguments(arg);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBookId = getArguments().getInt(ARG_BOOK_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setItems(R.array.manage_note_arr, dialogListener)
                .create();
    }

    private DialogInterface.OnClickListener dialogListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (dialog == getDialog()) {
                        switch (which) {
                            case 0:
                                Intent intent = BookActivity.newIntent(getActivity(), mBookId);
                                startActivity(intent);
                                break;
                            case 1:
                                Book book = VocaLab.getVoca(getActivity()).getBookByID(mBookId);
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("단어장 삭제")
                                        .setMessage("\"" + book.getBookName() + "\" 를 정말 삭제하시겠습니까?")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                VocaLab.getVoca(getActivity()).deleteBook(VocaAgentDbSchema.BookTable.Cols.book_id + " = ?",
                                                        new String[]{mBookId + ""});
                                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create()
                                        .show();
                                break;
                            case 2:
                                View titleChange = LayoutInflater.from(getActivity()).
                                        inflate(R.layout.dialog_change_book_title, null);
                                final EditText newTitle = (EditText) titleChange.findViewById(R.id.change_book_title_edit_text);
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("단어장 이름 변경")
                                        .setView(titleChange)
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Book newBook = VocaLab.getVoca(getActivity()).getBookByID(mBookId);
                                                newBook.setBookName(newTitle.getText().toString());
                                                VocaLab.getVoca(getActivity()).updateBook(newBook);
                                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show();
                                break;
                        }
                    }
                }
            };
}
