package in.andonsystem.model;

/**
 * Created by Md Zahid Raza on 25/06/2016.
 */
public class Problem implements Comparable<Problem> {

    private int issueId;
    private String line;
    private String deptName;
    private String probName;
    private String critical;
    private String raiseTime;
    private int downtime;
    private int flag;

    public Problem(){}

    public Problem(int issueId, String line, String deptName, String probName, String critical, String raiseTime,int downtime, int flag) {
        this.issueId = issueId;
        this.line = line;
        this.deptName = deptName;
        this.probName = probName;
        this.critical = critical;
        this.raiseTime = raiseTime;
        this.downtime = downtime;
        this.flag = flag;
    }

    public int getDowntime() {
        return downtime;
    }

    public int getIssueId() {
        return issueId;
    }

    public String getLine() {
        return line;
    }

    public String getDeptName() {
        return deptName;
    }

    public String getProbName() {
        return probName;
    }

    public String getCritical() {
        return critical;
    }

    public String getRaiseTime() {
        return raiseTime;
    }

    public int getFlag() {
        return flag;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public int compareTo(Problem another) {
        int result = this.flag - another.flag;
        if(result == 0){
            result = another.issueId - this.issueId;
        }
        return result;
    }
}
