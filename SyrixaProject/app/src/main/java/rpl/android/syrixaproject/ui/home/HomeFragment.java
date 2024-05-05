package rpl.android.syrixaproject.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.data.model.Groups;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.FragmentHomeBinding;
import rpl.android.syrixaproject.ui.helper.LoadingDialog;
import rpl.android.syrixaproject.ui.login.LoginFragmentDirections;


public class HomeFragment extends Fragment implements TaskGroupsAdapter.OnItemClickListener{

    private FragmentHomeBinding binding;

    private LoadingDialog loadingDialog;


    private TaskGroupsAdapter adapter;
    private List<Groups> dataList = new ArrayList<>();
    private FirebaseAuth mAuth;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        loadingDialog = new LoadingDialog(requireContext());


        if(currentUser == null){
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToLoginFragment();
            Navigation.findNavController(view).navigate(action);
        }else{
            initButton();
            initData();
        }
    }

    private void initData() {
        loadingDialog.startLoadingDialog();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            loadingDialog.dismissDialog();
                            DataSnapshot result = task.getResult();
                            if (result != null) {
                                String name = Objects.requireNonNull(result.child("username").getValue(String.class));
                                String profilePicture = Objects.requireNonNull(result.child("profilePicture").getValue(String.class));
                                String email = Objects.requireNonNull(result.child("email").getValue(String.class));
                                String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                                initView(new User(uid, name, email, profilePicture));
                                initRecyclerView(uid);
                            }
                        } else {
                            loadingDialog.dismissDialog();
                            makeToast(task.getException().getMessage());
                        }
                    }
                });

    }

    private void initView(User user) {
        binding.usernameTV.setText(user.getUsername());
        Glide.with(requireContext())
                .load(user.getProfilePicture())
                .centerCrop()
                .override(200,200)
                .placeholder(R.drawable.ic_default_avatar)
                .into(binding.profilePictureIv);
    }

    private void initRecyclerView(String currentUser) {
        loadingDialog.startLoadingDialog();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Groups singleItem = postSnapshot.getValue(Groups.class);
                        if (singleItem != null) {
                            for(String member : singleItem.getMemberUid()){
                                if(member.equals(currentUser)){
                                    dataList.add(singleItem);
                                    startRecylerView();
                                }
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
        adapter = new TaskGroupsAdapter(requireContext(), dataList);

        binding.groupListRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.groupListRV.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(Groups groups, View view) {
        NavDirections action = HomeFragmentDirections.actionHomeFragmentToGroupFragment(groups);
        Navigation.findNavController(view).navigate(action);
    }

    private void makeToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void initButton() {
        binding.addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = HomeFragmentDirections.actionHomeFragmentToCreateGroupFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dataList.clear();
        binding = null;
    }
}