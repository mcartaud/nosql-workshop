package nosql.workshop.batch.elasticsearch;

import nosql.workshop.batch.elasticsearch.util.ElasticSearchBatchUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static nosql.workshop.batch.elasticsearch.util.ElasticSearchBatchUtils.checkIndexExists;
import static nosql.workshop.batch.elasticsearch.util.ElasticSearchBatchUtils.dealWithFailures;

/**
 * Job d'import des rues de towns_paysdeloire.csv vers ElasticSearch (/towns/town)
 */
public class ImportTowns {

    private static String getIp() {
        String ret = "localhost";
        try {
            ret = InetAddress.getLocalHost().getHostAddress().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ImportTowns.class.getResourceAsStream("/csv/towns_paysdeloire.csv")));

             Client elasticSearchClient = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", "le_petit_pedestre").build())
                     .addTransportAddress(new InetSocketTransportAddress(getIp(), 9300));) {

            checkIndexExists("towns", elasticSearchClient);

            BulkRequestBuilder bulkRequest = elasticSearchClient.prepareBulk();

            reader.lines()
                    .skip(1)
                    .filter(line -> line.length() > 0)
                    .forEach(line -> insertTown(line, bulkRequest, elasticSearchClient));

            BulkResponse bulkItemResponses = bulkRequest.execute().actionGet();

            dealWithFailures(bulkItemResponses);
        }

    }

    private static void insertTown(String line, BulkRequestBuilder bulkRequest, Client elasticSearchClient) {
        line = ElasticSearchBatchUtils.handleComma(line);

        String[] split = line.split(",");

        String townName = split[1].replaceAll("\"", "");
        Double longitude = Double.valueOf(split[6]);
        Double latitude = Double.valueOf(split[7]);

        try {
            bulkRequest.add(elasticSearchClient.prepareIndex("towns", "town")
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("townName", townName)
                            .field("location", new Double[]{longitude, latitude})
                            .endObject()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
