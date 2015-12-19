package hci.com.vocaagent;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class StatisticsDialogFragment extends DialogFragment {
    private RecyclerView mRecyclerView;
    private ResultWordAdapter mAdapter;
    private Button mQuitButton;
    private Button mReviewButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_statistics, container, false);
        getDialog().setCanceledOnTouchOutside(false); // not modal
        getDialog().setTitle(R.string.end_of_test);
        mQuitButton = (Button) v.findViewById(R.id.quit_test_button);
        mReviewButton = (Button) v.findViewById(R.id.review_button);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.statistics_recycler_view);

        mQuitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setResult(SelectBookFragment.EXAM_TYPE_REVIEW, null);
                getActivity().finish();
            }
        });

        // auto fit height linear layout manager
        final org.solovyev.android.views.llm.LinearLayoutManager linearLayoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new ResultWordAdapter(VocaLab.getVoca(getActivity()).getResultWords());
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    private class ResultWordHolder extends RecyclerView.ViewHolder {
        private TextView mWordTextView;
        private TextView mPhaseDiffTextView;

        public ResultWordHolder(View itemView) {
            super(itemView);
            mWordTextView = (TextView) itemView.findViewById(R.id.result_word_text_view);
            mPhaseDiffTextView = (TextView) itemView.findViewById(R.id.phase_increment_text_view);
        }

        public void bind(ResultWord rw) {
            mWordTextView.setText(rw.getResultWord().getWord());
            if (rw.getPhaseIncrement() < 0) {
                mPhaseDiffTextView.setText("-" + rw.getPhaseIncrement());
                mPhaseDiffTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            } else {
                mPhaseDiffTextView.setText("+" + rw.getPhaseIncrement());
                mPhaseDiffTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            }
        }
    }

    private class ResultWordAdapter extends RecyclerView.Adapter<ResultWordHolder> {
        private List<ResultWord> mResultWords;

        public ResultWordAdapter(List<ResultWord> results) {
            mResultWords = results;
        }

        @Override
        public ResultWordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_result, parent, false);
            return new ResultWordHolder(view);
        }

        @Override
        public void onBindViewHolder(ResultWordHolder holder, int position) {
            holder.bind(mResultWords.get(position));
        }

        @Override
        public int getItemCount() {
            return mResultWords.size();
        }
    }
}
