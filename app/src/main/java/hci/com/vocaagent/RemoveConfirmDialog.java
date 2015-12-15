package hci.com.vocaagent;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class RemoveConfirmDialog extends DialogFragment {
    Button mYes, mNo;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_remove_confirm, container, false);
        getDialog().setTitle(R.string.confirm_delete); // set title

        mYes = (Button)v.findViewById(R.id.delete_yes);
        mNo = (Button)v.findViewById(R.id.delete_no);

        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_OK);
                dismiss();
            }
        });
        return v;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
    }
}
