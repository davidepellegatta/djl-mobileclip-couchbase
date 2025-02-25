package com.couchbase.demo.mobileclip.config;

import com.couchbase.lite.*;
import com.couchbase.demo.mobileclip.database.DatabaseService;
import com.couchbase.demo.mobileclip.database.DatabaseService.DBManagerBuilder;
import com.couchbase.demo.mobileclip.replicator.ReplicationListenersManager;
import com.couchbase.demo.mobileclip.replicator.ReplicationListenersManager.ReplicationListenersManagerBuilder;
import com.couchbase.demo.mobileclip.replicator.ReplicationManager;
import com.couchbase.demo.mobileclip.replicator.ReplicationManager.ReplicationManagerBuilder;
import com.couchbase.demo.mobileclip.utils.ConfigPrinter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Data
@EnableConfigurationProperties(CouchbaseLiteProperties.class)
@ConfigurationPropertiesScan
@Configuration
public class CouchbaseLiteConfig {

    static {
        CouchbaseLite.init();
    }

    final ConfigPrinter printer = new ConfigPrinter();
    final CouchbaseLiteProperties properties;

    public CouchbaseLiteConfig(CouchbaseLiteProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ReplicationListenersManager listenerManager(Executor executor) {
        return new ReplicationListenersManagerBuilder(executor, properties.getRemote().getListeners()).build();
    }

    @Bean
    public Executor executor() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public DatabaseService dbManager() {
        return new DBManagerBuilder(this.properties.getLocal(), properties.getLog()).build();
    }

    @Bean
    public ReplicationManager replicatorManager(DatabaseService databaseService, ReplicationListenersManager listenerManager) throws CouchbaseLiteException {
        Set<Collection> collections = databaseService.getDatabase().getScope(properties.getLocal().getScope().getName()).getCollections();
        return new ReplicationManagerBuilder(this.properties.getRemote(), listenerManager, collections).build();
    }


}