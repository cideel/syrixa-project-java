package rpl.android.syrixaproject.data.model;

import java.io.Serializable;
import java.util.List;

public class Project implements Serializable {

    private String groupId;
    private String id;
    private String name;
    private String desc;
    private String makerUid;
    private List<String> assignedUid;
    private String date;
    private String fileLink;
    private String fileName;

    public Project(){
    }

    public Project(String groupId, String id, String name, String desc, String makerUid, List<String> assignedUid, String date, String fileLink, String fileName) {
        this.name = name;
        this.desc = desc;
        this.makerUid = makerUid;
        this.assignedUid = assignedUid;
        this.date = date;
        this.fileLink = fileLink;
        this.id = id;
        this.groupId = groupId;
        this.fileName = fileName;
    }

    // Getter and Setter methods (if needed)

    public String getGroupId() {
        return groupId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAssignedUid() {
        return assignedUid;
    }

    public void setAssignedUid(List<String> assignedUid) {
        this.assignedUid = assignedUid;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMakerUid() {
        return makerUid;
    }

    public void setMakerUid(String makerUid) {
        this.makerUid = makerUid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFileLink() {
        return fileLink;
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }




}
