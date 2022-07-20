package com.amazonaws.samples.utils;

import lombok.Data;
import lombok.NonNull;

@Data
public class PseudonymResponse {
    @NonNull  public String[] pseudonyms;
}