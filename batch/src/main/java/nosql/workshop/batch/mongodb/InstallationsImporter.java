package nosql.workshop.batch.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.io.*;
import java.time.LocalDate;
import java.util.Calendar;

/**
 * Importe les 'installations' dans MongoDB.
 */
public class InstallationsImporter {

    private final DBCollection installationsCollection;

    public InstallationsImporter(DBCollection installationsCollection) {
        this.installationsCollection = installationsCollection;
    }

    public void run() {
        InputStream is = CsvToMongoDb.class.getResourceAsStream("/csv/installations.csv");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            reader.lines()
                    .skip(1)
                    .filter(line -> line.length() > 0)
                    .forEach(line -> installationsCollection.save(toDbObject(line)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private DBObject toDbObject(final String line) {
        String[] columns = line
                .substring(1, line.length() - 1)
                .split("\",\"");

        final Calendar calendar = Calendar.getInstance();
        if (columns.length == 29) {
            String val = columns[28].split(" ")[0].trim();
            LocalDate localDate = LocalDate.parse(val);
            calendar.set(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        }

        BasicDBObject basicDBObject = new BasicDBObject()
                .append("_id", columns[1].trim())
                .append("nom", columns[0].trim())
                .append("adresse", new BasicDBObject()
                        .append("numero", columns[6].trim())
                        .append("voie", columns[7].trim())
                        .append("lieuDit", columns[5].trim())
                        .append("codePostal", columns[4].trim())
                        .append("commune", columns[2].trim()))
                .append("location", new BasicDBObject()
                        .append("type", "Point")
                        .append("coordinates", new Double[]{Double.parseDouble(columns[9]), Double.parseDouble(columns[10])}))
                .append("multiCommune", (columns[16].trim().equals("oui") ? 0 : 1))
                .append("nbPlacesParking", columns[17].trim())
                .append("nbPlacesParkingHandicapes", columns[18].trim())
                .append("dateMiseAJourFiche", calendar.getTime());

        return basicDBObject;
    }
}
