package rpl.android.syrixaproject.ui.task;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.data.model.Groups;
import rpl.android.syrixaproject.data.model.Project;
import rpl.android.syrixaproject.databinding.FragmentGroupBinding;
import rpl.android.syrixaproject.databinding.FragmentTaskListBinding;
import rpl.android.syrixaproject.ui.group.GroupFragmentArgs;
import rpl.android.syrixaproject.ui.helper.LoadingDialog;
import rpl.android.syrixaproject.ui.home.HomeFragmentDirections;
import rpl.android.syrixaproject.ui.home.TaskGroupsAdapter;


public class TaskListFragment extends Fragment implements TaskListAdapter.OnItemClickListener{

    private FragmentTaskListBinding binding;

    private TaskListAdapter adapter;

    private LoadingDialog loadingDialog;

    private List<Project> projectList = new ArrayList<>();

    private Groups groups;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTaskListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = new LoadingDialog(requireContext());
        TaskListFragmentArgs args = TaskListFragmentArgs.fromBundle(getArguments());
        groups = args.getGroupDataTask();
        initData();
    }

    private void initData() {
        binding.groupNameTV.setText(groups.getName());
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = TaskListFragmentDirections.actionTaskListFragmentToGroupFragment(groups);
                Navigation.findNavController(view).navigate(action);
            }
        });
        projectList.clear();
        loadingDialog.startLoadingDialog();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Tasks").orderByChild("groupId").equalTo(groups.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Project singleItem = postSnapshot.getValue(Project.class);
                        if (singleItem != null) {
                            projectList.add(singleItem);
                        }
                    }
                    startRecylerView();
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
        adapter = new TaskListAdapter(requireContext(), projectList);

        binding.taskListRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.taskListRV.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
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
    public void onItemClick(Project project, View view) {
        NavDirections action = TaskListFragmentDirections.actionTaskListFragmentToTaskDetailFragment(groups, project);
        Navigation.findNavController(view).navigate(action);
    }
}