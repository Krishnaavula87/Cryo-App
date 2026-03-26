package com.cryo.freezer.dto;

import java.math.BigDecimal;

public class FreezerConfigResponse {
    private String freezerId;
    private String name;

    public FreezerConfigResponse(String freezerId, String name) {
        this.freezerId = freezerId;
        this.name = name;

    }

    public String getFreezerId() {
        return freezerId;
    }

    public String getName() {
        return name;
    }

}