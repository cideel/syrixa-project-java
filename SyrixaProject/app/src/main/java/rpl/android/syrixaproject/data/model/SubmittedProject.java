package rpl.android.syrixaproject.data.model;

public class SubmittedProject {

    private String projectId;

    private String fileName;

    private String fileLink;

    private String submitUid;

    private String date;

    public SubmittedProject() {
    }

    public SubmittedProject(String projectId, String fileName, String fileLink, String submitUid, String date) {
        this.projectId = projectId;
        this.fileName = fileName;
        this.fileLink = fileLink;
        this.submitUid = submitUid;
        this.date = date;
    }

    // Getter dan Setter
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileLink() {
        return fileLink;
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }

    public String getSubmitUid() {
        return submitUid;
    }

    public void setSubmitUid(String submitUid) {
        this.submitUid = submitUid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


}
