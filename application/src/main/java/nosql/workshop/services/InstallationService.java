package nosql.workshop.services;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import nosql.workshop.model.Installation;
import nosql.workshop.model.stats.CountByActivity;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.Oid;

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
        // TODO codez le service
        throw new UnsupportedOperationException();
    }

    /**
     * Compte le nombre d'installations par activité.
     *
     * @return le nombre d'installations par activité.
     */
    public List<CountByActivity> countByActivity() {
        return toList(installations.aggregate("{$unwind : \"$equipements\"}")
                .and("{$unwind : \"$equipements.activites\"}")
                .and("{$group : {_id : \"$equipements.activites\", number : {$sum : 1}}}]")
                .as(CountByActivity.class));
    }

    public double averageEquipmentsPerInstallation() {
        // TODO codez le service
        throw new UnsupportedOperationException();
    }

    /**
     * Recherche des installations sportives.
     *
     * @param searchQuery la requête de recherche.
     * @return les résultats correspondant à la requête.
     */
    public List<Installation> search(String searchQuery) {
        // TODO codez le service
        throw new UnsupportedOperationException();
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
        // TODO codez le service
        throw new UnsupportedOperationException();
    }
}
