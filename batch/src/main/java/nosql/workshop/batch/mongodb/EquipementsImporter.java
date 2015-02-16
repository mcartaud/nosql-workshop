package nosql.workshop.batch.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import java.io.*;

public class EquipementsImporter {

    private final DBCollection installationsCollection;

    public EquipementsImporter(DBCollection installationsCollection) {
        this.installationsCollection = installationsCollection;
    }

    public void run() {
        InputStream is = CsvToMongoDb.class.getResourceAsStream("/csv/equipements.csv");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            reader.lines()
                    .skip(1)
                    .filter(line -> line.length() > 0)
                    .forEach(line -> updateInstallation(line));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void updateInstallation(final String line) {
        String[] columns = line.split(",");

        BasicDBObject newDocument = new BasicDBObject()
            .append("$set", new BasicDBObject()
                .append("equipements", new BasicDBObject()
                    .append("equipmentId", columns[4].trim())
                    .append("nom", columns[5].trim())
                    .append("type", columns[7].trim())
                    .append("famille", columns[9].trim())));

        String installationId = columns[2];

        BasicDBObject searchQuery = new BasicDBObject().append("_id", installationId);
        installationsCollection.update(searchQuery, newDocument);
    }
}
