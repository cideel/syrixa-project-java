package rpl.android.syrixaproject.ui.task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import rpl.android.syrixaproject.data.model.Groups;
import rpl.android.syrixaproject.data.model.Project;
import rpl.android.syrixaproject.databinding.ItemTaskListBinding;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private final List<Project> dataList;
    private final LayoutInflater layoutInflater;

    private TaskListAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(TaskListAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // Constructor
    public TaskListAdapter(Context context, List<Project> dataList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public TaskListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ViewBinding
        ItemTaskListBinding binding = ItemTaskListBinding.inflate(layoutInflater, parent, false);
        return new TaskListAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        Project project = dataList.get(position);
        // Set data to the TextView using ViewBinding
        holder.binding.taskNameTV.setText(project.getName());
        holder.binding.taskDateTV.setText(project.getDate());
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(project, view);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {



























        
        private final ItemTaskListBinding binding;

        public ViewHolder(ItemTaskListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Project project, View view);
    }
}
