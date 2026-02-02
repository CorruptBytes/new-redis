package com.example.resp;

import lombok.Getter;

@Getter
public class Errors implements Resp {
    private final String content;

    public Errors(String content) {
        this.content = content;
    }
}
