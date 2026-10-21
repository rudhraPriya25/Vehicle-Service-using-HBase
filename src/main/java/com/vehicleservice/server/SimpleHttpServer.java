package com.vehicleservice.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vehicleservice.config.HBaseConfig;
import com.vehicleservice.dao.VehicleServiceDAO;
import com.vehicleservice.model.ServiceRecord;
import com.vehicleservice.util.JsonUtil;
import com.vehicleservice.util.SampleDataLoader;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * SimpleHttpServer provides a clean, robust, lightweight HTTP server using Java's built-in
 * com.sun.net.httpserver. It serves both the Frontend static UI assets and the REST API
 * endpoints that communicate directly with Apache HBase.
 */
public class SimpleHttpServer {

    private final int port;
    private final VehicleServiceDAO dao;
    private HttpServer server;
    private final Path frontendPath;

    public SimpleHttpServer(int port, VehicleServiceDAO dao) {
        this.port = port;
        this.dao = dao;
        this.frontendPath = Paths.get("frontend").toAbsolutePath().normalize();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // REST API Handlers
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/dashboard", new DashboardHandler());
        server.createContext("/api/records", new RecordsHandler());
        server.createContext("/api/history", new HistoryHandler());
        server.createContext("/api/sample-data", new SampleDataHandler());

        // Static Web UI Handler
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("==================================================================");
        System.out.println(" Vehicle Service Record Management System (HBase Backend) Started ");
        System.out.println(" Application URL: http://localhost:" + port);
        System.out.println(" API Endpoint:    http://localhost:" + port + "/api/status");
        System.out.println(" HBase Table:     " + HBaseConfig.TABLE_NAME_STR);
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            System.out.println("HTTP Server stopped.");
        }
    }

    // --- Helper Methods ---

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String val = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, val);
        }
        return params;
    }

    // --- Handlers ---

    /**
     * GET /api/status - Health check & HBase connection status.
     */
    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            boolean connected = HBaseConfig.isHBaseConnected();
            String response = "{" +
                    "\"status\":\"" + (connected ? "connected" : "disconnected") + "\"," +
                    "\"hbaseTable\":\"" + HBaseConfig.TABLE_NAME_STR + "\"," +
                    "\"columnFamilies\":[\"vehicle\",\"customer\",\"service\"]," +
                    "\"rowKeyPattern\":\"vehicleNumber_serviceId\"," +
                    "\"serverTime\":\"" + new Date() + "\"" +
                    "}";
            sendJsonResponse(exchange, connected ? 200 : 503, response);
        }
    }

    /**
     * GET /api/dashboard - Summary statistics and recent activity.
     */
    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            try {
                List<ServiceRecord> allRecords = dao.getAllRecords();

                Set<String> uniqueVehicles = new HashSet<>();
                Set<String> uniqueCustomers = new HashSet<>();
                double totalRevenue = 0.0;
                int completedCount = 0;
                int inProgressCount = 0;
                int pendingCount = 0;
                int deliveredCount = 0;

                for (ServiceRecord r : allRecords) {
                    if (r.getVehicleNumber() != null && !r.getVehicleNumber().isEmpty()) {
                        uniqueVehicles.add(r.getVehicleNumber());
                    }
                    if (r.getCustomerName() != null && !r.getCustomerName().isEmpty()) {
                        uniqueCustomers.add(r.getCustomerName());
                    }
                    try {
                        totalRevenue += Double.parseDouble(r.getServiceCost().replaceAll("[^0-9.]", ""));
                    } catch (Exception ignored) {}

                    String status = r.getServiceStatus() != null ? r.getServiceStatus().trim().toLowerCase() : "";
                    if (status.contains("complete")) completedCount++;
                    else if (status.contains("progress")) inProgressCount++;
                    else if (status.contains("deliver")) deliveredCount++;
                    else pendingCount++;
                }

                List<ServiceRecord> recent = allRecords.size() > 5 ? allRecords.subList(0, 5) : allRecords;

                String json = "{" +
                        "\"totalRecords\":" + allRecords.size() + "," +
                        "\"totalVehicles\":" + uniqueVehicles.size() + "," +
                        "\"totalCustomers\":" + uniqueCustomers.size() + "," +
                        "\"totalRevenue\":" + String.format(Locale.US, "%.2f", totalRevenue) + "," +
                        "\"completedCount\":" + completedCount + "," +
                        "\"inProgressCount\":" + inProgressCount + "," +
                        "\"pendingCount\":" + pendingCount + "," +
                        "\"deliveredCount\":" + deliveredCount + "," +
                        "\"recentRecords\":" + JsonUtil.recordListToJson(recent) +
                        "}";

                sendJsonResponse(exchange, 200, JsonUtil.success("Dashboard metrics retrieved", json));
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, JsonUtil.error("Failed to load dashboard metrics from HBase: " + e.getMessage()));
            }
        }
    }

    /**
     * /api/records - CRUD operations:
     * GET: Scan all records OR Get single record (by rowKey or vehicleNumber & serviceId)
     * POST: Add new record (HBase Put)
     * PUT: Update existing record (HBase Put)
     * DELETE: Delete record (HBase Delete)
     */
    private class RecordsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            try {
                if ("GET".equals(method)) {
                    Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                    String rowKey = params.get("rowKey");
                    String vehicleNumber = params.get("vehicleNumber");
                    String serviceId = params.get("serviceId");

                    if (rowKey != null && !rowKey.isEmpty()) {
                        ServiceRecord record = dao.getRecordByRowKey(rowKey);
                        if (record != null) {
                            sendJsonResponse(exchange, 200, JsonUtil.success("Record found", JsonUtil.recordToJson(record)));
                        } else {
                            sendJsonResponse(exchange, 404, JsonUtil.error("Record not found for Row Key: " + rowKey));
                        }
                    } else if (vehicleNumber != null && serviceId != null) {
                        ServiceRecord record = dao.getRecord(vehicleNumber, serviceId);
                        if (record != null) {
                            sendJsonResponse(exchange, 200, JsonUtil.success("Record found", JsonUtil.recordToJson(record)));
                        } else {
                            sendJsonResponse(exchange, 404, JsonUtil.error("Record not found for Vehicle: " + vehicleNumber + ", Service: " + serviceId));
                        }
                    } else {
                        List<ServiceRecord> records = dao.getAllRecords();
                        sendJsonResponse(exchange, 200, JsonUtil.success("All records retrieved from HBase", JsonUtil.recordListToJson(records)));
                    }
                } else if ("POST".equals(method)) {
                    String body = readRequestBody(exchange);
                    ServiceRecord record = JsonUtil.parseServiceRecord(body);

                    if (record == null || record.getVehicleNumber().isEmpty() || record.getServiceId().isEmpty()) {
                        sendJsonResponse(exchange, 400, JsonUtil.error("Vehicle Number and Service ID are required."));
                        return;
                    }
                    if (record.getCustomerName().isEmpty()) {
                        sendJsonResponse(exchange, 400, JsonUtil.error("Customer Name is required."));
                        return;
                    }

                    // Check for duplicate on create
                    ServiceRecord existing = dao.getRecordByRowKey(record.getRowKey());
                    if (existing != null) {
                        sendJsonResponse(exchange, 409, JsonUtil.error("Service Record with Row Key '" + record.getRowKey() + "' already exists in HBase. Use Update to modify."));
                        return;
                    }

                    dao.saveRecord(record);
                    sendJsonResponse(exchange, 201, JsonUtil.success("Vehicle service record added to HBase successfully.", JsonUtil.recordToJson(record)));
                } else if ("PUT".equals(method)) {
                    String body = readRequestBody(exchange);
                    ServiceRecord record = JsonUtil.parseServiceRecord(body);

                    if (record == null || record.getVehicleNumber().isEmpty() || record.getServiceId().isEmpty()) {
                        sendJsonResponse(exchange, 400, JsonUtil.error("Vehicle Number and Service ID are required."));
                        return;
                    }

                    dao.saveRecord(record);
                    sendJsonResponse(exchange, 200, JsonUtil.success("Service record updated in HBase successfully.", JsonUtil.recordToJson(record)));
                } else if ("DELETE".equals(method)) {
                    Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                    String rowKey = params.get("rowKey");

                    if (rowKey == null || rowKey.trim().isEmpty()) {
                        sendJsonResponse(exchange, 400, JsonUtil.error("Row Key query parameter is required for deletion."));
                        return;
                    }

                    ServiceRecord existing = dao.getRecordByRowKey(rowKey);
                    if (existing == null) {
                        sendJsonResponse(exchange, 404, JsonUtil.error("Cannot delete: Record not found for Row Key: " + rowKey));
                        return;
                    }

                    dao.deleteRecord(rowKey);
                    sendJsonResponse(exchange, 200, JsonUtil.success("Record '" + rowKey + "' deleted from HBase successfully.", "null"));
                } else {
                    sendJsonResponse(exchange, 405, JsonUtil.error("Method not allowed: " + method));
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, JsonUtil.error("HBase Operation Error: " + e.getMessage()));
            }
        }
    }

    /**
     * GET /api/history - Retrieve all services for a specific vehicle using HBase Prefix Scan.
     */
    private class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            try {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String vehicleNumber = params.get("vehicleNumber");

                if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
                    sendJsonResponse(exchange, 400, JsonUtil.error("vehicleNumber parameter is required."));
                    return;
                }

                List<ServiceRecord> history = dao.getServiceHistory(vehicleNumber);
                if (history.isEmpty()) {
                    sendJsonResponse(exchange, 404, JsonUtil.error("No service history records found in HBase for Vehicle Number: " + vehicleNumber.toUpperCase()));
                } else {
                    // Extract common vehicle and customer details from the most recent record
                    ServiceRecord latest = history.get(history.size() - 1);
                    String vehicleCustomerInfo = "{" +
                            "\"vehicleNumber\":\"" + JsonUtil.escapeJson(latest.getVehicleNumber()) + "\"," +
                            "\"vehicleType\":\"" + JsonUtil.escapeJson(latest.getVehicleType()) + "\"," +
                            "\"brand\":\"" + JsonUtil.escapeJson(latest.getBrand()) + "\"," +
                            "\"model\":\"" + JsonUtil.escapeJson(latest.getModel()) + "\"," +
                            "\"manufacturingYear\":\"" + JsonUtil.escapeJson(latest.getManufacturingYear()) + "\"," +
                            "\"customerName\":\"" + JsonUtil.escapeJson(latest.getCustomerName()) + "\"," +
                            "\"phone\":\"" + JsonUtil.escapeJson(latest.getPhone()) + "\"," +
                            "\"email\":\"" + JsonUtil.escapeJson(latest.getEmail()) + "\"," +
                            "\"address\":\"" + JsonUtil.escapeJson(latest.getAddress()) + "\"," +
                            "\"totalServices\":" + history.size() + "," +
                            "\"history\":" + JsonUtil.recordListToJson(history) +
                            "}";

                    sendJsonResponse(exchange, 200, JsonUtil.success("Service history retrieved from HBase", vehicleCustomerInfo));
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, JsonUtil.error("HBase Service History Scan Error: " + e.getMessage()));
            }
        }
    }

    /**
     * POST /api/sample-data - Loads 10 realistic sample records into HBase.
     */
    private class SampleDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            try {
                int count = SampleDataLoader.loadSampleData(dao);
                sendJsonResponse(exchange, 200, JsonUtil.success(count + " sample service records loaded into HBase table '" + HBaseConfig.TABLE_NAME_STR + "' successfully.", "{\"inserted\":" + count + "}"));
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, JsonUtil.error("Failed to insert sample records into HBase: " + e.getMessage()));
            }
        }
    }

    /**
     * Serves static HTML/CSS/JS files from the frontend directory.
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/") || requestPath.isEmpty()) {
                requestPath = "/index.html";
            }

            // Prevent path traversal
            Path resolvedPath = frontendPath.resolve(requestPath.substring(1)).normalize();
            if (!resolvedPath.startsWith(frontendPath) || !Files.exists(resolvedPath) || Files.isDirectory(resolvedPath)) {
                // Fallback to index.html or 404
                resolvedPath = frontendPath.resolve("index.html");
                if (!Files.exists(resolvedPath)) {
                    String notFound = "<h1>404 Not Found</h1>";
                    exchange.sendResponseHeaders(404, notFound.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(notFound.getBytes(StandardCharsets.UTF_8));
                    }
                    return;
                }
            }

            String contentType = getContentType(resolvedPath.toString());
            byte[] fileBytes = Files.readAllBytes(resolvedPath);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".json")) return "application/json; charset=UTF-8";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".ico")) return "image/x-icon";
            return "text/plain; charset=UTF-8";
        }
    }
}
