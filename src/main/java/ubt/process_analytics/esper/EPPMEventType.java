package ubt.process_analytics.esper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class EPPMEventType {
    String activity;
    long timestamp;
    String caseID;

    public EPPMEventType(String activity, LocalDateTime timestamp, String caseID) {
        this.activity = activity;
        this.timestamp =  timestamp.toInstant(ZoneOffset.UTC).toEpochMilli();
        this.caseID = caseID;
    }

    public String getActivity() {
        return activity;
    }
    public void setActivity(String activity) {
        this.activity = activity;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    public String getCaseID() {
        return caseID;
    }
    public void setCaseID(String caseID) {
        this.caseID = caseID;
    }

    @Override
    public String toString() {
        return STR."EPPMEventType [activity=\{activity}, timestamp=\{timestamp}, caseID=\{caseID}]";
    }
}
