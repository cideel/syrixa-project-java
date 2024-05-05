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
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.ItemLayoutPersonBinding;

public class AssignedUserAdapter extends RecyclerView.Adapter<AssignedUserAdapter.ViewHolder> {

    private final List<User> dataList;
    private final LayoutInflater layoutInflater;

    private Context contexts;

    // Constructor
    public AssignedUserAdapter(Context context, List<User> dataList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.dataList = dataList;
        this.contexts = context;
    }

    @NonNull
    @Override
    public AssignedUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ViewBinding
        ItemLayoutPersonBinding binding = ItemLayoutPersonBinding.inflate(layoutInflater, parent, false);
        return new AssignedUserAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignedUserAdapter.ViewHolder holder, int position) {
        User user = dataList.get(position);
        // Set data to the TextView using ViewBinding
        holder.binding.teamMemberTV.setText(user.getUsername());
        Glide.with(contexts).load(user.getProfilePicture()).centerCrop().override(150,150).into(holder.binding.profilePictureMemberIv);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLayoutPersonBinding binding;

        public ViewHolder(ItemLayoutPersonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

