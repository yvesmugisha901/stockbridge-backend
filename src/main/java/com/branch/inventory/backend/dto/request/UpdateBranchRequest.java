package com.branch.inventory.backend.dto.request;

import lombok.Data;

@Data
public class UpdateBranchRequest {

    private String name;

    private String location;

    private String contactInfo; // renamed from "contactPhone" to match service's getContactInfo()
}