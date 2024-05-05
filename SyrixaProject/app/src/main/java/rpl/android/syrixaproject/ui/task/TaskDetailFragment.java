package rpl.android.syrixaproject.ui.task;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.data.model.Groups;
import rpl.android.syrixaproject.data.model.Project;
import rpl.android.syrixaproject.data.model.SubmittedProject;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.DialogFileSubmitBinding;
import rpl.android.syrixaproject.databinding.FragmentTaskDetailBinding;
import rpl.android.syrixaproject.databinding.ItemEmailDialogBinding;
import rpl.android.syrixaproject.ui.helper.LoadingDialog;
import rpl.android.syrixaproject.ui.project.AddProjectFragmentDirections;

public class TaskDetailFragment extends Fragment implements  SubmittedFileAdapter.OnItemClickListener{

    private FragmentTaskDetailBinding binding;

    private LoadingDialog loadingDialog;

    private Uri projectUri;

    private DialogFileSubmitBinding dialogView;

    private Groups groups;

    private Project projects;

    private User makerUser;

    private List<User> assignedUser = new ArrayList<>();

    private FirebaseUser currentUser;

    private List<SubmittedProject> submittedData = new ArrayList<>();


    private Boolean isUserAssigned = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.startLoadingDialog();
        TaskDetailFragmentArgs args = TaskDetailFragmentArgs.fromBundle(getArguments());
        groups = args.getGroupDataTaskDetail();
        projects = args.getTaskProject();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        initDataAssigned();
    }

    private void makeToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        binding.groupNameTV.setText(groups.getName());
        binding.taskNameTV.setText(projects.getName());
        binding.dueDateTV.setText(projects.getDate());
        binding.createdMemberTV.setText(makerUser.getUsername());
        Glide.with(requireContext()).load(makerUser.getProfilePicture()).override(200,200).centerCrop().into(binding.profilePictureMemberIv);
        binding.descTV.setText(projects.getDesc());
        binding.fileNameTV.setText(projects.getFileName());

        if(projects.getAssignedUid().contains(currentUser.getUid()) || projects.getMakerUid().equals(currentUser.getUid())){
            initSubmitData();
        }else{
            binding.subStatusTV.setText("Not Assigned");
        }

        binding.fileNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(projects.getFileLink()));
                startActivity(browserIntent);
            }
        });


        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUploadDialog();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = TaskDetailFragmentDirections.actionTaskDetailFragmentToTaskListFragment(groups);
                Navigation.findNavController(view).navigate(action);
            }
        });

    }

    private void initSubmitData() {
        loadingDialog.startLoadingDialog();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference submitRef = rootRef.child("Submitted");
        submitRef.orderByChild("projectId").equalTo(projects.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        SubmittedProject singleItem = postSnapshot.getValue(SubmittedProject.class);
                        if (singleItem != null) {
                            submittedData.add(singleItem);
                        }else{
                            loadingDialog.dismissDialog();
                        }
                    }
                    initSubmittedView();
                    submitRef.removeEventListener(this);
                    loadingDialog.dismissDialog();
                } else {
                    initSubmittedView();
                    submitRef.removeEventListener(this);
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }

    private void initSubmittedView() {
        if(projects.getAssignedUid().contains(currentUser.getUid())){
            SubmittedProject submittedProject = getSubmittedProjectByUser();
            if(submittedData.isEmpty() || submittedProject==null){
                binding.submitBtn.setVisibility(View.VISIBLE);
                binding.subStatusTV.setText("No Attempt");
            }else{
                binding.submitBtn.setVisibility(View.INVISIBLE);
                binding.subStatusTV.setText("Submitted");
                binding.fileSubmissionTV.setVisibility(View.VISIBLE);
                binding.fileSubNameTV.setText(submittedProject.getFileName());
                binding.fileSubNameTV.setVisibility(View.VISIBLE);
                binding.fileSubNameTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(submittedProject.getFileLink()));
                        startActivity(browserIntent);
                    }
                });

                binding.dateSubmissionTV.setVisibility(View.VISIBLE);
                binding.fileSubDateTV.setText(submittedProject.getDate());
                binding.fileSubDateTV.setVisibility(View.VISIBLE);
            }
        }else if(projects.getMakerUid().equals(currentUser.getUid())){
            if(submittedData.isEmpty()){
                binding.subStatusTV.setText("No Attempt");
            }else{
                binding.subStatusTV.setText(submittedData.size() + " person submitted");
                binding.submitedHeaderTV.setVisibility(View.VISIBLE);
                binding.submittedfileRV.setVisibility(View.VISIBLE);
                SubmittedFileAdapter submitAdapter = new SubmittedFileAdapter(requireContext(), submittedData, assignedUser);

                binding.submittedfileRV.setLayoutManager(new LinearLayoutManager(requireContext()));
                binding.submittedfileRV.setAdapter(submitAdapter);
                submitAdapter.setOnItemClickListener(this);
            }

        }
    }

    private boolean isUserInSubmittedProjects() {
        for (SubmittedProject project : submittedData) {
            // Memeriksa apakah currentUserUid terdapat di dalam submitUid objek tersebut
            if (currentUser.getUid().equals(project.getSubmitUid())) {
                return true; // User ditemukan
            }
        }
        return false; // User tidak ditemukan
    }

    private SubmittedProject getSubmittedProjectByUser() {
        for (SubmittedProject project : submittedData) {
            if (currentUser.getUid().equals(project.getSubmitUid())) {
                return project;
            }
        }
        return null;
    }

    private void showUploadDialog(){
        dialogView = DialogFileSubmitBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setView(dialogView.getRoot());

        AlertDialog dialog = builder.create();

        dialogView.closeLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialogView.uploadFileBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFilePicker();
            }
        });

        dialogView.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogView.textSubmitET.setError(null);
                if(dialogView.textSubmitET.getEditText().getText() == null || dialogView.textSubmitET.getEditText().getText().toString().isEmpty()){
                    dialogView.textSubmitET.setError(getString(R.string.field_cant_be_empty_error));
                    dialogView.textSubmitET.requestFocus();
                }else if(projectUri == null){
                    makeToast("Submit a file first!");
                }else {
                    loadingDialog.startLoadingDialog();
                    String textDesc = dialogView.textSubmitET.getEditText().getText().toString();
                    String fileName = queryName(requireActivity().getContentResolver(), projectUri);
                    LocalDate currentDate = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault());
                    String formattedDate = currentDate.format(formatter);

                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference submitRef = rootRef.child("Submitted");
                    String key = submitRef.push().getKey();


                    File file = new File(String.valueOf(projectUri));
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference().child("submittedTask/" + projects.getId()+"/"+currentUser.getUid()+"/"+ fileName);

                    storageRef.putFile(projectUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    makeToast("Submitted file Uploaded Successfully");
                                    Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                      @Override
                                                                      public void onSuccess(Uri uri) {
                                                                          String generatedFilePath = uri.toString();

                                                                          SubmittedProject submittedProject = new SubmittedProject(projects.getId(), fileName, generatedFilePath, currentUser.getUid(), formattedDate);

                                                                          submitRef.child(key).setValue(submittedProject)
                                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                                          loadingDialog.dismissDialog();
                                                                                          if (task.isSuccessful()) {
                                                                                              makeToast("Submitted");
                                                                                              dialog.dismiss();
                                                                                              NavDirections action = TaskDetailFragmentDirections.actionTaskDetailFragmentSelf(groups, projects);
                                                                                              Navigation.findNavController(requireView()).navigate(action);
                                                                                          } else {
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
                }
            }
        });

        dialog.show();
    }

    private void startFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try{
            startActivityForResult(Intent.createChooser(intent, "Select file"), 100);
        }catch(Exception e){
            makeToast(e.getMessage());
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCodes, Intent data) {
        super.onActivityResult(requestCode, resultCodes, data);

        if(requestCode==100 && resultCodes == RESULT_OK && data!=null){
            Uri uri = data.getData();

            projectUri = uri;
            dialogView.submittedFileNameDialogTV.setText(queryName(requireActivity().getContentResolver(), uri));
            dialogView.submittedFileNameDialogTV.setVisibility(View.VISIBLE);
            dialogView.fileSymbol.setVisibility(View.INVISIBLE);
        }
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    private void initDataAssigned() {
        assignedUser.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("Users").orderByChild("uid");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        User singleItem = postSnapshot.getValue(User.class);
                        if (singleItem != null) {
                            if(projects.getAssignedUid().contains(singleItem.getUid())){
                                assignedUser.add(singleItem);
                            }else if(projects.getMakerUid().equals(singleItem.getUid())){
                                makerUser = singleItem;
                            }
                        }
                    }
                    loadingDialog.dismissDialog();
                    initView();
                    startRecylerViewAssigned();
                    ref.removeEventListener(this);
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

    private void startRecylerViewAssigned(){
        AssignedUserAdapter assignedAdapter = new AssignedUserAdapter(requireContext(), assignedUser);

        binding.assignedRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.assignedRV.setAdapter(assignedAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(String link, View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }
}