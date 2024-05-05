package rpl.android.syrixaproject.ui.profile;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.FragmentLoginBinding;
import rpl.android.syrixaproject.databinding.FragmentProfileBinding;
import rpl.android.syrixaproject.databinding.ItemEmailDialogBinding;
import rpl.android.syrixaproject.databinding.ItemPasswordNewBinding;
import rpl.android.syrixaproject.databinding.ItemPasswordOldBinding;
import rpl.android.syrixaproject.ui.helper.LoadingDialog;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private LoadingDialog loadingDialog;
    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri imageUri;


    private Boolean profileChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = new LoadingDialog(requireContext());
        initData();
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
                            }
                        } else {
                            loadingDialog.dismissDialog();
                            makeToast(task.getException().getMessage());
                        }
                    }
                });

    }

    private void initView(User user) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        binding.usernameET.getEditText().setText(user.getUsername());
        Glide.with(requireContext())
                    .load(user.getProfilePicture())
                    .centerCrop()
                .override(200,200)
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(binding.profilePictureIV);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                makeToast("Logout success");
                NavDirections action = ProfileFragmentDirections.actionProfileFragmentToLoginFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameNew = binding.usernameET.getEditText().getText().toString();
                if(!usernameNew.equalsIgnoreCase(user.getUsername()) || profileChanged){
                    binding.usernameET.setError(null);
                    if(binding.usernameET.getEditText().getText() == null || binding.usernameET.getEditText().getText().toString().isEmpty()){
                        binding.usernameET.setError(getString(R.string.field_cant_be_empty_error));
                        binding.usernameET.requestFocus();
                    }else{
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
                        alertDialogBuilder.setMessage("Update user data according to input?");

                        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                loadingDialog.startLoadingDialog();
                                if(profileChanged){
                                    File file = new File(String.valueOf(imageUri));
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference storageRef = storage.getReference().child("profilePicture/" + user.getUid());

                                    storageRef.putFile(imageUri)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    makeToast("Image Uploaded Successfully");
                                                    Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl()
                                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            String generatedFilePath = uri.toString();

                                                            Map<String, Object> updatedValues = new HashMap<>();
                                                            updatedValues.put("username", usernameNew);
                                                            updatedValues.put("profilePicture", generatedFilePath);

                                                            FirebaseDatabase.getInstance().getReference("Users")
                                                                    .child(user.getUid())
                                                                    .updateChildren(updatedValues)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                loadingDialog.dismissDialog();
                                                                                makeToast("Change Saved");
                                                                                initData();
                                                                            } else {
                                                                                loadingDialog.dismissDialog();
                                                                                makeToast(task.getException().getMessage());
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }

                                                    )
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    loadingDialog.dismissDialog();
                                                                    makeToast(e.getMessage());
                                                                }
                                                            })

                                                            ;}
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    loadingDialog.dismissDialog();
                                                    makeToast(e.getMessage());
                                                }
                                            });

                                } else{
                                    Map<String, Object> updatedValues = new HashMap<>();
                                    updatedValues.put("username", usernameNew);

                                    FirebaseDatabase.getInstance().getReference("Users")
                                            .child(user.getUid())
                                            .updateChildren(updatedValues)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        loadingDialog.dismissDialog();
                                                        makeToast("Save changed");
                                                        initData();
                                                    } else {
                                                        loadingDialog.dismissDialog();
                                                        makeToast(task.getException().getMessage());
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }

            }
        });

        binding.changeEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemEmailDialogBinding dialogView = ItemEmailDialogBinding.inflate(getLayoutInflater());
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setView(dialogView.getRoot());

                AlertDialog dialog = builder.create();

                dialogView.oldEmailText.setText("Email : " + user.getEmail());
                dialogView.buttonChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newEmail = dialogView.editTextEmail.getText().toString();
                        if (newEmail.isEmpty() || newEmail == null) {
                            dialogView.editTextEmail.setError(getString(R.string.field_cant_be_empty_error));
                            dialogView.editTextEmail.requestFocus();
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                            dialogView.editTextEmail.setError(getString(R.string.email_must_valid));
                            dialogView.editTextEmail.requestFocus();
                        } else {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());

                            alertDialogBuilder.setMessage("Change Email?");

                            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInside, int which) {
                                    dialog.dismiss();
                                    dialogInside.dismiss();
                                    ItemPasswordOldBinding oldDialogView = ItemPasswordOldBinding.inflate(getLayoutInflater());
                                    AlertDialog.Builder oldBuilder = new AlertDialog.Builder(requireContext()).setView(oldDialogView.getRoot());
                                    AlertDialog oldDialog = oldBuilder.create();

                                    oldDialogView.textViewTitle.setText("Enter password to confirm change");

                                    oldDialogView.buttonConfirm.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String oldPassword = oldDialogView.editTextPasswordOld.getText().toString();
                                            if (oldPassword.isEmpty()) {
                                                oldDialogView.editTextPasswordOld.setError(getString(R.string.field_cant_be_empty_error));
                                                oldDialogView.editTextPasswordOld.requestFocus();
                                            } else {
                                                loadingDialog.startLoadingDialog();
                                                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
                                                auth.signInWithEmailAndPassword(user.getEmail(), oldPassword)
                                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                if (task.isSuccessful()) {
                                                                    auth.getCurrentUser().reauthenticate(credential)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> taskCredential) {
                                                                                    if (taskCredential.isSuccessful()) {
                                                                                        auth.getCurrentUser().updateEmail(newEmail)
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> taskUpdate) {
                                                                                                        if (taskUpdate.isSuccessful()) {
                                                                                                            HashMap<String, Object> updatedValues = new HashMap<>();
                                                                                                            updatedValues.put("email", newEmail);
                                                                                                            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).updateChildren(updatedValues)
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> taskDatabase) {
                                                                                                                            if (taskDatabase.isSuccessful()) {
                                                                                                                                loadingDialog.dismissDialog();
                                                                                                                                oldDialog.dismiss();

                                                                                                                                makeToast("Email changed");
                                                                                                                                initData();
                                                                                                                            } else {
                                                                                                                                loadingDialog.dismissDialog();
                                                                                                                                makeToast(taskDatabase.getException().getMessage());
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                        } else {
                                                                                                            loadingDialog.dismissDialog();
                                                                                                            makeToast(taskUpdate.getException().getMessage());
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    } else {
                                                                                        loadingDialog.dismissDialog();
                                                                                        makeToast(taskCredential.getException().getMessage());
                                                                                    }
                                                                                }
                                                                            });
                                                                } else {
                                                                    loadingDialog.dismissDialog();
                                                                    makeToast(task.getException().getMessage());
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });

                                    oldDialog.show();
                                }
                            });

                            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInside, int which) {
                                    dialogInside.dismiss();
                                }
                            });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    }
                });

                dialog.show();
            }
        });


