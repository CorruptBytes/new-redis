package com.example.resp;

import lombok.Getter;

@Getter
public class SimpleString implements Resp {
    public static final SimpleString OK = new SimpleString("OK");
    public static final SimpleString PONG = new SimpleString("PONG");
    private final String content;

    public SimpleString(String content) {
        this.content = content;
    }
}
