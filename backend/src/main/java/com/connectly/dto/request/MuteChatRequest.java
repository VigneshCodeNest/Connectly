package com.connectly.dto.request;

import com.connectly.constant.MuteOption;

import java.time.LocalDateTime;

public class MuteChatRequest {

    private MuteOption duration = MuteOption.UNTIL_MANUALLY_UNMUTED;
    private Integer customHours;
    private Integer customMinutes;

    public MuteChatRequest() {}

    public MuteChatRequest(MuteOption duration) {
        this.duration = duration;
    }

    public MuteOption getDuration() { return duration; }
    public void setDuration(MuteOption duration) { this.duration = duration; }

    public Integer getCustomHours() { return customHours; }
    public void setCustomHours(Integer customHours) { this.customHours = customHours; }

    public Integer getCustomMinutes() { return customMinutes; }
    public void setCustomMinutes(Integer customMinutes) { this.customMinutes = customMinutes; }

    public LocalDateTime calculateMutedUntil() {
        if (customMinutes != null && customMinutes > 0) {
            return LocalDateTime.now().plusMinutes(customMinutes);
        }
        if (customHours != null && customHours > 0) {
            return LocalDateTime.now().plusHours(customHours);
        }
        if (duration == null) {
            return null; // Muted indefinitely
        }
        return switch (duration) {
            case ONE_HOUR -> LocalDateTime.now().plusHours(1);
            case EIGHT_HOURS -> LocalDateTime.now().plusHours(8);
            case ONE_WEEK -> LocalDateTime.now().plusDays(7);
            case UNTIL_MANUALLY_UNMUTED, INDEFINITE, ALWAYS -> null;
        };
    }
}
