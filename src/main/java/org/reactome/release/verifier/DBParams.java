package org.reactome.release.verifier;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 8/20/2024
 */
public class DBParams {

    private String userName;
    private String password;
    private String host;
    private long port;

    private DBParams(String userName, String password, String host, long port) {
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * Creates object of database parameters
     *
     * @param userName Database username
     * @param password Database password
     * @param host Database host
     * @param port Database port
     * @return Object of database parameters
     */
    public static DBParams create(String userName, String password, String host, long port) {
        return new DBParams(userName, password, host, port);
    }

    /**
     * @return Username
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * @return Password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @return Host
     */
    public String getHost() {
        return this.host;
    }

    /**
     * @return Port
     */
    public long getPort() {
        return this.port;
    }

    /**
     * Gets GraphDB Connection URI of form bolt://hostname:port
     *
     * @return GraphDB Connection URI
     */
    public String getGraphDBConnectionURI() {
        return String.format("bolt://%s:%d", getHost(), getPort());
    }

    /**
     * Get GraphDB Driver based on DB parameters in this object
     *
     * @return GraphDB Driver
     */
    public Driver getGraphDBDriver() {
        AuthToken connectionCredentials = AuthTokens.basic(getUserName(), getPassword());
        return GraphDatabase.driver(getGraphDBConnectionURI(), connectionCredentials);
    }
}
