package bloodbank.model;

import java.time.LocalDateTime;

public class BloodRequest {
    private int requestId;
    private int hospitalId;
    private String requiredBloodType;
    private String requiredComponent;
    private int unitsRequested;
    private String urgencyLevel;
    private LocalDateTime requestTimestamp;
    private String requestStatus;

    public BloodRequest() {}

    public BloodRequest(int requestId, int hospitalId, String requiredBloodType,
            String requiredComponent, int unitsRequested, String urgencyLevel,
            LocalDateTime requestTimestamp, String requestStatus) {
        this.requestId = requestId;
        this.hospitalId = hospitalId;
        this.requiredBloodType = requiredBloodType;
        this.requiredComponent = requiredComponent;
        this.unitsRequested = unitsRequested;
        this.urgencyLevel = urgencyLevel;
        this.requestTimestamp = requestTimestamp;
        this.requestStatus = requestStatus;
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int v) { requestId = v; }
    public int getHospitalId() { return hospitalId; }
    public void setHospitalId(int v) { hospitalId = v; }
    public String getRequiredBloodType() { return requiredBloodType; }
    public void setRequiredBloodType(String v) { requiredBloodType = v; }
    public String getRequiredComponent() { return requiredComponent; }
    public void setRequiredComponent(String v) { requiredComponent = v; }
    public int getUnitsRequested() { return unitsRequested; }
    public void setUnitsRequested(int v) { unitsRequested = v; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String v) { urgencyLevel = v; }
    public LocalDateTime getRequestTimestamp() { return requestTimestamp; }
    public void setRequestTimestamp(LocalDateTime v) { requestTimestamp = v; }
    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String v) { requestStatus = v; }
}
