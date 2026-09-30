package com.connectly.dto.request;

import java.util.ArrayList;
import java.util.List;

public class ForwardMessageRequest {

    private List<Long> targetChatIds = new ArrayList<>();
    private List<Long> targetUserIds = new ArrayList<>();

    public ForwardMessageRequest() {}

    public ForwardMessageRequest(List<Long> targetChatIds) {
        this.targetChatIds = targetChatIds;
    }

    public List<Long> getTargetChatIds() { return targetChatIds; }
    public void setTargetChatIds(List<Long> targetChatIds) { this.targetChatIds = targetChatIds; }

    public List<Long> getTargetUserIds() { return targetUserIds; }
    public void setTargetUserIds(List<Long> targetUserIds) { this.targetUserIds = targetUserIds; }
}
