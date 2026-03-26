package com.cryo.freezer.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class FreezerRegisterRequest {

    // REMOVED freezerId

    @NotBlank(message = "A display name for the freezer is required.")
    private String name;

    @NotBlank(message = "Purchase Order Number is required.")
    private String poNumber;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }


}