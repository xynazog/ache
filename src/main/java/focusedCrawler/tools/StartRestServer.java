package focusedCrawler.tools;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.codahale.metrics.MetricRegistry;

import focusedCrawler.Main;
import focusedCrawler.config.ConfigService;
import focusedCrawler.rest.RestConfig;
import focusedCrawler.rest.RestServer;
import focusedCrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "StartRestServer", description = "Start a REST API for to manage web crawls")
public class StartRestServer extends CliTool {

    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    @Option(name = {"-p", "--port"}, required = false,
            description = "Port at which the web server will be available")
    private int port = 8080;

    private String host = "0.0.0.0";

    private boolean enableCors = true;

    @Option(name = {"-d", "--data"}, required = false,
            description = "Path to folder where server should store its data")
    private String data = "server-data";

    @Option(name = {"-c", "--config"}, required = false,
            description = "Path to the configuration file")
    private String configPath = null;

    @Option(name = {"-e", "--elasticIndex"}, required = false,
            description = "Name of Elasticsearch index to be used")
    private String indexName;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new StartRestServer());
    }

    @Override
    public void execute() throws Exception {
        MetricRegistry metricsRegistry = new MetricRegistry();

        RestServer server = null;
        if(configPath != null && !configPath.isEmpty()) {
            
            if(Files.isDirectory(Paths.get(configPath))) {
                configPath = Paths.get(configPath, "ache.yml").toString();
            }
            
            ConfigService config = new ConfigService(configPath);
            server = RestServer.create(metricsRegistry, config, indexName);
        }
        
        if(server == null) {
            RestConfig restConfig = new RestConfig(host, port, enableCors);
            server = RestServer.create(restConfig, metricsRegistry);
        }
        
        server.start();
        
    }

}
