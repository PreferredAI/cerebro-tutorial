package extractor;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;

/**
 * @author hpminh@apcs.vn
 */
public class Extractor {
    public static final char Sp = SystemUtils.IS_OS_WINDOWS ? '\\' : '/';
    String host;
    String port;
    String filePath;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Extractor(){
        host = "localhost";
        port = "27017";
        this.filePath = "/Users/ulfbert/movieLens/ml-1m";
    }

    public Extractor(String filePath) {
        this();
        this.filePath = filePath;
    }

    public void extract(){
        File csvfile = new File(filePath + Sp+ "ratings.csv");
        File datfile = new File(filePath + Sp + "ratings.dat");
        if(datfile.exists() && datfile.isFile()){
            DATExtractor extractor = new DATExtractor(filePath, host, port);
            extractor.extract();
        }
        else if(csvfile.exists() && csvfile.isFile()){
            CSVExtractor extractor = new CSVExtractor(filePath, host, port);
            extractor.extract();

        }
        else
            System.out.println("File not found, operation aborted");
    }
}
