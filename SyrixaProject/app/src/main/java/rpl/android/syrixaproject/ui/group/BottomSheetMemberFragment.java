package rpl.android.syrixaproject.ui.group;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import rpl.android.syrixaproject.data.model.MemberChecked;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.BottomSheetMemberLayoutBinding;

public class BottomSheetMemberFragment extends BottomSheetDialogFragment {

    private BottomSheetMemberLayoutBinding bottomSheetBinding;

    private List<MemberChecked> memberCheckedList;

    private String currentUid;

    private Context contexts;

    private AddMemberCheckedAdapter adapter;

    private BottomSheetMemberFragment.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(BottomSheetMemberFragment.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public BottomSheetMemberFragment(List<MemberChecked> member, Context contexts, String currentUid) {
        this.contexts = contexts;
        this.memberCheckedList = member;
        this.currentUid = currentUid;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bottomSheetBinding = BottomSheetMemberLayoutBinding.inflate(inflater, container, false);
        initRecyclerView();
        initButton();
        return bottomSheetBinding.getRoot();
    }

    private void initRecyclerView() {
        // Inisialisasi RecyclerView dan adapter Anda di sini
         adapter = new AddMemberCheckedAdapter(contexts, memberCheckedList, currentUid);

         RecyclerView recyclerView = bottomSheetBinding.groupMemberRV;
         recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
         recyclerView.setAdapter(adapter);
    }

    private void initButton(){
        bottomSheetBinding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClicked(adapter.getData(), view);
                }
            }
        });
    }

    // Fungsi untuk menutup Bottom Sheet
    private void closeBottomSheet() {
        dismiss();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomSheetBinding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeBottomSheet();
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClicked(List<MemberChecked> memberCheckedList, View view);
    }
}
