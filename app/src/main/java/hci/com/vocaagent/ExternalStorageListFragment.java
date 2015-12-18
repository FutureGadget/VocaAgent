package hci.com.vocaagent;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        mRecyclerView = (RecyclerView) v.findViewById(R.id.import_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), android.support.v7.widget.LinearLayoutManager.VERTICAL, false));

        mAdapter = new FileListAdapter(Utils.getListFiles());
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    private class FileHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public FileHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.import_list_text_view);
        }

        public void bind(String fileName) {
            mTextView.setText(fileName);
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
