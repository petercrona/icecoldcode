package com.icecoldcode.core;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class Clock {

    public Instant now() {
        return Instant.now();
    }

}
