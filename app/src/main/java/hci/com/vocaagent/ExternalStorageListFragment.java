package hci.com.vocaagent;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.List;

/*
 * Fragment which shows external storage contents.
 */
public class ExternalStorageListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private FileListAdapter mAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_import, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("단어장 가져오기");

        mRecyclerView = (RecyclerView) v.findViewById(R.id.import_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), android.support.v7.widget.LinearLayoutManager.VERTICAL, false));

        mAdapter = new FileListAdapter(Utils.getListFiles());
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    private class FileHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTextView;

        public FileHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTextView = (TextView) itemView.findViewById(R.id.import_list_text_view);
        }

        public void bind(String fileName) {
            mTextView.setText(fileName);
        }

        @Override
        public void onClick(View v) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View exportView = inflater.inflate(R.layout.dialog_import_book_name, null);
            final EditText inBookName = (EditText) exportView.findViewById(R.id.import_book_name);

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.import_confirm, mTextView.getText().toString()))
                    .setView(exportView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            VocaLab.getVoca(getActivity()).importNote(mTextView.getText().toString(), inBookName.getText().toString());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            dialog.show();
        }
    }

    private class FileListAdapter extends RecyclerView.Adapter<FileHolder> {
        List<String> files;

        public FileListAdapter(List<String> files) {
            this.files = files;
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            holder.bind(files.get(position));
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_import, parent, false);
            return new FileHolder(view);
        }
    }
}
