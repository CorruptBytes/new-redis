package com.example;

import com.example.aof.AofManager;
import com.example.database.Database;
import com.example.evnetloop.MainEventLoop;
import com.example.rdb.DefaultRdbModule;
import com.example.repl.ReplicaManager;
import com.example.type.RedisObject;
import com.example.type.RedisString;
import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class ServerContext {

    private final Database<RedisString, RedisObject> database;
    private final AofManager aof;
    private MainEventLoop eventLoop;
    private DefaultRdbModule rdbModule;
    private ServerRole role = ServerRole.MASTER;
    private String masterHost;
    private int masterPort;
    private final ReplicaManager replicaManager;
    
    public ServerContext(Database<RedisString, RedisObject> database, AofManager aofManager, MainEventLoop eventLoop) {
        this.database = database;
        this.aof = aofManager;
        this.eventLoop = eventLoop;
        this.replicaManager = new ReplicaManager(this);
    }

    public Database<RedisString, RedisObject> db() {
        return database;
    }

    public AofManager aof() {
        return aof;
    }
    
    public EventLoopGroup main() {
        return eventLoop.getGroup();
    }
    
    public MainEventLoop mainEventLoop() {
        return eventLoop;
    }
    
    public ReplicaManager getReplicaManager() {
        return replicaManager;
    }
    
    public enum ServerRole {
        MASTER,
        SLAVE
    }
}
