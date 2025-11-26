package com.avatar.avatar_online.DTOs;

import java.util.List;

public class HistoryDataWrapperDTO {
    private List<HistoryDataDTO> data;

    public HistoryDataWrapperDTO() {}

    public List<HistoryDataDTO> getData() {
        return data;
    }

    public void setData(List<HistoryDataDTO> data) {
        this.data = data;
    }
}
