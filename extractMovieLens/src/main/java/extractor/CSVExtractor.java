package extractor;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.SystemUtils;
import org.bson.Document;


import java.io.IOException;
import java.io.Reader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

/**
 * @author hpminh@apcs.vn
 *
 * Class to read from csv files and insert into mongoDB
 */
public class CSVExtractor {
    public static final char Sp = SystemUtils.IS_OS_WINDOWS ? '\\' : '/';

    String host;
    String port;
    String filePath;

    public CSVExtractor(String filePath, String host, String port) {
        this.filePath = filePath;
        this.host = host;
        this.port = port;
    }

    public void extract(){
        insertMovies();
        insertUsersAndRatings();
    }


    public void insertUsersAndRatings() {
        int bufferSize = 1_000_000;
        try (
                Reader reader = Files.newBufferedReader(Paths.get(filePath + Sp + "ratings.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
                MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port));
        )
        {

            MongoDatabase database = mongoClient.getDatabase("movieLens");
            MongoCollection<Document> ratings = database.getCollection("ratings");
            MongoCollection<Document> users = database.getCollection("users");
            //csvParser.getRecords().size();

            //List<CSVRecord> records = csvParser.getRecords();
            //System.out.println("File size: " + records.size());
            Iterator<CSVRecord> it = csvParser.iterator();
            ArrayList<Document> ratingList = new ArrayList<>(bufferSize);

            CSVRecord csvRecord;
            //skip header
            it.next();
            //handle first case
            csvRecord = it.next();
            String id = csvRecord.get(0);
            String movie = csvRecord.get(1);
            float rating = Float.parseFloat(csvRecord.get(2));
            Date date = Date.from(Instant.ofEpochSecond(Long.parseLong(csvRecord.get(3))));


            Document ratingRow = new Document("userID", id)
                                .append("itemID", movie)
                                .append("rating", rating)
                                .append("datetime", date);
            ratingList.add(ratingRow);

            users.insertOne(new Document("_id", id));
            String prevID = id;

            while(it.hasNext()){
                csvRecord = it.next();
                id = csvRecord.get(0);
                movie = csvRecord.get(1);
                rating = Float.parseFloat(csvRecord.get(2));
                date = Date.from(Instant.ofEpochSecond(Long.parseLong(csvRecord.get(3))));

                ratingRow = new Document("userID", id)
                        .append("itemID", movie)
                        .append("rating", rating)
                        .append("datetime", date);
                ratingList.add(ratingRow);

                if(ratingList.size() == bufferSize){
                    System.out.println("Commit" + ratingList.size());
                    ratings.insertMany(ratingList);
                    ratingList.clear();
                    System.gc();
                }

                if(!prevID.equals(id)){
                    users.insertOne(new Document("_id", id));
                    prevID = id;
                }
            }
            ratings.insertMany(ratingList);
            IndexOptions options = new IndexOptions();
            options.name("pairIDtime");
            Document indexConfig = new Document("userID", 1).append("itemID", 1).append("date", -1);
            ratings.createIndex(indexConfig, options);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertMovies() {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(filePath  + Sp + "movies.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withQuote('"'));
                MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port))
        )
        {
            MongoDatabase database = mongoClient.getDatabase("movieLens");
            MongoCollection<Document> collection = database.getCollection("items");

            ArrayList<Document> documentList = new ArrayList<>();
            Iterator<CSVRecord> it = csvParser.iterator();
            CSVRecord csvRecord;
            it.next();
            while(it.hasNext()){
                csvRecord = it.next();
                String id = csvRecord.get(0);
                String title = csvRecord.get(1);
                String genres = csvRecord.get(2);
                /*
                Use this line if you want the genres fields to be separated.
                List<String> genres = Arrays.asList(csvRecord.get(2).split("\\|")); */

                Document doc = new Document("_id", id)
                        .append("title", title)
                        .append("genres", genres);
                documentList.add(doc);
            }

            collection.insertMany(documentList);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


}
