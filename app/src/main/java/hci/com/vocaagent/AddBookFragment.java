package hci.com.vocaagent;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

public class AddBookFragment extends DialogFragment {
    private Button addButton;
    private EditText bookTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_book, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        bookTitle = (EditText) v.findViewById(R.id.edit_text_add_book);
        addButton = (Button) v.findViewById(R.id.button_add_book);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VocaLab vocaLab = VocaLab.getVoca(getActivity());
                Book book = new Book();
                String lastModified = DateFormat.format("yyyy-MM-dd", new Date()).toString();
                book.setBookName(bookTitle.getText().toString());
                book.setLastModified(lastModified);
                book.setNumWords(0);
                vocaLab.addBook(book);
                dismiss();
            }
        });
        return v;
    }
}
