package com.couchbase.demo.mobileclip.database;

import com.couchbase.demo.mobileclip.config.CouchbaseLiteProperties;
import com.couchbase.demo.mobileclip.listeners.CounterCollectionChangeListener;
import com.couchbase.demo.mobileclip.models.EmbeddingDbModel;
import com.couchbase.demo.mobileclip.models.PredictionDbModel;
import com.couchbase.demo.mobileclip.utils.DBUtils;
import com.couchbase.demo.mobileclip.utils.FileUtils;
import com.couchbase.demo.mobileclip.utils.ZipUtils;
import com.couchbase.lite.*;

import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@Slf4j
@Data
public class DatabaseService {

    final DBUtils dbUtils = new DBUtils();
    final CouchbaseLiteProperties.LocalDBProperties properties;
    final CouchbaseLiteProperties.LogProperties logProperties;

    final Database database;
    final List<ListenerToken> listenerTokens = new ArrayList<>();

    private final static String EMBEDDINGS_SCOPE = "scopePOS";
    private final static String EMBEDDINGS_COLLECTION = "photoClassification";

    public DatabaseService(CouchbaseLiteProperties.LocalDBProperties properties, CouchbaseLiteProperties.LogProperties logProperties, Database database, List<ListenerToken> tokens) {
        this.properties = properties;
        this.logProperties = logProperties;
        this.database = database;
        this.listenerTokens.addAll(tokens);
    }

    public Long countNumberOfLabels() throws CouchbaseLiteException {

        String queryStmt = format("SELECT count(*) total FROM scopePOS.photoClassification");
        return database.createQuery(queryStmt).execute().allResults().get(0).getLong("total");
    }

    public List<String> findUniqueClasses() throws CouchbaseLiteException {

        List<String> resultsList = new ArrayList<>();

        String queryStmt = format("SELECT className FROM scopePOS.photoClassification GROUP BY className");

        ResultSet results = database.createQuery(queryStmt).execute();

        for (Result result : results) {
            String tag = result.getString("className");
            resultsList.add(tag);
        }

        return resultsList;
    }

    public void saveEmbedding(String id, float[] embeddings, String className) throws CouchbaseLiteException {

        Collection embeddingsCol = database.getCollection(EMBEDDINGS_COLLECTION, EMBEDDINGS_SCOPE);
        EmbeddingDbModel model = new EmbeddingDbModel(id, embeddings, className);
        embeddingsCol.save(model.toMutable());
    }

    public  List<PredictionDbModel> findNearbyEmbeddings(float[] embeddings) throws CouchbaseLiteException {
        String queryStmt = format("SELECT " +
                "className, APPROX_VECTOR_DISTANCE(embeddings, $vectorParam) as distance " +
                " FROM scopePOS.photoClassification " +
                " WHERE APPROX_VECTOR_DISTANCE(embeddings, $vectorParam) < 0.4 " +
                " LIMIT 3");

        ArrayList<Float> floatList = new ArrayList<>();
        for (float value : embeddings) {
            floatList.add(value); // Autoboxing converts float to Float
        }

        Parameters params = new Parameters();
        params.setArray("vectorParam", new MutableArray(String.valueOf(floatList)));

        Query qq = database.createQuery(queryStmt);
        qq.setParameters(params);

        ResultSet results = qq.execute();

        List<PredictionDbModel> resultsList = new ArrayList<>();

        for (Result result : results) {

            String tag = result.getString("className");
            Float distance = result.getFloat("distance");

            resultsList.add(new PredictionDbModel(tag, distance));
        }

        return resultsList;
    }



    private void removeListeners() {
        this.listenerTokens.forEach(ListenerToken::remove);
    }

    @PreDestroy
    public void close() {
        log.info("closing database...");
        try {
            compactDb();
            removeListeners();
            database.close();
        } catch (CouchbaseLiteException e) {
            log.error("Exception closing database", e);
            throw new RuntimeException(e);
        }
    }

    void purgeAll(String collectionName, List<String> ids) {
        //TODO Reactive Async
        ids.forEach(it -> {
            try {
                Objects.requireNonNull(database.getCollection(collectionName)).purge(it);
            } catch (CouchbaseLiteException e) {
                log.error("Exception purging {} doc. ",it,e);
            }
        });
    }


    private void compactDb() throws CouchbaseLiteException {
        log.info("Compacting DB in progress... {}",database.performMaintenance(MaintenanceType.COMPACT)? "DONE": "SKIPPED");
    }


    @Data
    public static class DBManagerBuilder {
        CouchbaseLiteProperties.LocalDBProperties dbProperties;
        CouchbaseLiteProperties.LogProperties logProperties;

        List<CollectionChangeListener> listeners = List.of(new CounterCollectionChangeListener());
        List<ListenerToken> changeListenerTokens = new ArrayList<>();
        DatabaseConfiguration cfg;
        Database database;

        public DBManagerBuilder(CouchbaseLiteProperties.LocalDBProperties dbProperties, CouchbaseLiteProperties.LogProperties logProperties) {
            this.dbProperties = dbProperties;
            this.logProperties = logProperties;
        }


