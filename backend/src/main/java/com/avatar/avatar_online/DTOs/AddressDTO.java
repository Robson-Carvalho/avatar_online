package com.avatar.avatar_online.DTOs;

public class AddressDTO {

    private String address;

    public AddressDTO(String address) {
        this.address = address;
    }

    public AddressDTO() {};

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
