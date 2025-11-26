package com.avatar.avatar_online.DTOs;

import java.util.List;

public class HistoryDataDTO {
    private List<OpenPackageDTO> open_package;
    private List<SwapCardsDTO> swap_cards;
    private List<MatchRegisterDTO> match_register;
    private List<MintCardsDTO> mint_cards;
    private List<TimelineItemDTO> timeline;

    public HistoryDataDTO() {}

    public List<OpenPackageDTO> getOpen_package() {
        return open_package;
    }

    public void setOpen_package(List<OpenPackageDTO> open_package) {
        this.open_package = open_package;
    }

    public List<SwapCardsDTO> getSwap_cards() {
        return swap_cards;
    }

    public void setSwap_cards(List<SwapCardsDTO> swap_cards) {
        this.swap_cards = swap_cards;
    }

    public List<MatchRegisterDTO> getMatch_register() {
        return match_register;
    }

    public void setMatch_register(List<MatchRegisterDTO> match_register) {
        this.match_register = match_register;
    }

    public List<MintCardsDTO> getMint_cards() {
        return mint_cards;
    }

    public void setMint_cards(List<MintCardsDTO> mint_cards) {
        this.mint_cards = mint_cards;
    }

    public List<TimelineItemDTO> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineItemDTO> timeline) {
        this.timeline = timeline;
    }
}
