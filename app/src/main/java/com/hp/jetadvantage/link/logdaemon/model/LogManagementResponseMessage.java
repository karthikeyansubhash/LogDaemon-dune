package com.hp.jetadvantage.link.logdaemon.model;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;

@Keep
public class LogManagementResponseMessage {

    @SerializedName("logManagement")
    private LogManagement logManagement;

    public LogManagement getLogManagement() {
        return logManagement;
    }

    public void setLogManagement(LogManagement logManagement) {
        this.logManagement = logManagement;
    }

    @Keep
    public static class LogManagement {
        @SerializedName("details")
        private Details details;

        public Details getDetails() {
            return details;
        }

        public void setDetails(Details details) {
            this.details = details;
        }
    }

    @Keep
    public static class Details {
        @SerializedName("exportStatus")
        private OperationStatus exportStatus;

        @SerializedName("cleanupStatus")
        private OperationStatus cleanupStatus;

        public OperationStatus getExportStatus() {
            return exportStatus;
        }

        public void setExportStatus(OperationStatus exportStatus) {
            this.exportStatus = exportStatus;
        }

        public OperationStatus getCleanupStatus() {
            return cleanupStatus;
        }

        public void setCleanupStatus(OperationStatus cleanupStatus) {
            this.cleanupStatus = cleanupStatus;
        }
    }

    @Keep
    public static class OperationStatus {
        @SerializedName("operationCallId")
        private String operationCallId;

        @SerializedName("logType")
        private String logType;

        @SerializedName("status")
        private String status;

        @SerializedName("path")
        private String path;

        @SerializedName("solutionId")
        private String solutionId;

        @SerializedName("error")
        private String error;

        public String getOperationCallId() {
            return operationCallId;
        }

        public void setOperationCallId(String operationCallId) {
            this.operationCallId = operationCallId;
        }

        public String getLogType() {
            return logType;
        }

        public void setLogType(String logType) {
            this.logType = logType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSolutionId() {
            return solutionId;
        }

        public void setSolutionId(String solutionId) {
            this.solutionId = solutionId;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    // Export status constants based on schema enum
    public static final String STATUS_COMPLETED = "esCompleted";
    public static final String STATUS_IN_PROGRESS = "esInProgress";
    public static final String STATUS_FAILED = "esFailed";

    // Cleanup status constants
    public static final String CLEANUP_STATUS_COMPLETED = "cuCompleted";
    public static final String CLEANUP_STATUS_FAILED = "cuFailed";
}
