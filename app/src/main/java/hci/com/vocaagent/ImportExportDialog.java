package hci.com.vocaagent;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class ImportExportDialog extends DialogFragment {
    private Button mImportButton, mExportButton;
    private final String DIALOG_IMPORT_LIST = "DIALOG_IMPORT_LIST";

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_import_export, dialog.getListView()); // is this code okay?
        dialog.setView(v);

        mExportButton = (Button) v.findViewById(R.id.export_button);
        mImportButton = (Button) v.findViewById(R.id.import_button);

        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExternalStorageListFragment fragment = new ExternalStorageListFragment();
                fragment.show(getActivity().getSupportFragmentManager(), DIALOG_IMPORT_LIST);
            }
        });


        return dialog;
    }
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.dialog_import_export, container, false);
//        return v;
//    }
}