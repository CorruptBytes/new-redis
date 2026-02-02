package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.SimpleStringResult;
import com.example.command.result.CommandResult;

public class SLAVEOF extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.SLAVEOF;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        if (argc() != 3) {
            return new com.example.command.result.ErrorResult("ERR wrong number of arguments for 'slaveof' command");
        }
        
        String masterHost = stringValue(1).getValue().toUtf8String();
        int masterPort = (int) longValue(2);
        
        // 处理 SLAVEOF <host> <port>
        context.getReplicaManager().handleSlaveof(masterHost, masterPort);
        
        return SimpleStringResult.OK;
    }
}
