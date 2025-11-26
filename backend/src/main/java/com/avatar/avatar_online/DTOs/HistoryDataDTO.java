package com.avatar.avatar_online.DTOs;

import java.util.List;

public class HistoryDataDTO {
    private List<EventBlockDTO> data;
    private List<TimelineItemDTO> timeline;

    public HistoryDataDTO() {}

    public List<EventBlockDTO> getData() {
        return data;
    }

    public void setData(List<EventBlockDTO> data) {
        this.data = data;
    }

    public List<TimelineItemDTO> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineItemDTO> timeline) {
        this.timeline = timeline;
    }
}
