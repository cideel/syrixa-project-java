package rpl.android.syrixaproject.ui.project;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.data.model.Groups;
import rpl.android.syrixaproject.data.model.MemberChecked;
import rpl.android.syrixaproject.data.model.Project;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.FragmentAddProjectBinding;
import rpl.android.syrixaproject.databinding.FragmentGroupBinding;
import rpl.android.syrixaproject.ui.create.CreateGroupFragmentDirections;
import rpl.android.syrixaproject.ui.group.BottomSheetMemberFragment;
import rpl.android.syrixaproject.ui.group.GroupFragment;
import rpl.android.syrixaproject.ui.group.GroupFragmentArgs;
import rpl.android.syrixaproject.ui.group.GroupFragmentDirections;
import rpl.android.syrixaproject.ui.group.GroupMemberAdapter;
import rpl.android.syrixaproject.ui.helper.LoadingDialog;


public class AddProjectFragment extends Fragment implements BottomSheetMemberFragment.OnItemClickListener {


    private FragmentAddProjectBinding binding;

    private LoadingDialog loadingDialog;

    private List<MemberChecked> listMemberChecked = new ArrayList<>();

    private BottomSheetMemberFragment bottomSheetFragment;

    private List<String> assignedUid = new ArrayList<>();
    private Uri projectUri;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddProjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = new LoadingDialog(requireContext());
        AddProjectFragmentArgs args = AddProjectFragmentArgs.fromBundle(getArguments());
        Groups groups = args.getGroupDataProject();
        initData(groups);
        initButton(groups);
    }

    private static final int STORAGE_PERMISSION_CODE = 1;


    private Boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    private void initData(Groups groups) {
        listMemberChecked.clear();
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
                            if(groups.getMemberUid().contains(singleItem.getUid()) && !singleItem.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                MemberChecked memberChecked = new MemberChecked(singleItem, false);
                                listMemberChecked.add(memberChecked);
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

    private void initButton(Groups groups) {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = AddProjectFragmentDirections.actionAddProjectFragmentToGroupFragment(groups);
                Navigation.findNavController(view).navigate(action);
            }
        });

        binding.projectDateET.getEditText().setKeyListener(null);
        binding.projectPersonET.getEditText().setKeyListener(null);
        binding.projectFileET.getEditText().setKeyListener(null);

        binding.projectFileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFilePicker();
            }
        });

        binding.projectPersonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetFragment = new BottomSheetMemberFragment(listMemberChecked, requireContext(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
                bottomSheetFragment.setOnItemClickListener(AddProjectFragment.this::onItemClicked);
            }
        });

        binding.projectDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();

                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String formattedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear);
                                binding.projectDateET.getEditText().setText(formattedDate);
                            }
                        },
                        year,
                        month,
                        day
                );
                datePickerDialog.show();
            }
        });

        binding.createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.projectNameET.setError(null);
                binding.taskDescET.setError(null);
                binding.projectPersonET.setError(null);
                binding.projectDateET.setError(null);
                binding.projectFileET.setError(null);
                if(binding.projectNameET.getEditText().getText() == null || binding.projectNameET.getEditText().getText().toString().isEmpty()){
                    binding.projectNameET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.projectNameET.requestFocus();
                }else if(binding.taskDescET.getEditText().getText() == null || binding.taskDescET.getEditText().getText().toString().isEmpty()){
                    binding.taskDescET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.taskDescET.requestFocus();
                }else if(binding.projectPersonET.getEditText().getText() == null || binding.projectPersonET.getEditText().getText().toString().isEmpty()){
                    binding.projectPersonET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.projectPersonET.requestFocus();
                }else if(binding.projectDateET.getEditText().getText() == null || binding.projectDateET.getEditText().getText().toString().isEmpty()){
                    binding.projectDateET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.projectDateET.requestFocus();
                }else if(binding.projectFileET.getEditText().getText() == null || binding.projectDateET.getEditText().getText().toString().isEmpty()){
                    binding.projectFileET.setError(getString(R.string.field_cant_be_empty_error));
                    binding.projectFileET.requestFocus();
                }else{
                    loadingDialog.startLoadingDialog();
                    String projectName = binding.projectNameET.getEditText().getText().toString();
                    String projectDesc = binding.taskDescET.getEditText().getText().toString();
                    String date = binding.projectDateET.getEditText().getText().toString();
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference taskRef = rootRef.child("Tasks");
                    String key = taskRef.push().getKey();
                    String fileName = queryName(getActivity().getContentResolver(), projectUri);

                    File file = new File(String.valueOf(projectUri));
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference().child("tasksFile/" + key+"/"+fileName);

                    storageRef.putFile(projectUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    makeToast("Task file Uploaded Successfully");
                                    Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                      @Override
                                                                      public void onSuccess(Uri uri) {
                                                                          String generatedFilePath = uri.toString();

                                                                          Project project = new Project(groups.getId(), key, projectName, projectDesc, groups.getLeaderUid(), assignedUid, date, generatedFilePath, fileName);
                                                                          taskRef.child(key).setValue(project)
                                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                                          loadingDialog.dismissDialog();
                                                                                          if (task.isSuccessful()) {
                                                                                              makeToast("Task Created");
                                                                                              NavDirections action = AddProjectFragmentDirections.actionAddProjectFragmentToGroupFragment(groups);
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCodes, Intent data) {
        super.onActivityResult(requestCode, resultCodes, data);

        if(requestCode==100 && resultCodes == RESULT_OK && data!=null){
            Uri uri = data.getData();

            projectUri = uri;
            binding.projectFileET.getEditText().setText(queryName(requireActivity().getContentResolver(), uri));
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

    private void addThemToView(ArrayList<Uri> docPaths) {
        binding.projectFileET.getEditText().setText(docPaths.size() + " File Selected");
        makeToast(docPaths.toString());
    }

    private void makeToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemClicked(List<MemberChecked> memberCheckedList,View view) {
        loadingDialog.startLoadingDialog();
        assignedUid.clear();
        listMemberChecked = memberCheckedList;
        for(MemberChecked checked: memberCheckedList){
            if(checked.getChecked()){
                assignedUid.add(checked.getUser().getUid());
            }
        }
        if(assignedUid.isEmpty()){
            binding.projectPersonET.getEditText().setText(null);
        }else{
            binding.projectPersonET.getEditText().setText(assignedUid.size() + " person selected.");
        }
        bottomSheetFragment.dismiss();
        loadingDialog.dismissDialog();
    }
}