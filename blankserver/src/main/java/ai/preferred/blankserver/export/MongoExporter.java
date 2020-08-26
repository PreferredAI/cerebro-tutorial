package ai.preferred.blankserver.export;

import ai.preferred.blankserver.webservice.models.Items;
import ai.preferred.blankserver.webservice.models.Users;
import ai.preferred.blankserver.webservice.repositories.ItemsRepository;
import ai.preferred.blankserver.webservice.repositories.UsersRepository;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import java.util.ArrayList;

/**
 * @author hpminh@apcs.vn
 */
public class MongoExporter implements Exporter {
    UsersRepository usersRepository;
    ItemsRepository itemsRepository;
    MongoCollection<Document> ratingCollection;

    MongoCollection<Document> cRatings;
    MongoCollection<Document> cUsers;
    MongoCollection<Document> cItems;

    String cerebroDBHost;
    String cerebroDBPort;

    public MongoExporter(UsersRepository usersRepository, ItemsRepository itemsRepository, MongoCollection<Document> ratingCollection, String cerebroDBHost, String cerebroDBPort) {
        this.usersRepository = usersRepository;
        this.itemsRepository = itemsRepository;
        this.ratingCollection = ratingCollection;
        this.cerebroDBHost = cerebroDBHost;
        this.cerebroDBPort = cerebroDBPort;

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + cerebroDBHost + ":" + cerebroDBPort));
        MongoDatabase database = mongoClient.getDatabase("cerebro");
        cUsers = database.getCollection("users");
        cItems = database.getCollection("items");
        cRatings = database.getCollection("ratings");
    }

    @Override
    public void exportItemIds(){
        ArrayList<Document> ids = new ArrayList<>((int)itemsRepository.count());
        for(Items item : itemsRepository.findAll()){
            ids.add(new Document("_id", item._id));
        }
        cItems.insertMany(ids);
    }

    @Override
    public void exportUserIds(){
        ArrayList<Document> ids = new ArrayList<>((int)usersRepository.count());
        for(Users users : usersRepository.findAll()){
            ids.add(new Document("_id", users.get_id()));
        }
        cUsers.insertMany(ids);
    }

    @Override
    public void exportRatings(){
        int buffersize = 1_000_000;
        ArrayList<Document> ratings = new ArrayList<>(buffersize);
        Document cDoc;
        for (Document docRating: ratingCollection.find()) {
            cDoc = new Document();
            cDoc.append("userID", docRating.getString("userID"));
            cDoc.append("itemID", docRating.getString("itemID"));
            cDoc.append("rating", docRating.getDouble("rating"));
            cDoc.append("datetime", docRating.getDate("datetime"));
            ratings.add(cDoc);
            if(ratings.size() == buffersize){
                cRatings.insertMany(ratings);
                ratings.clear();
                System.gc();
            }
        }
        cRatings.insertMany(ratings);
        IndexOptions options = new IndexOptions();
        options.name("pairIDtime");
        Document indexConfig = new Document("userID", 1).append("itemID", 1).append("datetime", -1);
        cRatings.createIndex(indexConfig, options);

    }

    /*
    public void exportUserRatings(){
        for (Users user: usersRepository.findAll()) {
            FindIterable<Document> iterable = ratingCollection.find(new Document("userID", user.get_id()));
            Document doc = new Document("_id", user.get_id());
            Document temp = iterable.first();
            iterable.skip(1);
            String itemID = temp.getString("itemID");
            String prevID = itemID;
            Double rating = temp.getDouble("rating");
            doc.append(itemID, rating);
            for(Document rateDoc : iterable){
                itemID = rateDoc.getString("itemID");
                if(itemID.equals(prevID))
                    continue;
                prevID = itemID;
                rating = rateDoc.getDouble("rating");
                doc.append(itemID, rating);

            }
            cUserRatings.insertOne(doc);
        }
    }


     */
}
