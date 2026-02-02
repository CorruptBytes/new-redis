package com.example.command.general;

import com.example.ServerContext;
import com.example.command.AbstractCommand;
import com.example.command.CommandType;
import com.example.command.result.CommandResult;
import com.example.command.result.BulkStringResult;

public class INFO extends AbstractCommand {

    @Override
    public CommandType type() {
        return CommandType.INFO;
    }

    @Override
    public CommandResult execute(ServerContext context) {
        StringBuilder info = new StringBuilder();
        
        // 服务器基本信息
        info.append("# Server\n");
        info.append("redis_version:0.1.0\n");
        info.append("redis_git_sha1:00000000\n");
        info.append("redis_git_dirty:0\n");
        info.append("redis_build_id:0000000000000000\n");
        info.append("redis_mode:standalone\n");
        info.append("os:Windows\n");
        info.append("arch:amd64\n");
        info.append("multiplexing_api:Netty NIO\n");
        info.append("process_id:").append(ProcessHandle.current().pid()).append("\n");
        info.append("tcp_port:6379\n");
        info.append("uptime_in_seconds:0\n");
        info.append("uptime_in_days:0\n");
        info.append("hz:10\n");
        info.append("configured_hz:10\n");
        info.append("lru_clock:0\n");
        info.append("executable:\n");
        info.append("config_file:\n");
        
        // 复制信息
        info.append("# Replication\n");
        if (context.getRole() == ServerContext.ServerRole.MASTER) {
            info.append("role:master\n");
            info.append("connected_slaves:").append(context.getReplicaManager().getSlaveCount()).append("\n");
            info.append("master_replid:0000000000000000000000000000000000000000\n");
            info.append("master_replid2:0000000000000000000000000000000000000000\n");
            info.append("master_repl_offset:0\n");
            info.append("second_repl_offset:-1\n");
            info.append("repl_backlog_active:0\n");
            info.append("repl_backlog_size:1048576\n");
            info.append("repl_backlog_first_byte_offset:0\n");
            info.append("repl_backlog_histlen:0\n");
        } else {
            info.append("role:slave\n");
            info.append("master_host:").append(context.getMasterHost()).append("\n");
            info.append("master_port:").append(context.getMasterPort()).append("\n");
            info.append("master_link_status:down\n");
            info.append("master_last_io_seconds_ago:-1\n");
            info.append("master_sync_in_progress:0\n");
            info.append("slave_repl_offset:0\n");
            info.append("slave_priority:100\n");
            info.append("slave_read_only:1\n");
            info.append("connected_slaves:0\n");
            info.append("master_replid:0000000000000000000000000000000000000000\n");
            info.append("master_replid2:0000000000000000000000000000000000000000\n");
            info.append("master_repl_offset:0\n");
        }
        
        // 内存信息
        info.append("# Memory\n");
        info.append("used_memory:0\n");
        info.append("used_memory_human:0.00B\n");
        info.append("used_memory_rss:0\n");
        info.append("used_memory_rss_human:0.00B\n");
        info.append("used_memory_peak:0\n");
        info.append("used_memory_peak_human:0.00B\n");
        info.append("used_memory_peak_perc:0.00%\n");
        info.append("used_memory_overhead:0\n");
        info.append("used_memory_startup:0\n");
        info.append("used_memory_dataset:0\n");
        info.append("used_memory_dataset_perc:0.00%\n");
        info.append("allocator_allocated:0\n");
        info.append("allocator_active:0\n");
        info.append("allocator_resident:0\n");
        info.append("total_system_memory:0\n");
        info.append("total_system_memory_human:0.00B\n");
        info.append("used_memory_lua:0\n");
        info.append("used_memory_lua_human:0.00B\n");
        info.append("used_memory_scripts:0\n");
        info.append("used_memory_scripts_human:0.00B\n");
        info.append("number_of_cached_scripts:0\n");
        info.append("maxmemory:0\n");
        info.append("maxmemory_human:0.00B\n");
        info.append("maxmemory_policy:noeviction\n");
        info.append("allocator_frag_ratio:0.00\n");
        info.append("allocator_frag_bytes:0\n");
        info.append("allocator_rss_ratio:0.00\n");
        info.append("allocator_rss_bytes:0\n");
        info.append("rss_overhead_ratio:0.00\n");
        info.append("rss_overhead_bytes:0\n");
        info.append("mem_fragmentation_ratio:0.00\n");
        info.append("mem_fragmentation_bytes:0\n");
        info.append("mem_not_counted_for_evict:0\n");
        info.append("mem_replication_backlog:0\n");
        info.append("mem_clients_normal:0\n");
        info.append("mem_clients_slaves:0\n");
        info.append("mem_clients_output_buffer:0\n");
        info.append("mem_total_replication_buffers:0\n");
        info.append("mem_expire_children:0\n");
        info.append("mem_active_defrag_running:0\n");
        info.append("mem_allocator:jemalloc-5.3.0\n");
        info.append("active_defrag_enabled:0\n");
        info.append("active_defrag_running:0\n");
        info.append("active_defrag_max_scan_fields:0\n");
        
        // 统计信息
        info.append("# Stats\n");
        info.append("total_connections_received:0\n");
        info.append("total_commands_processed:0\n");
        info.append("instantaneous_ops_per_sec:0\n");
        info.append("total_net_input_bytes:0\n");
        info.append("total_net_output_bytes:0\n");
        info.append("instantaneous_input_kbps:0.00\n");
        info.append("instantaneous_output_kbps:0.00\n");
        info.append("rejected_connections:0\n");
        info.append("sync_full:0\n");
        info.append("sync_partial_ok:0\n");
        info.append("sync_partial_err:0\n");
        info.append("expired_keys:0\n");
        info.append("expired_stale_perc:0.00\n");
        info.append("expired_time_cap_reached_count:0\n");
        info.append("evicted_keys:0\n");
        info.append("keyspace_hits:0\n");
        info.append("keyspace_misses:0\n");
        info.append("pubsub_channels:0\n");
        info.append("pubsub_patterns:0\n");
        info.append("latest_fork_usec:0\n");
        info.append("migrate_cached_sockets:0\n");
        info.append("slave_expires_tracked_keys:0\n");
        info.append("active_defrag_hits:0\n");
        info.append("active_defrag_misses:0\n");
        info.append("active_defrag_key_hits:0\n");
        info.append("active_defrag_key_misses:0\n");
        info.append("tracking_total_keys:0\n");
        info.append("tracking_total_items:0\n");
        info.append("tracking_total_prefixes:0\n");
        info.append("unexpected_error_replies:0\n");
        
        return new BulkStringResult(info.toString().getBytes());
    }
}
