package rpl.android.syrixaproject.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import rpl.android.syrixaproject.data.model.Groups;
import rpl.android.syrixaproject.databinding.ItemLayoutGroupsBinding;

public class TaskGroupsAdapter extends RecyclerView.Adapter<TaskGroupsAdapter.ViewHolder> {

    private final List<Groups> dataList;
    private final LayoutInflater layoutInflater;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // Constructor
    public TaskGroupsAdapter(Context context, List<Groups> dataList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ViewBinding
        ItemLayoutGroupsBinding binding = ItemLayoutGroupsBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Groups group = dataList.get(position);
        // Set data to the TextView using ViewBinding
        holder.binding.groupNameTV.setText(group.getName());
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(group, view);
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
        private final ItemLayoutGroupsBinding binding;

        public ViewHolder(ItemLayoutGroupsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Groups groups, View view);
    }
}