package com.example.command;

import com.example.ServerContext;
import com.example.database.Database;
import com.example.resp.Resp;
import com.example.command.result.CommandResult;

public interface Command<K,V> {


    /**
     * 获取接口类型
     *
     * @return 接口类型
     */
    CommandType type();

    /**
     * 注入属性
     *
     * @param array 操作数组
     */
    void setContent(Resp[] array);

    /**
     * 处理消息命令
     *
     * @param context 服务器上下文
     */
    CommandResult execute(ServerContext context);
}