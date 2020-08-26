package Main;

import extractor.DATExtractor;
import extractor.Extractor;
import org.apache.commons.cli.*;

/**
 * @author hpminh@apcs.vn
 */
public class Main {
    public static void main(String[] args){

        Options options = new Options();

        Option pathOpt = new Option("fp", "filePath", true, "path to the movielens folder");
        pathOpt.setRequired(false);
        options.addOption(pathOpt);

        Option defOpt = new Option("d", "default", false, "Secret argument, used by the developer only!");
        defOpt.setRequired(false);
        options.addOption(defOpt);

        Option hostOpt = new Option("h", "host", true, "mongodb hostname");
        hostOpt.setRequired(false);
        options.addOption(hostOpt);

        Option postOpt = new Option("p", "port", true, "mongodb port number");
        postOpt.setRequired(false);
        options.addOption(postOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        String filePath = cmd.getOptionValue("filePath");
        String host = cmd.getOptionValue("host");
        String port = cmd.getOptionValue("port");

        Extractor dataParser = null;
        if(filePath == null && !cmd.hasOption("default")){
            System.out.println("No filepath specified, Operation aborted.");
            System.exit(1);
        }
        else if(filePath != null){
            dataParser = new Extractor(filePath);
        }
        else
            dataParser = new Extractor();

        if(host != null)
            dataParser.setHost(host);
        if(port != null)
            dataParser.setPort(port);

        dataParser.extract();

    }
}
