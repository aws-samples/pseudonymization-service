package com.amazonaws.samples.utils;

import lombok.Data;
import lombok.NonNull;

@Data
public class ReidentifyResponse {
    @NonNull  public String[] identifiers;
}