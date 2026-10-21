package com.vehicleservice.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

/**
 * HBaseConfig manages the Apache HBase connection and configuration.
 * Connects to the local standalone HBase cluster via ZooKeeper.
 */
public class HBaseConfig {

    public static final String TABLE_NAME_STR = "vehicle_service";
    public static final TableName TABLE_NAME = TableName.valueOf(TABLE_NAME_STR);

    // Column Families
    public static final byte[] CF_VEHICLE = "vehicle".getBytes();
    public static final byte[] CF_CUSTOMER = "customer".getBytes();
    public static final byte[] CF_SERVICE = "service".getBytes();

    private static volatile Connection connection = null;
    private static final Object lock = new Object();

    /**
     * Initializes and returns HBase Configuration.
     */
    public static Configuration getConfiguration() {
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "127.0.0.1");
        config.set("hbase.zookeeper.property.clientPort", "2181");
        config.set("hbase.rpc.timeout", "5000");
        config.set("hbase.client.retries.number", "3");
        config.set("hbase.client.pause", "500");
        config.set("zookeeper.session.timeout", "6000");
        return config;
    }

    /**
     * Obtains the shared singleton HBase Connection instance.
     */
    public static Connection getConnection() throws IOException {
        if (connection == null || connection.isClosed()) {
            synchronized (lock) {
                if (connection == null || connection.isClosed()) {
                    connection = ConnectionFactory.createConnection(getConfiguration());
                }
            }
        }
        return connection;
    }

    /**
     * Obtains an HBase Table instance for vehicle_service.
     */
    public static Table getTable() throws IOException {
        return getConnection().getTable(TABLE_NAME);
    }

    /**
     * Obtains an HBase Admin instance for DDL operations.
     */
    public static Admin getAdmin() throws IOException {
        return getConnection().getAdmin();
    }

    /**
     * Tests whether HBase connection is alive and reachable.
     */
    public static boolean isHBaseConnected() {
        try {
            Admin admin = getAdmin();
            boolean isRunning = admin.isMasterInMaintenanceMode() || true;
            admin.close();
            return isRunning;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes the HBase Connection safely.
     */
    public static void closeConnection() {
        synchronized (lock) {
            if (connection != null && !connection.isClosed()) {
                try {
                    connection.close();
                } catch (IOException e) {
                    System.err.println("Error closing HBase connection: " + e.getMessage());
                }
            }
        }
    }
}
