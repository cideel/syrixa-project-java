package rpl.android.syrixaproject.ui.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.reflect.Member;
import java.util.List;

import rpl.android.syrixaproject.R;
import rpl.android.syrixaproject.data.model.MemberChecked;
import rpl.android.syrixaproject.data.model.User;
import rpl.android.syrixaproject.databinding.ItemLayoutMemberCheckboxBinding;
import rpl.android.syrixaproject.databinding.ItemLayoutPersonGroupBinding;


public class AddMemberCheckedAdapter extends RecyclerView.Adapter<AddMemberCheckedAdapter.ViewHolder> {
    private final List<MemberChecked> dataList;
    private final LayoutInflater layoutInflater;

    private final Context contexts;

    private String currentUid;

    // Constructor
    public AddMemberCheckedAdapter(Context context, List<MemberChecked> dataList, String currentUid) {
        this.layoutInflater = LayoutInflater.from(context);
        this.contexts = context;
        this.dataList = dataList;
        this.currentUid = currentUid;
    }

    @NonNull
    @Override
    public AddMemberCheckedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ViewBinding
        ItemLayoutMemberCheckboxBinding binding = ItemLayoutMemberCheckboxBinding.inflate(layoutInflater, parent, false);
        return new AddMemberCheckedAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddMemberCheckedAdapter.ViewHolder holder, int position) {
        MemberChecked member = dataList.get(position);

        if(member.getUser().getUid().equals(currentUid)){
            holder.binding.memberCheckbox.setVisibility(View.INVISIBLE);
        }else {
            holder.binding.memberCheckbox.setVisibility(View.VISIBLE);
        }
       holder.binding.teamMemberTV.setText(member.getUser().getUsername());
       holder.binding.memberCheckbox.setChecked(member.getChecked());
       holder.binding.memberCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               dataList.get(position).setChecked(b);
           }
       });
       Glide.with(contexts).load(member.getUser().getProfilePicture()).override(200,200).into(holder.binding.profilePictureMemberIv);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLayoutMemberCheckboxBinding binding;

        public ViewHolder(ItemLayoutMemberCheckboxBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public List<MemberChecked> getData(){
        return dataList;
    }
}
