package extractor;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.IndexOptions;
import org.apache.commons.lang3.SystemUtils;
import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;

/**
 * @author hpminh@apcs.vn
 *
 * Class to read from .dat files and insert into mongoDB
 */
public class DATExtractor {
    public static final char Sp = SystemUtils.IS_OS_WINDOWS ? '\\' : '/';
    String host;
    String port;
    String filePath;


    public DATExtractor(String filePath, String host, String port) {
        this.filePath = filePath;
        this.host = host;
        this.port = port;
    }

    public void extract(){
        File userFile = new File(filePath + Sp + "users.dat");
        if(userFile.exists() && userFile.isFile()){
            insertUsers();
            insertRatings();
        }
        else
            insertUsersAndRatings();
        insertMovies();

    }

    public void insertUsersAndRatings() {
        int bufferSize = 1_000_000;
        FileInputStream fis= null;
        try {
            fis = new FileInputStream(filePath  + Sp + "ratings.dat");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try (
                Scanner sc=new Scanner(fis);
                MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port));
        )
        {

            MongoDatabase database = mongoClient.getDatabase("movieLens");
            MongoCollection<Document> ratings = database.getCollection("ratings");
            MongoCollection<Document> users = database.getCollection("users");

            ArrayList<Document> ratingList = new ArrayList<>(bufferSize);

            String[] attributes;
            //handle first case
            attributes = sc.nextLine().split("::");
            String id = attributes[0];
            String movie = attributes[1];
            float rating = Float.parseFloat(attributes[2]);
            Date date = Date.from(Instant.ofEpochSecond(Long.parseLong(attributes[3])));


            Document ratingRow = new Document("userID", id)
                    .append("itemID", movie)
                    .append("rating", rating)
                    .append("datetime", date);
            ratingList.add(ratingRow);

            users.insertOne(new Document("_id", id));
            String prevID = id;

            while(sc.hasNextLine()){
                attributes = sc.nextLine().split("::");
                id = attributes[0];
                movie = attributes[1];
                rating = Float.parseFloat(attributes[2]);
                date = Date.from(Instant.ofEpochSecond(Long.parseLong(attributes[3])));

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
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read and insert into user collection
     */
    public void insertRatings() {
        FileInputStream fis= null;
        try {
            fis = new FileInputStream(filePath  + Sp + "ratings.dat");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try (
                Scanner sc=new Scanner(fis);
                MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port));
        )
        {

            MongoDatabase database = mongoClient.getDatabase("movieLens");
            MongoCollection<Document> collection = database.getCollection("ratings");
            ArrayList<Document> documentList = new ArrayList<>(1_000_000);
            Document doc;
            String[] attributes;
            String userId;
            String itemId;
            Float rating;
            Date date;
            while(sc.hasNextLine()){
                attributes = sc.nextLine().split("::");
                userId = attributes[0];
                itemId = attributes[1];
                rating = Float.parseFloat(attributes[2]);
                date = Date.from(Instant.ofEpochSecond(Long.parseLong(attributes[3])));
                doc = new Document();
                doc.append("userID", userId);
                doc.append("itemID", itemId);
                doc.append("rating", rating);
                doc.append("datetime", date);
                documentList.add(doc);
            }
            collection.insertMany(documentList);
            IndexOptions options = new IndexOptions();
            options.name("pairIDtime");
            Document indexConfig = new Document("userID", 1).append("itemID", 1).append("date", -1);
            collection.createIndex(indexConfig, options);
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static Map<Integer, String> mapValToKey(int[] keys, String[] vals){
        assert keys.length == vals.length;
        Map<Integer, String> map = new HashMap<>(keys.length * 2);
        for(int i = 0; i < keys.length; i++){
            map.put(keys[i], vals[i]);
        }
        return map;
    }

    public void insertUsers(){
        //creating age map
        int [] agekeys = {1, 18, 25, 35, 45, 50, 56};
        String[] agevals = {"Under 18", "18-24","25-34",  "35-44", "45-49","50-55", "56+"};
        Map<Integer, String> agemap = mapValToKey(agekeys, agevals);

        //creating job map, no need for map, keys are incremental
        String[] jobvals = {
                "other", "academic/educator", "artist", "clerical/admin","college/grad student",
                "customer service", "doctor/health care", "executive/managerial","farmer",
                "homemaker" ,"K-12 student" ,"lawyer","programmer" ,"retired" ,"sales/marketing"
                ,"scientist","self-employed" ,"technician/engineer" , "tradesman/craftsman" ,"unemployed","writer"};

        Map<String, String> gendermap = new HashMap<>();
        gendermap.put("M", "Male");
        gendermap.put("F", "Female");

        FileInputStream fis= null;
        try {
            fis = new FileInputStream(filePath  + Sp + "users.dat");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try (
                Scanner sc=new Scanner(fis);
                MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port));
        )
        {

            MongoDatabase database = mongoClient.getDatabase("movieLens");
            MongoCollection<Document> collection = database.getCollection("users");
            ArrayList<Document> documentList = new ArrayList<>();

            String[] attributes;
            while(sc.hasNextLine()){
                attributes = sc.nextLine().split("::");
                Document doc = new Document("_id", attributes[0])
                        .append("gender", gendermap.get(attributes[1]))
                        .append("age", agemap.get(Integer.parseInt(attributes[2])))
                        .append("occupation", jobvals[Integer.parseInt(attributes[3])])
                        .append("zip_code", attributes[4]);
                documentList.add(doc);
            }
            collection.insertMany(documentList);
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public void insertMovies() {
        FileInputStream fis= null;
        try {
            fis = new FileInputStream(filePath  + Sp + "movies.dat");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try (
                Scanner sc=new Scanner(fis);
                MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port));
        )
        {

            MongoDatabase database = mongoClient.getDatabase("movieLens");
            MongoCollection<Document> collection = database.getCollection("items");
            ArrayList<Document> documentList = new ArrayList<>();

            String[] attributes;
            while(sc.hasNextLine()){
                attributes = sc.nextLine().split("::");
                String id = attributes[0];
                String title = attributes[1];
                String genres = attributes[2];
                Document doc = new Document("_id", id)
                        .append("title", title)
                        .append("genres", genres);
                documentList.add(doc);
            }
            collection.insertMany(documentList);
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }
}
