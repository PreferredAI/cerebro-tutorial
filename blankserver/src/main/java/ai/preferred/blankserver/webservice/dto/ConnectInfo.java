package ai.preferred.blankserver.webservice.dto;

/**
 * @author hpminh@apcs.vn
 */
public class ConnectInfo {
    public String dbhost;
    public String dbport;

    public String getDbhost() {
        return dbhost;
    }

    public void setDbhost(String dbhost) {
        this.dbhost = dbhost;
    }

    public String getDbport() {
        return dbport;
    }

    public void setDbport(String dbport) {
        this.dbport = dbport;
    }
}
