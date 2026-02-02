package com.example.resp;

import lombok.Getter;

@Getter
public class RespInt implements Resp {
    private final long value;

    public RespInt(long value) {
        this.value = value;
    }
}
