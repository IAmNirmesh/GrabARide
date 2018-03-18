package rahul.nirmesh.grabaride;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by NIRMESH on 19-Mar-18.
 */

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {
    String mTag;

    public static BottomSheetRiderFragment newInstance (String tag) {
        BottomSheetRiderFragment f = new BottomSheetRiderFragment();
        Bundle args = new Bundle();
        args.putString("TAG", tag);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString("TAG");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_rider, container, false);
        return view;
    }
}
