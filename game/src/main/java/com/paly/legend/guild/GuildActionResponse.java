package com.paly.legend.guild;

public class GuildActionResponse {

    private boolean success;
    private String message;
    private GuildDetailResponse guild;

    public GuildActionResponse() {
    }

    public GuildActionResponse(boolean success, String message, GuildDetailResponse guild) {
        this.success = success;
        this.message = message;
        this.guild = guild;
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

    public GuildDetailResponse getGuild() {
        return guild;
    }

    public void setGuild(GuildDetailResponse guild) {
        this.guild = guild;
    }
}
