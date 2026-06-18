package com.paly.legend.admin;

public class AdminConfigReloadResponse {

    private boolean success;
    private String message;
    private String summary;

    public AdminConfigReloadResponse() {
    }

    public AdminConfigReloadResponse(boolean success, String message, String summary) {
        this.success = success;
        this.message = message;
        this.summary = summary;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
