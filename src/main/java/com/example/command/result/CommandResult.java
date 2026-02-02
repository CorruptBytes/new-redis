package com.example.command.result;

import com.example.resp.Resp;

public sealed interface CommandResult permits
        SimpleStringResult,
        IntegerResult,
        BulkStringResult,
        ArrayResult,
        ErrorResult,
        NoReplyResult,
        BlockedResult {
    Resp toResp();
}
