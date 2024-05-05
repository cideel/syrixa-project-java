package rpl.android.syrixaproject.ui.register;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.FragmentRegisterBinding;
import rpl.android.syrixaproject.ui.helper.LoadingDialog;


public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
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
        binding.toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.emailET.setError(null);
                binding.passwordET.setError(null);
                binding.usernameET.setError(null);
                binding.confirmPasswordET.setError(null);

                if(binding.usernameET.getEditText().getText() == null || binding.usernameET.getEditText().getText().toString().isEmpty()){
                    binding.usernameET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.usernameET.requestFocus();
                }else if(binding.emailET.getEditText().getText() == null || binding.emailET.getEditText().getText().toString().isEmpty()){
                    binding.emailET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.emailET.requestFocus();
                }else if(binding.passwordET.getEditText().getText() == null || binding.passwordET.getEditText().getText().toString().isEmpty()){
                    binding.passwordET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.passwordET.requestFocus();
                }else if(binding.confirmPasswordET.getEditText().getText() == null || binding.confirmPasswordET.getEditText().getText().toString().isEmpty()){
                    binding.confirmPasswordET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.confirmPasswordET.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.emailET.getEditText().getText().toString()).matches()){
                    binding.emailET.setError(getString(R.string.email_must_valid));
                    binding.emailET.requestFocus();
                }else if(binding.passwordET.getEditText().getText().equals(binding.confirmPasswordET.getEditText().getText())){
                    binding.confirmPasswordET.setError(getString(R.string.password_not_match));
                    binding.confirmPasswordET.requestFocus();
                }else{
                    loadingDialog.startLoadingDialog();
                    String email = binding.emailET.getEditText().getText().toString();
                    String username = binding.usernameET.getEditText().getText().toString();
                    String password = binding.passwordET.getEditText().getText().toString();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        String uid = auth.getCurrentUser().getUid();
                                        User user = new User(uid, username, email, "");

                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> innerTask) {
                                                        if (innerTask.isSuccessful()) {
                                                            loadingDialog.dismissDialog();
                                                            makeToast(getString(R.string.success_register));
                                                            auth.signOut();
                                                            NavDirections action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment();
                                                            Navigation.findNavController(view).navigate(action);
                                                        } else {
                                                            loadingDialog.dismissDialog();
                                                            makeToast(getString(R.string.failed_register));
                                                        }
                                                    }
                                                });
                                    } else {
                                        loadingDialog.dismissDialog();
                                        makeToast(task.getException().toString());
                                    }
                                }
                            });
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