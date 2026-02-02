package com.example.resp;

import lombok.Getter;

@Getter
public class RespArray implements Resp {
    private final Resp[] array;

    public RespArray(Resp[] array) {
        this.array = array;
    }
}
