package nosql.workshop.batch.elasticsearch;

import com.mongodb.*;
import nosql.workshop.batch.elasticsearch.util.ElasticSearchBatchUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.HppcMaps;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static nosql.workshop.batch.elasticsearch.util.ElasticSearchBatchUtils.*;

/**
 * Transferts les documents depuis MongoDB vers Elasticsearch.
 */
public class MongoDbToElasticsearch {

    private static String getIp() {
        String ret = "localhost";
        try {
            ret = InetAddress.getLocalHost().getHostAddress().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void main(String[] args) throws UnknownHostException {

        MongoClient mongoClient = null;

        long startTime = System.currentTimeMillis();
        try (Client elasticSearchClient = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", "le_petit_pedestre").build())
                .addTransportAddress(new InetSocketTransportAddress(getIp(), ES_DEFAULT_PORT));){
            checkIndexExists("installations", elasticSearchClient);

            mongoClient = new MongoClient();

            // cursor all database objects from mongo db
            DBCursor cursor = ElasticSearchBatchUtils.getMongoCursorToAllInstallations(mongoClient);
            BulkRequestBuilder bulkRequest = elasticSearchClient.prepareBulk();

            while (cursor.hasNext()) {
                DBObject object = cursor.next();

                String objectId = (String) object.get("_id");
                object.removeField("dateMiseAJourFiche");

                bulkRequest.add(elasticSearchClient.prepareIndex("installations", "installation", objectId).setSource(object.toMap()));
            }
            BulkResponse bulkItemResponses = bulkRequest.execute().actionGet();

            dealWithFailures(bulkItemResponses);

            System.out.println("Inserted all documents in " + (System.currentTimeMillis() - startTime) + " ms");
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }


    }

}
