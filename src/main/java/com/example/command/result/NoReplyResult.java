package com.example.command.result;

import com.example.resp.Resp;

public final class NoReplyResult implements CommandResult {

    public static final NoReplyResult INSTANCE = new NoReplyResult();

    private NoReplyResult() {}

    @Override
    public Resp toResp() {
        return null; // 表示不写回客户端
    }
}
