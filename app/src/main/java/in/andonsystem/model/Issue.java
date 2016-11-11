package in.andonsystem.model;

/**
 * Created by Md Zahid Raza on 17/06/2016.
 */
public class Issue {
    private int id;
    private int line;
    private int secId;
    private int deptId;
    private int probId;
    private String critical;
    private String operatorNo;
    private String desc;
    private long raisedAt;
    private long ackAt;
    private long fixAt;
    private int raisedBy;
    private int ackBy;
    private int fixBy;
    private int processingAt;
    private  int status;
    private int seekHelp;

    public Issue(){}

    public Issue(int id, int line, int secId, int deptId, int probId, String critical, String operatorNo, String desc, long raisedAt, long ackAt, long fixAt, int raisedBy, int ackBy,int fixBy,int processingAt, int status, int seekHelp) {
        this.id = id;
        this.line = line;
        this.secId = secId;
        this.deptId = deptId;
        this.probId = probId;
        this.critical = critical;
        this.operatorNo = operatorNo;
        this.desc = desc;
        this.raisedAt = raisedAt;
        this.ackAt = ackAt;
        this.fixAt = fixAt;
        this.raisedBy = raisedBy;
        this.ackBy = ackBy;
        this.fixBy = fixBy;
        this.processingAt = processingAt;
        this.status = status;
        this.seekHelp = seekHelp;
    }

    public int getSeekHelp() {
        return seekHelp;
    }

    public int getId() {
        return id;
    }

    public int getLine() {
        return line;
    }

    public int getSecId() {
        return secId;
    }

    public int getDeptId() {
        return deptId;
    }

    public int getProbId() {
        return probId;
    }

    public String getCritical() {
        return critical;
    }

    public String getOperatorNo() {
        return operatorNo;
    }

    public String getDesc() {
        return desc;
    }

    public long getRaisedAt() {
        return raisedAt;
    }

    public long getAckAt() {
        return ackAt;
    }

    public int getRaisedBy() {
        return raisedBy;
    }

    public int getStatus() {
        return status;
    }

    public int getAckBy() {
        return ackBy;
    }

    public int getFixBy() {
        return fixBy;
    }

    public long getFixAt() {
        return fixAt;
    }

    public int getProcessingAt() {
        return processingAt;
    }
}
