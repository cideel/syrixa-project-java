package rpl.android.syrixaproject.ui.create;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.data.model.Groups;
import rpl.android.syrixaproject.databinding.FragmentCreateGroupBinding;
import rpl.android.syrixaproject.databinding.FragmentLoginBinding;
import rpl.android.syrixaproject.ui.helper.LoadingDialog;
import rpl.android.syrixaproject.ui.register.RegisterFragmentDirections;


public class CreateGroupFragment extends Fragment {

    private FragmentCreateGroupBinding binding;

    private LoadingDialog loadingDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateGroupBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = new LoadingDialog(requireContext());
        initButton();

    }

    private void initButton() {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections directions = CreateGroupFragmentDirections.actionCreateGroupFragmentToHomeFragment();
                Navigation.findNavController(view).navigate(directions);
            }
        });

        binding.createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.groupNameET.setError(null);
                binding.groupDescET.setError(null);
                if(binding.groupNameET.getEditText().getText() == null || binding.groupNameET.getEditText().getText().toString().isEmpty()){
                    binding.groupNameET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.groupNameET.requestFocus();
                }else if(binding.groupDescET.getEditText().getText() == null || binding.groupDescET.getEditText().getText().toString().isEmpty()){
                    binding.groupDescET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.groupDescET.requestFocus();
                }else {
                    try {
                        loadingDialog.startLoadingDialog();
                        String groupName = binding.groupNameET.getEditText().getText().toString();
                        String groupDesc = binding.groupDescET.getEditText().getText().toString();
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        List<String> memberList = new ArrayList<>();
                        memberList.add(uid);
                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference groupRef = rootRef.child("Groups");
                        String key = groupRef.push().getKey();
                        Groups groups = new Groups(key,groupName, groupDesc, uid, memberList);
                        groupRef.child(key).setValue(groups)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        loadingDialog.dismissDialog();
                                        if (task.isSuccessful()) {
                                            makeToast("Group Created");
                                            NavDirections action = CreateGroupFragmentDirections.actionCreateGroupFragmentToHomeFragment();
                                            Navigation.findNavController(view).navigate(action);
                                        } else {
                                            makeToast(task.getException().getMessage());
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        makeToast(e.toString());
                    }



                }
            }
        });
    }

    private void makeToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}