package nosql.workshop.services;

import com.google.inject.Inject;
import nosql.workshop.model.Installation;
import nosql.workshop.model.stats.Average;
import nosql.workshop.model.stats.CountByActivity;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service permettant de manipuler les installations sportives.
 */
public class InstallationService {

    /**
     * Nom de la collection MongoDB.
     */
    public static final String COLLECTION_NAME = "installations";

    private final MongoCollection installations;

    @Inject
    public InstallationService(MongoDB mongoDB) throws UnknownHostException {
        this.installations = mongoDB.getJongo().getCollection(COLLECTION_NAME);
    }

    /**
     * Retourne une installation étant donné son numéro.
     *
     * @param numero le numéro de l'installation.
     * @return l'installation correspondante, ou <code>null</code> si non trouvée.
     */
    public Installation get(String numero) {
        return installations.findOne("{_id:#}", numero).as(Installation.class);
    }

    /**
     * Retourne la liste des installations.
     *
     * @param page     la page à retourner.
     * @param pageSize le nombre d'installations par page.
     * @return la liste des installations.
     */
    public List<Installation> list(int page, int pageSize) {
        List<Installation> installationList = new ArrayList<>();

        MongoCursor<Installation> all = installations.find().skip(page * pageSize).as(Installation.class);
        for (int i = 0; i < pageSize; i++) {
            installationList.add(all.next());
        }
        return installationList;
    }

    /**
     * Retourne une installation aléatoirement.
     *
     * @return une installation.
     */
    public Installation random() {
        long count = count();
        int random = new Random().nextInt((int) count);
        MongoCursor<Installation> installation = installations.find().skip(random).as(Installation.class);
        return  installation.next();
    }

    /**
     * Retourne le nombre total d'installations.
     *
     * @return le nombre total d'installations
     */
    public long count() {
        return installations.count();
    }

    /**
     * Retourne l'installation avec le plus d'équipements.
     *
     * @return l'installation avec le plus d'équipements.
     */
    public Installation installationWithMaxEquipments() {
        return installations.aggregate("{$project: {nbEquipements:{$size: '$equipements'}, nom: 1, equipements: 1}}")
                .and("{$sort: {'nbEquipements' : -1}}")
                .and("{$limit: 1}")
                .as(Installation.class).get(0);
    }

    /**
     * Compte le nombre d'installations par activité.
     *
     * @return le nombre d'installations par activité.
     */
    public List<CountByActivity> countByActivity() {
        return installations.aggregate("{$unwind : '$equipements'}")
                .and("{$unwind : '$equipements.activites'}")
                .and("{$group : {_id : '$equipements.activites', total : {$sum : 1}}}")
                .and("{$project: {activite : '$_id', total : 1}}")
                .as(CountByActivity.class);
    }

    public double averageEquipmentsPerInstallation() {
        return installations.aggregate("{$unwind: '$equipements'}")
                .and("{$group: {_id: '$_id', number: {$sum: 1 }} }")
                .and("{$group: {_id: null, average: { $avg : '$number'} } }")
                .as(Average.class).get(0).getAverage();
    }

    /**
     * Recherche des installations sportives.
     *
     * @param searchQuery la requête de recherche.
     * @return les résultats correspondant à la requête.
     */
    public List<Installation> search(String searchQuery) {
        MongoCursor<Installation> res = installations.find("{ $text: { $search: # } }", searchQuery).as(Installation.class);

        List<Installation> result = new ArrayList<>();
        while( res.hasNext() ) {
            result.add( res.next() );
        }
        return result;
    }

    /**
     * Recherche des installations sportives par proximité géographique.
     *
     * @param lat      latitude du point de départ.
     * @param lng      longitude du point de départ.
     * @param distance rayon de recherche.
     * @return les installations dans la zone géographique demandée.
     */
    public List<Installation> geosearch(double lat, double lng, double distance) {
        List<Installation> installationList = new ArrayList<>();
        MongoCursor<Installation> installationMongoCursor = installations.find("{location :{ $near :{ $geometry :{ type : 'Point',coordinates: [" + lng + ", " + lat + "]},$maxDistance: " + distance + "}}}").as(Installation.class);
        for (Installation installation : installationMongoCursor) {
            installationList.add(installation);
        }
        return installationList;
    }
}
