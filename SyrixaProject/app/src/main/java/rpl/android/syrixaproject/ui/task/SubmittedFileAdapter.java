package rpl.android.syrixaproject.ui.task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import rpl.android.syrixaproject.data.model.Project;
import rpl.android.syrixaproject.data.model.SubmittedProject;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.ItemLayoutSubmittedBinding;

public class SubmittedFileAdapter extends RecyclerView.Adapter<SubmittedFileAdapter.ViewHolder> {

    private final List<SubmittedProject> dataList;

    private final List<User> userList;
    private final LayoutInflater layoutInflater;

    private Context contexts;

    private SubmittedFileAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(SubmittedFileAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // Constructor
    public SubmittedFileAdapter(Context context, List<SubmittedProject> dataList, List<User> userList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.dataList = dataList;
        this.userList = userList;
        this.contexts = context;
    }

    @NonNull
    @Override
    public SubmittedFileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ViewBinding
        ItemLayoutSubmittedBinding binding = ItemLayoutSubmittedBinding.inflate(layoutInflater, parent, false);
        return new SubmittedFileAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmittedFileAdapter.ViewHolder holder, int position) {
        SubmittedProject project = dataList.get(position);
        // Set data to the TextView using ViewBinding
        User currentUserRV = getUserByUid(project.getSubmitUid());
        if(currentUserRV!=null){
            holder.binding.teamMemberTV.setText(currentUserRV.getUsername());
            Glide.with(contexts).load(currentUserRV.getProfilePicture()).override(200,200).centerCrop().into(holder.binding.profilePictureMemberIv);
            holder.binding.submittedDateRVTV.setText(project.getDate());
            holder.binding.submittedNameRVTV.setText(project.getFileName());
            holder.binding.submittedNameRVTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(project.getFileLink(), view);
                    }
                }
            });
        }

    }

    private User getUserByUid(String uid){
        for(User user: userList){
            if(user.getUid().equals(uid)){
                return user;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLayoutSubmittedBinding binding;

        public ViewHolder(ItemLayoutSubmittedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String link, View view);
    }
}