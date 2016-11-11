package in.andonsystem.model;

/**
 * Created by Md Zahid Raza on 23/06/2016.
 */
public class Notification implements Comparable<Notification> {
    private int issueId;
    private String message;
    private long time;
    private int flag;  // values: 0 - raised, 1 - acknowledged , 2 - solved

    public Notification(){}

    public Notification(int issueId, String message, long time, int flag) {
        this.issueId = issueId;
        this.message = message;
        this.time = time;
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int compareTo(Notification another) {
        int result = (int)(this.time - another.time);
        return result;
    }
}