        protected void setup(){

            this.cfg =  databaseConfiguration();

            try {

                prepareDatabase();
                this.database = new Database(dbProperties.getDatabase(), cfg);
                setupCollections();

            } catch (CouchbaseLiteException e ) {
                log.error("Exception instantiating database", e);
            }
        }

        private DatabaseConfiguration databaseConfiguration() {

            DatabaseConfiguration cfg = new DatabaseConfiguration();
            cfg.setDirectory(dbProperties.getDbPath()); //System.getProperty("user.dir"));

            if(dbProperties.isEncryptedDb())
                cfg.setEncryptionKey(new EncryptionKey(dbProperties.getEncryptionKey()));

            return cfg;
        }

        private void setupDatabaseLogs() {
            File logPath = new File(logProperties.getPath());
            if (!logPath.exists()) {
                if(!logPath.mkdirs()) {
                    log.error("Log path: {} folder cannot be created", logPath);
                }
            }
            LogFileConfiguration logCfg = new LogFileConfiguration(logPath.getAbsolutePath());
            logCfg.setMaxSize(logProperties.getMaxSize());
            logCfg.setMaxRotateCount(logProperties.getRotationCount());
            logCfg.setUsePlaintext(logProperties.isPlainText());
            Database.log.getFile().setConfig(logCfg);
            Database.log.getFile().setLevel(logProperties.getLevel());
        }

        private void copyDatabase() throws CouchbaseLiteException {

            File srcDir = new File(dbProperties.getDownloadPath());
            File srcDb = dbProperties.getUnzippedDbFolderFile();
            File srcDbZip = dbProperties.getZippedDbFile();
            File dstDb = dbProperties.getDbFolderFile();

            if (dstDb.exists()) {
                log.warn(" -------------> Target database {} already exist!!!", dstDb.getAbsolutePath());
            } else {
                // Unzip database
                if(srcDbZip.exists()) {

                    log.info("download path: {}", srcDbZip.getAbsolutePath());
                    log.info("unzipped path: {}", srcDb.getAbsolutePath());
                    ZipUtils.unzip(srcDbZip.getAbsolutePath(), srcDir);

                } else {

                    log.warn("Skipping unzipping download database, source zip file not found at {}",srcDbZip.getAbsolutePath());
                }

                if(srcDb.exists()) {

                    log.info("Coping database: {}",srcDb);
                    log.info("... to database: {}",cfg.getDirectory());
                    log.info(" encrypted with: {}", cfg.getEncryptionKey());
                    Database.copy(srcDb, dbProperties.getDatabase(), cfg);

                } else {

                    log.warn("Skipping copy database, source database not found at {}",srcDb.getAbsolutePath());
                }
            }
            log.info("Target Database size: {} bytes",dbProperties.getSQLLiteDBFile().length());

        }

        private void flushPreviousDB() {
            log.info("deleting '{}' folder... {}",dbProperties.getDbFolderFile().getAbsolutePath(), FileUtils.deleteDirectory(dbProperties.getDbFolderFile().getAbsoluteFile()) ? "OK": "FAILED");
            log.info("deleting '{}' folder... {}",dbProperties.getUnzippedDbFolderFile().getAbsolutePath(), FileUtils.deleteDirectory(dbProperties.getUnzippedDbFolderFile()) ? "OK": "FAILED");
        }

        private void prepareDatabase() throws CouchbaseLiteException {
            if (dbProperties.isFlushPreviousDb())
                flushPreviousDB();
            // Copy database & Download from ftp
            if (dbProperties.isCopyDb()) {
                // SFTPUtils.download(configFiles);
                // copy database for using new UUID
                copyDatabase();
            }
            setupDatabaseLogs();

            CouchbaseLite.enableVectorSearch();
        }

        private void setupCollections() {

            changeListenerTokens.clear();
            log.info("Setting local '{}' database's collections", dbProperties.getDatabase());

            CouchbaseLiteProperties.ScopeProperties scope = dbProperties.getScope();

            //TODO: this index init should be externalized
            //centroids: 1-64000. The general guideline is an approximate square root of the number of documents
            VectorIndexConfiguration vectorIndexConfiguration =
                    new VectorIndexConfiguration("embeddings", 512, 40);

            vectorIndexConfiguration
                    .setEncoding(VectorEncoding.none())
                    .setMetric(VectorIndexConfiguration.DistanceMetric.COSINE)
                    .setLazy(false);

            scope.getCollections().forEach(collection -> {

                try {

                    log.info(" - creating {}.{} collection", scope.getName(), collection);
                    Collection col = database.createCollection(collection, scope.getName());
                    //TODO setup individual collections listeners
                    setupListeners(col);

                    col.createIndex("embeddingsIndex", vectorIndexConfiguration);

                } catch (CouchbaseLiteException e) {
                    log.error("{} creating collection {}.{}", e.getClass().getSimpleName(), scope.getName(), collection);
                    throw new RuntimeException(e);
                }
            });
        }


        public void setupListeners(Collection collection) throws CouchbaseLiteException {
            //TODO reset listeners
            for (CollectionChangeListener listener : listeners) {
                changeListenerTokens.add(collection.addChangeListener(listener));
            }
        }


        public DatabaseService build() {
            setup();
            return new DatabaseService(this.dbProperties, this.logProperties, this.database, this.changeListenerTokens); //TODO remove unnecessary log properties
        }
    }
}