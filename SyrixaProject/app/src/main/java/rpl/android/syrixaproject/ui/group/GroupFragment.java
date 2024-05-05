package rpl.android.syrixaproject.ui.group;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import rpl.android.syrixaproject.data.model.Groups;
import rpl.android.syrixaproject.data.model.MemberChecked;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.DialogMemberCommandBinding;
import rpl.android.syrixaproject.databinding.FragmentGroupBinding;
import rpl.android.syrixaproject.ui.helper.LoadingDialog;


public class GroupFragment extends Fragment implements GroupMemberAdapter.OnItemClickListener, BottomSheetMemberFragment.OnItemClickListener {

    private FragmentGroupBinding binding;

    private LoadingDialog loadingDialog;

    private List<User> dataList = new ArrayList<>();

    private List<MemberChecked> memberChecled = new ArrayList<>();

    private GroupMemberAdapter adapter;

    private Groups groups;

    private  BottomSheetMemberFragment bottomSheetFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = new LoadingDialog(requireContext());
        GroupFragmentArgs args = GroupFragmentArgs.fromBundle(getArguments());
        groups = args.getGroupData();
        initView();
        initButton();
        initRecyclerView();
    }

    private void initRecyclerView() {
        dataList.clear();
        loadingDialog.startLoadingDialog();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("Users").orderByChild("uid");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        User singleItem = postSnapshot.getValue(User.class);
                        if (singleItem != null) {
                            if(groups.getMemberUid().contains(singleItem.getUid())){
                                dataList.add(singleItem);
                                startRecylerView();
                            }
                        }
                    }
                    ref.removeEventListener(this);
                    loadingDialog.dismissDialog();
                } else {
                    ref.removeEventListener(this);
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });

    }

    private void startRecylerView(){
        String leaderUid = groups.getLeaderUid();
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Boolean isUserLeader = leaderUid.equals(currentUid);
        adapter = new GroupMemberAdapter(requireContext(), currentUid, isUserLeader, dataList);

        binding.groupMemberRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.groupMemberRV.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private void initButton() {

        binding.toTaskListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = GroupFragmentDirections.actionGroupFragmentToTaskListFragment(groups);
                Navigation.findNavController(view).navigate(action);
            }
        });

        binding.addProjectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = GroupFragmentDirections.actionGroupFragmentToAddProjectFragment(groups);
                Navigation.findNavController(view).navigate(action);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = GroupFragmentDirections.actionGroupFragmentToHomeFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });




        binding.addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberChecled.clear();
                loadingDialog.startLoadingDialog();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query query = ref.child("Users").orderByChild("uid");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                User singleItem = postSnapshot.getValue(User.class);
                                if (singleItem != null) {
                                    if(!groups.getMemberUid().contains(singleItem.getUid())){
                                        MemberChecked member =  new MemberChecked(singleItem, false);
                                        memberChecled.add(member);
                                    }
                                }
                            }
                            bottomSheetFragment = new BottomSheetMemberFragment(memberChecled, requireContext(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                            bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
                            bottomSheetFragment.setOnItemClickListener(GroupFragment.this::onItemClicked);
                            ref.removeEventListener(this);
                            loadingDialog.dismissDialog();
                        } else {
                            ref.removeEventListener(this);
                            loadingDialog.dismissDialog();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle onCancelled
                    }
                });


            }
        });
    }

    private void initView() {

        binding.groupNameTV.setText(groups.getName());
        Boolean isUserLeader = groups.getLeaderUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
        if(isUserLeader){
            binding.addMemberBtn.setVisibility(View.VISIBLE);
            binding.addProjectBtn.setVisibility(View.VISIBLE);
        }else{
            binding.addMemberBtn.setVisibility(View.GONE);
            binding.addProjectBtn.setVisibility(View.GONE);
        }
    }

    private void makeToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(User user, View view) {
        DialogMemberCommandBinding dialogView = DialogMemberCommandBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setView(dialogView.getRoot());
        AlertDialog dialog = builder.create();

        dialogView.kickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View views) {
                List<String> memberNow = groups.getMemberUid();
                if(memberNow.removeIf(item -> item.equals(user.getUid()))){
                    Map<String, Object> updatedValues = new HashMap<>();
                    updatedValues.put("memberUid", memberNow);
                    FirebaseDatabase.getInstance().getReference("Groups")
                            .child(groups.getId())
                            .updateChildren(updatedValues)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        makeToast("Member kicked.");
                                        FirebaseDatabase.getInstance().getReference("Groups")
                                                .child(groups.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            dialog.dismiss();
                                                            loadingDialog.dismissDialog();
                                                            if(task.getResult()!=null){
                                                                Groups result = task.getResult().getValue(Groups.class);
                                                                NavDirections action = GroupFragmentDirections.actionGroupFragmentSelf(result);
                                                                Navigation.findNavController(view).navigate(action);
                                                            }else{
                                                                dialog.dismiss();
                                                                loadingDialog.dismissDialog();
                                                                makeToast("Unexpected Error");
                                                                NavDirections action = GroupFragmentDirections.actionGroupFragmentToHomeFragment();
                                                                Navigation.findNavController(view).navigate(action);
                                                            }
                                                        } else {
                                                            dialog.dismiss();
                                                            loadingDialog.dismissDialog();
                                                            makeToast(task.getException().getMessage());
                                                            NavDirections action = GroupFragmentDirections.actionGroupFragmentToHomeFragment();
                                                            Navigation.findNavController(view).navigate(action);
                                                        }
                                                    }
                                                });
                                    } else {
                                        dialog.dismiss();
                                        loadingDialog.dismissDialog();
                                        makeToast(task.getException().getMessage());
                                    }
                                }
                            });
                }
            }
        });

        dialogView.setLeaderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View views) {
                Map<String, Object> updatedValues = new HashMap<>();
                updatedValues.put("leaderUid", user.getUid());
                FirebaseDatabase.getInstance().getReference("Groups")
                        .child(groups.getId())
                        .updateChildren(updatedValues)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    makeToast("Leader Changed");
                                    FirebaseDatabase.getInstance().getReference("Groups")
                                            .child(groups.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        dialog.dismiss();
                                                        loadingDialog.dismissDialog();
                                                        if(task.getResult()!=null){
                                                            Groups result = task.getResult().getValue(Groups.class);
                                                            NavDirections action = GroupFragmentDirections.actionGroupFragmentSelf(result);
                                                            Navigation.findNavController(view).navigate(action);
                                                        }else{
                                                            dialog.dismiss();
                                                            loadingDialog.dismissDialog();
                                                            makeToast("Unexpected Error");
                                                            NavDirections action = GroupFragmentDirections.actionGroupFragmentToHomeFragment();
                                                            Navigation.findNavController(view).navigate(action);
                                                        }
                                                    } else {
                                                        dialog.dismiss();
                                                        loadingDialog.dismissDialog();
                                                        makeToast(task.getException().getMessage());
                                                        NavDirections action = GroupFragmentDirections.actionGroupFragmentToHomeFragment();
                                                        Navigation.findNavController(view).navigate(action);
                                                    }
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    loadingDialog.dismissDialog();
                                    makeToast(task.getException().getMessage());
                                }
                            }
                        });
            }
        });
        dialog.show();
    }


    @Override
    public void onItemClicked(List<MemberChecked> memberCheckedList,View view) {
        loadingDialog.startLoadingDialog();
        List<String> checkedUserUIDs = new ArrayList<>();
        for(MemberChecked memberChecked : memberCheckedList){
            if(memberChecked.getChecked()){
                checkedUserUIDs.add(memberChecked.getUser().getUid());
            }
        }
        checkedUserUIDs.addAll(groups.getMemberUid());
        Map<String, Object> updatedValues = new HashMap<>();
        updatedValues.put("memberUid", checkedUserUIDs);
        FirebaseDatabase.getInstance().getReference("Groups")
                .child(groups.getId())
                .updateChildren(updatedValues)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            makeToast("Member added");
                            FirebaseDatabase.getInstance().getReference("Groups")
                                    .child(groups.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                bottomSheetFragment.dismiss();
                                                loadingDialog.dismissDialog();
                                                if(task.getResult()!=null){
                                                    Groups result = task.getResult().getValue(Groups.class);
                                                    NavDirections action = GroupFragmentDirections.actionGroupFragmentSelf(result);
                                                    Navigation.findNavController(requireView()).navigate(action);
                                                }else{
                                                    bottomSheetFragment.dismiss();
                                                    loadingDialog.dismissDialog();
                                                    makeToast("Unexpected Error");
                                                    NavDirections action = GroupFragmentDirections.actionGroupFragmentToHomeFragment();
                                                    Navigation.findNavController(requireView()).navigate(action);
                                                }
                                            } else {
                                                bottomSheetFragment.dismiss();
                                                loadingDialog.dismissDialog();
                                                makeToast(task.getException().getMessage());
                                                NavDirections action = GroupFragmentDirections.actionGroupFragmentToHomeFragment();
                                                Navigation.findNavController(requireView()).navigate(action);
                                            }
                                        }
                                    });
                        } else {
                            bottomSheetFragment.dismiss();
                            loadingDialog.dismissDialog();
                            makeToast(task.getException().getMessage());
                        }
                    }
                });

    }
}