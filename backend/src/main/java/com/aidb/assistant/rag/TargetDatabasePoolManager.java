package com.aidb.assistant.rag;

import com.aidb.assistant.entity.ConnectionConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TargetDatabasePoolManager {

    private static final Logger log = LoggerFactory.getLogger(TargetDatabasePoolManager.class);

    // Cache DataSources by ConnectionConfig ID to reuse connection pools per database target
    private final ConcurrentHashMap<Long, HikariDataSource> dataSources = new ConcurrentHashMap<>();

    public HikariDataSource getDataSource(ConnectionConfig config) {
        if (config == null || config.getId() == null) {
            throw new IllegalArgumentException("ConnectionConfig and its ID must not be null");
        }

        return dataSources.computeIfAbsent(config.getId(), id -> {
            log.info("Initializing new HikariCP pool for target database: {}", config.getDatabaseName());
            HikariConfig hc = new HikariConfig();
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                    config.getHost(), config.getPort(), config.getDatabaseName());
            
            hc.setJdbcUrl(url);
            hc.setUsername(config.getUsername());
            hc.setPassword(config.getPassword());
            hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // Pool optimization settings
            hc.setMaximumPoolSize(10);
            hc.setMinimumIdle(2);
            hc.setIdleTimeout(300000); // 5 minutes
            hc.setConnectionTimeout(20000); // 20 seconds
            hc.setMaxLifetime(1800000); // 30 minutes
            hc.setPoolName("TargetDB-Pool-" + id);
            
            return new HikariDataSource(hc);
        });
    }

    public void invalidatePool(Long configId) {
        HikariDataSource ds = dataSources.remove(configId);
        if (ds != null) {
            log.info("Closing HikariCP pool for connection config ID: {}", configId);
            ds.close();
        }
    }

    @PreDestroy
    public void shutdownAll() {
        log.info("Shutting down all target database connection pools...");
        dataSources.values().forEach(HikariDataSource::close);
        dataSources.clear();
    }
}
