package ai.preferred.blankserver.completed;


import ai.preferred.blankserver.export.Exporter;
import ai.preferred.blankserver.export.MongoExporter;
import ai.preferred.blankserver.webservice.dto.*;
import ai.preferred.blankserver.webservice.models.Items;
import ai.preferred.blankserver.webservice.repositories.ItemsRepository;
import ai.preferred.blankserver.webservice.repositories.UsersRepository;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

//import org.apache.lucene.document.Document;

/**
 * @author hpminh@apcs.vn
 */
@RestController
@RequestMapping("/blankapp")
public class AppController {
    static private String idxDir = "./idx";
    static private int k = 20;

    MongoCollection<Document> ratingCollection;
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    ItemsRepository itemsRepository;

    private QueryParser defaultParser;
    private IndexSearcher textSearcher;
    private String cerebro_url;

    AppController(){

        //Connecting to database
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String host = (String) properties.getOrDefault("spring.data.mongodb.host", "localhost");
        //System.getenv("MONGO_HOST");
        String port =(String) properties.getOrDefault("spring.data.mongodb.port", "27017");
        //System.getenv("MONGO_PORT");
        String db = (String) properties.getOrDefault("spring.data.mongodb.database", "movieLens");
        //System.getenv("MONGO_DATABASE");
        cerebro_url = (String) properties.get("cerebro.url");

        System.out.println("Read MONGO_HOST: " +  host);
        System.out.println("Read MONGO_PORT: " +  port);
        System.out.println("Read MONGO_DATABASE: " +  db);

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port));
        MongoDatabase database = mongoClient.getDatabase(db);
        ratingCollection = database.getCollection("ratings");


        //checking index, loading one up or building one
        Exception error = null;
        this.defaultParser = new QueryParser("title", new StandardAnalyzer());
        IndexReader reader = null;
        try{
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(idxDir)));
        }
        catch(IOException e){
            error = e;
        }
        //error not thrown to signify index built
        if(error == null)
            textSearcher = new IndexSearcher(reader , Executors.newFixedThreadPool(2));

        else
            buildIdxOnTitle(database.getCollection("items"));

    }


    private void buildIdxOnTitle(MongoCollection<Document> collection){
        IndexWriter writer = null;
        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(idxDir));
            IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
            writer = new IndexWriter(indexDirectory, iwc);
            for(Document item : collection.find()){
                org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
                doc.add(new StringField("ID", item.getString("_id"), Field.Store.YES));
                doc.add(new TextField("title", item.getString("title"), Field.Store.NO));
                writer.addDocument(doc);
            }
            writer.close();
            textSearcher= new IndexSearcher(DirectoryReader.open(indexDirectory), Executors.newFixedThreadPool(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/searchTitle", method = RequestMethod.POST)
    public ItemListResponse searchKeyword(@Valid @RequestBody TextQuery qObject) throws IOException {
        String keyword = qObject.getText();
        Query query = null;
        try {
            query = defaultParser.parse(keyword);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TopDocs hits = textSearcher.search(query, k);
        ArrayList<String> ids = new ArrayList<>();
        for (ScoreDoc hit : hits.scoreDocs) {
            String ID = textSearcher.doc(hit.doc).get("ID");
            ids.add(ID);
        }
        return new ItemListResponse((List<Items>) itemsRepository.findAllById(ids));
    }

    @CrossOrigin
    @RequestMapping(value="/getRating", method = RequestMethod.POST)
    public RatingResponse getRating(@Valid @RequestBody PairIds pairIds) throws IOException{
        Document query = new Document()
                .append("userID", pairIds.getUserId())
                .append("itemID", pairIds.getItemId());
        Document rating = ratingCollection.find(query).first();
        if(rating == null)
            return new RatingResponse(null);
        return new RatingResponse(rating.getDouble("rating"));
    }

    @CrossOrigin
    @RequestMapping(value="/setRating", method = RequestMethod.POST)
    public void setRating(@Valid @RequestBody Rating rating) throws IOException{
        Document doc = new Document()
                .append("userID", rating.userID)
                .append("itemID", rating.itemID)
                .append("rating", rating.rating)
                .append("datetime", Date.from(Instant.now()));
        ratingCollection.insertOne(doc);
    }

    @CrossOrigin
    @RequestMapping(value="/getRecom", method = RequestMethod.POST)
    public ItemListResponse getRecommendation(@Valid @RequestBody TextQuery qObject) throws IOException,
                                                                                    InterruptedException,
                                                                org.json.simple.parser.ParseException {
        //object to hold request body
        JSONObject requestObj = new JSONObject();
        //we will basically forwarding this to cerebro server
        requestObj.put("text", qObject.getText());

        URL obj = new URL(cerebro_url + "/search/getRecom");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(requestObj.toJSONString().getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);
        ArrayList<String> ids = new ArrayList<>();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            //success, parsing the resulting ids
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(response.toString());
            JSONArray jsonArray = (JSONArray) jsonObject.get("ids");
            for(Object id : jsonArray){
                ids.add((String) id);
            }
        } else {
            System.out.println("POST request not worked");
        }
        //from the list of ids query the database for the actual items
        return new ItemListResponse((List<Items>) itemsRepository.findAllById(ids));
    }

    @CrossOrigin
    @RequestMapping(value="/export", method = RequestMethod.POST)
    public void export(@Valid @RequestBody ConnectInfo info){
        Exporter exporter = new MongoExporter(usersRepository,
                itemsRepository,
                ratingCollection, info.dbhost, info.dbport);
        exporter.exportUserIds();
        exporter.exportItemIds();
        exporter.exportRatings();
    }


}
