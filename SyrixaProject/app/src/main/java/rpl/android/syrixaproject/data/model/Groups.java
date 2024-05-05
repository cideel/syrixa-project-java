package rpl.android.syrixaproject.data.model;

import java.io.Serializable;
import java.util.List;


public class Groups implements Serializable {

    private String id;
    private String name;
    private String desc;
    private String leaderUid;
    private List<String> memberUid;

    public Groups(){

    }

    // Constructor
    public Groups(String id, String name, String desc, String leaderUid, List<String> memberUid) {
        this.name = name;
        this.desc = desc;
        this.leaderUid = leaderUid;
        this.memberUid = memberUid;
        this.id = id;
    }

    // Getter and Setter methods (if needed)

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLeaderUid() {
        return leaderUid;
    }

    public void setLeaderUid(String leaderUid) {
        this.leaderUid = leaderUid;
    }

    public List<String> getMemberUid() {
        return memberUid;
    }

    public void setMemberUid(List<String> memberUid) {
        this.memberUid = memberUid;
    }
}
