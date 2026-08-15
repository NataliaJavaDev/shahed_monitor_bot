package com.tgbot.shahedmonitorbot.model.admin;

public class AdminSession {

    private AdminSessionState state = AdminSessionState.IDLE;
    private String pendingSourceId;

    public AdminSessionState getState() {
        return state;
    }

    public void setState(AdminSessionState state) {
        this.state = state;
    }

    public String getPendingSourceId() {
        return pendingSourceId;
    }

    public void setPendingSourceId(String pendingSourceId) {
        this.pendingSourceId = pendingSourceId;
    }
}