package rpl.android.syrixaproject.ui.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.data.model.Groups;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.ItemLayoutGroupsBinding;
import rpl.android.syrixaproject.databinding.ItemLayoutPersonGroupBinding;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
    private final List<User> dataList;
    private final LayoutInflater layoutInflater;

    private final Boolean isLeader;

    private final Context contexts;

    private final String currentUid;

    private GroupMemberAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(GroupMemberAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // Constructor
    public GroupMemberAdapter(Context context, String currentUid, Boolean isLeader, List<User> dataList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.isLeader = isLeader;
        this.contexts = context;
        this.currentUid = currentUid;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public GroupMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ViewBinding
        ItemLayoutPersonGroupBinding binding = ItemLayoutPersonGroupBinding.inflate(layoutInflater, parent, false);
        return new GroupMemberAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberAdapter.ViewHolder holder, int position) {
        User user = dataList.get(position);

        holder.binding.teamMemberTV.setText(user.getUsername());
        // Set data to the TextView using ViewBinding
        Glide.with(contexts)
                .load(user.getProfilePicture())
                .centerCrop()
                .override(200,200)
                .placeholder(R.drawable.ic_default_avatar)
                .into(holder.binding.profilePictureMemberIv);
        if(user.getUid().equals(currentUid)){
            holder.binding.memberCommandIV.setVisibility(View.INVISIBLE);
        }else if(isLeader){
            holder.binding.memberCommandIV.setVisibility(View.VISIBLE);
        }else{
            holder.binding.memberCommandIV.setVisibility(View.INVISIBLE);
        }
        holder.binding.memberCommandIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(user, view);
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
        private final ItemLayoutPersonGroupBinding binding;

        public ViewHolder(ItemLayoutPersonGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(User user, View view);
    }
}
