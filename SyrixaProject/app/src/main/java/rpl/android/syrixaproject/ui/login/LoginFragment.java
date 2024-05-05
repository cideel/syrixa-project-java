package rpl.android.syrixaproject.ui.login;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.databinding.FragmentLoginBinding;
import rpl.android.syrixaproject.ui.helper.LoadingDialog;
import rpl.android.syrixaproject.ui.register.RegisterFragmentDirections;


public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoadingDialog loadingDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
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
        binding.toSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.emailET.setError(null);
                binding.passwordET.setError(null);
                if(binding.emailET.getEditText().getText() == null || binding.emailET.getEditText().getText().toString().isEmpty()){
                    binding.emailET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.emailET.requestFocus();
                }else if(binding.passwordET.getEditText().getText() == null || binding.passwordET.getEditText().getText().toString().isEmpty()){
                    binding.passwordET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.passwordET.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.emailET.getEditText().getText().toString()).matches()){
                    binding.emailET.setError(getString(R.string.email_must_valid));
                    binding.emailET.requestFocus();
                }else{
                    loadingDialog.startLoadingDialog();
                    String email = binding.emailET.getEditText().getText().toString();
                    String password = binding.passwordET.getEditText().getText().toString();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    makeToast("Login success");
                                    loadingDialog.dismissDialog();
                                    auth.getCurrentUser().sendEmailVerification();

                                    NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
                                    Navigation.findNavController(view).navigate(action);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingDialog.dismissDialog();
                                    makeToast(e.getMessage());
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