//        binding.changePasswordBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        binding.changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemPasswordNewBinding dialogView = ItemPasswordNewBinding.inflate(getLayoutInflater());
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setView(dialogView.getRoot());

                AlertDialog dialog = builder.create();

                dialogView.buttonChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newPassword = dialogView.editTextPassword.getText().toString();
                        if (newPassword.isEmpty() || newPassword == null) {
                            dialogView.editTextPassword.setError(getString(R.string.field_cant_be_empty_error));
                            dialogView.editTextPassword.requestFocus();
                        } else {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());

                            alertDialogBuilder.setMessage("Change password?");

                            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInside, int which) {
                                    dialog.dismiss();
                                    dialogInside.dismiss();
                                    ItemPasswordOldBinding oldDialogView = ItemPasswordOldBinding.inflate(getLayoutInflater());
                                    AlertDialog.Builder oldBuilder = new AlertDialog.Builder(requireContext()).setView(oldDialogView.getRoot());
                                    AlertDialog oldDialog = oldBuilder.create();

                                    oldDialogView.buttonConfirm.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String oldPassword = oldDialogView.editTextPasswordOld.getText().toString();
                                            if (oldPassword.isEmpty()) {
                                                oldDialogView.editTextPasswordOld.setError(getString(R.string.field_cant_be_empty_error));
                                                oldDialogView.editTextPasswordOld.requestFocus();
                                            } else {
                                                loadingDialog.startLoadingDialog();
                                                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
                                                auth.getCurrentUser().reauthenticate(credential)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    auth.getCurrentUser().updatePassword(newPassword)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> taskUpdate) {
                                                                                    if (taskUpdate.isSuccessful()) {
                                                                                        loadingDialog.dismissDialog();
                                                                                        oldDialog.dismiss();

                                                                                        makeToast("Password changed");
                                                                                    } else {
                                                                                        loadingDialog.dismissDialog();
                                                                                        makeToast(taskUpdate.getException().getMessage());
                                                                                    }
                                                                                }
                                                                            });
                                                                } else {
                                                                    loadingDialog.dismissDialog();
                                                                    makeToast(task.getException().getMessage());
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });

                                    oldDialog.show();
                                }
                            });

                            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInside, int which) {
                                    dialogInside.dismiss();
                                }
                            });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    }
                });

                dialog.show();
            }
        });


        binding.profileCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(ProfileFragment.this)
                        .crop(1f,1f)	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            imageUri = data.getData();

            Glide.with(requireContext())
                    .load(imageUri)
                    .centerCrop()
                    .override(200,200)
                    .into(binding.profilePictureIV);

            profileChanged = true;
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            makeToast(ImagePicker.getError(data));
            makeToast("call2");

        } else {
            makeToast("Task Cancelled");
        }
    }

    private final ActivityResultLauncher<Intent> startForProfileImageResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                int resultCode = result.getResultCode();
                Intent data = result.getData();

                if (resultCode == Activity.RESULT_OK) {
                    Uri fileUri = data.getData();

                    imageUri = fileUri;
                    makeToast(imageUri.toString());
                    Glide.with(requireContext())
                            .load(imageUri)
                            .centerCrop()
                            .override(200,200)
                            .into(binding.profilePictureIV);

                    profileChanged = true;
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    makeToast(ImagePicker.getError(data));
                } else {
                    makeToast("Task Cancelled");
                }
            });



    private void makeToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}