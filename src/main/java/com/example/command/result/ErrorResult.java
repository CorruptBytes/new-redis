package com.example.command.result;

import com.example.resp.Errors;
import com.example.resp.Resp;

public final class ErrorResult implements CommandResult {

    private final String message;

    public ErrorResult(String message) {
        this.message = message;
    }

    @Override
    public Resp toResp() {
        return new Errors(message);
    }
}
