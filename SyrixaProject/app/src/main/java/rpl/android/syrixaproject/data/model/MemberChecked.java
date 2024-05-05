package rpl.android.syrixaproject.data.model;

import java.util.List;

public class MemberChecked {
    private User user;
    private Boolean checked;

    public MemberChecked(){

    }

    public MemberChecked(User user, Boolean checked) {
        this.user = user;
        this.checked = checked;
    }

    public User getUser() {
        return user;
    }

    public void setUser(String name) {
        this.user = user;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
