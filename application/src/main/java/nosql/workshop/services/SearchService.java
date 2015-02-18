package nosql.workshop.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import nosql.workshop.model.Installation;
import nosql.workshop.model.suggest.TownSuggest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Chris on 12/02/15.
 */
public class SearchService {
    public static final String INSTALLATIONS_INDEX = "installations";
    public static final String INSTALLATION_TYPE = "installation";
    public static final String TOWNS_INDEX = "towns";
    private static final String TOWN_TYPE = "town";


    public static final String ES_HOST = "es.host";
    public static final String ES_TRANSPORT_PORT = "es.transport.port";

    final Client elasticSearchClient;
    final ObjectMapper objectMapper;

    @Inject
    public SearchService(@Named(ES_HOST) String host, @Named(ES_TRANSPORT_PORT) int transportPort) {
        elasticSearchClient = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", "le_petit_pedestre").build())
                .addTransportAddress(new InetSocketTransportAddress(host, transportPort));

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Recherche les installations à l'aide d'une requête full-text
     * @param searchQuery la requête
     * @return la listes de installations
     */
    public List<Installation> search(String searchQuery) {
        SearchResponse searchResponse = elasticSearchClient.prepareSearch(INSTALLATIONS_INDEX)
                .setTypes(INSTALLATION_TYPE)
                .setQuery(QueryBuilders.queryString(searchQuery))
                .setFrom(0).setSize(60)
                .execute()
                .actionGet();

        List<Installation> installations = new ArrayList<>();
        SearchHits searchHits = searchResponse.getHits();
        for (SearchHit searchHit : searchHits) {
            installations.add(mapToInstallation(searchHit));
        }
        return installations;
    }

    /**
     * Transforme un résultat de recherche ES en objet installation.
     *
     * @param searchHit l'objet ES.
     * @return l'installation.
     */
    private Installation mapToInstallation(SearchHit searchHit) {
        try {
            return objectMapper.readValue(searchHit.getSourceAsString(), Installation.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TownSuggest> suggestTownName(String townName){
        // TODO codez le service
        throw new UnsupportedOperationException();
    }

    public Double[] getTownLocation(String townName) {
        // TODO codez le service
        throw new UnsupportedOperationException();
    }
}
