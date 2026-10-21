package com.vehicleservice;

import com.vehicleservice.config.HBaseConfig;
import com.vehicleservice.dao.VehicleServiceDAO;
import com.vehicleservice.server.SimpleHttpServer;
import com.vehicleservice.util.SampleDataLoader;

/**
 * Main application entry point for the Vehicle Service Record Management System.
 */
public class Main {

    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port argument. Using default port: " + DEFAULT_PORT);
            }
        }

        System.out.println("================================================================");
        System.out.println("  VEHICLE SERVICE RECORD MANAGEMENT SYSTEM USING APACHE HBASE   ");
        System.out.println("================================================================");
        System.out.println("Initializing HBase DAO and Table Schema...");

        VehicleServiceDAO dao = new VehicleServiceDAO();

        try {
            // 1. Create HBase table if not exists
            dao.createTableIfNotExists();

            // 2. Check existing records, auto-populate sample data if table is fresh
            int count = dao.getAllRecords().size();
            System.out.println("Current records in HBase table '" + HBaseConfig.TABLE_NAME_STR + "': " + count);

            if (count == 0) {
                System.out.println("Populating realistic sample service records into HBase...");
                int loaded = SampleDataLoader.loadSampleData(dao);
                System.out.println("Successfully inserted " + loaded + " sample service records into HBase.");
            }

            // 3. Start the Web Server
            SimpleHttpServer server = new SimpleHttpServer(port, dao);
            server.start();

            // 4. Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down Vehicle Service Application...");
                server.stop();
                HBaseConfig.closeConnection();
                System.out.println("Application shutdown complete.");
            }));

        } catch (Exception e) {
            System.err.println("FATAL: Failed to initialize application with HBase: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
