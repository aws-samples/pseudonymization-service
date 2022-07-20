package com.amazonaws.samples.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public
class PseudonymRequest {
    public String[] identifiers;
}
