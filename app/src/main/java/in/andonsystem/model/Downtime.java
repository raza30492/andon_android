package in.andonsystem.model;

/**
 * Created by Md Zahid Raza on 26/06/2016.
 */
public class Downtime{
    private String probName;
    private String deptName;
    private int line;
    private int downtime;

    public Downtime(String probName, String deptName,int line, int downtime) {
        this.deptName = deptName;
        this.probName = probName;
        this.line = line;
        this.downtime = downtime;
    }

    public String getProbName() {
        return probName;
    }

    public String getDeptName() {
        return deptName;
    }

    public int getDowntime() {
        return downtime;
    }

    public int getLine() {
        return line;
    }
}
