package com.samtheoracle.discovery;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;
import java.util.Optional;

@RegisterForReflection
public enum Status {
    UP,DOWN,UNKNOWN;

    public static Status from(String status) {
        return Arrays.stream(values()).filter(st->st.name().equalsIgnoreCase(status)).findFirst().orElse(UNKNOWN);
    }
}
