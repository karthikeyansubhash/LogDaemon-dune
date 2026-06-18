package com.hp.jetadvantage.link.logdaemon.model;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;

@Keep
public class LogManagementRequestMessage {

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

        @SerializedName("traceId")
        private int traceId;

        public Details getDetails() {
            return details;
        }

        public void setDetails(Details details) {
            this.details = details;
        }

        public int getTraceId() {
            return traceId;
        }

        public void setTraceId(int traceId) {
            this.traceId = traceId;
        }
    }

    @Keep
    public static class Details {
        @SerializedName("export")
        private LogOperation export;

        @SerializedName("cleanup")
        private LogOperation cleanup;

        public LogOperation getExport() {
            return export;
        }

        public void setExport(LogOperation export) {
            this.export = export;
        }

        public LogOperation getCleanup() {
            return cleanup;
        }

        public void setCleanup(LogOperation cleanup) {
            this.cleanup = cleanup;
        }
    }

    @Keep
    public static class LogOperation {
        @SerializedName("operationCallId")
        private String operationCallId;

        @SerializedName("logType")
        private String logType;

        @SerializedName("solutionId")
        private String solutionId;

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

        public String getSolutionId() {
            return solutionId;
        }

        public void setSolutionId(String solutionId) {
            this.solutionId = solutionId;
        }
    }

    // Log type constants based on schema enum
    public static final String LOG_TYPE_ALL = "ltAll";
    public static final String LOG_TYPE_PLATFORM = "ltPlatform";
    public static final String LOG_TYPE_SOLUTION_DEBUG = "ltSolutionDebug";
    public static final String LOG_TYPE_SOLUTION_CRASH = "ltSolutionCrash";
    public static final String LOG_TYPE_DUMMY_CRASH = "ltDummyCrash";
}
