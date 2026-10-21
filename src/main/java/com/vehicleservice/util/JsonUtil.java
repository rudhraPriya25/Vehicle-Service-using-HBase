package com.vehicleservice.util;

import com.vehicleservice.model.ServiceRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonUtil provides simple, robust JSON serialization and parsing
 * without requiring external heavy third-party libraries.
 */
public class JsonUtil {

    public static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static String recordToJson(ServiceRecord r) {
        if (r == null) return "null";
        return "{" +
                "\"rowKey\":\"" + escapeJson(r.getRowKey()) + "\"," +
                "\"vehicleNumber\":\"" + escapeJson(r.getVehicleNumber()) + "\"," +
                "\"vehicleType\":\"" + escapeJson(r.getVehicleType()) + "\"," +
                "\"brand\":\"" + escapeJson(r.getBrand()) + "\"," +
                "\"model\":\"" + escapeJson(r.getModel()) + "\"," +
                "\"manufacturingYear\":\"" + escapeJson(r.getManufacturingYear()) + "\"," +
                "\"customerName\":\"" + escapeJson(r.getCustomerName()) + "\"," +
                "\"phone\":\"" + escapeJson(r.getPhone()) + "\"," +
                "\"email\":\"" + escapeJson(r.getEmail()) + "\"," +
                "\"address\":\"" + escapeJson(r.getAddress()) + "\"," +
                "\"serviceId\":\"" + escapeJson(r.getServiceId()) + "\"," +
                "\"serviceDate\":\"" + escapeJson(r.getServiceDate()) + "\"," +
                "\"serviceType\":\"" + escapeJson(r.getServiceType()) + "\"," +
                "\"problemDescription\":\"" + escapeJson(r.getProblemDescription()) + "\"," +
                "\"partsReplaced\":\"" + escapeJson(r.getPartsReplaced()) + "\"," +
                "\"serviceCost\":\"" + escapeJson(r.getServiceCost()) + "\"," +
                "\"mechanicName\":\"" + escapeJson(r.getMechanicName()) + "\"," +
                "\"nextServiceDate\":\"" + escapeJson(r.getNextServiceDate()) + "\"," +
                "\"serviceStatus\":\"" + escapeJson(r.getServiceStatus()) + "\"" +
                "}";
    }

    public static String recordListToJson(List<ServiceRecord> records) {
        if (records == null) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < records.size(); i++) {
            sb.append(recordToJson(records.get(i)));
            if (i < records.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String success(String message, Object dataJson) {
        return "{" +
                "\"status\":\"success\"," +
                "\"message\":\"" + escapeJson(message) + "\"," +
                "\"data\":" + (dataJson != null ? dataJson.toString() : "null") +
                "}";
    }

    public static String error(String message) {
        return "{" +
                "\"status\":\"error\"," +
                "\"message\":\"" + escapeJson(message) + "\"" +
                "}";
    }

    /**
     * Parses a simple JSON object string into a Map of key-value pairs.
     */
    public static Map<String, String> parseJsonMap(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        // Matches "key" : "value" or "key" : 123
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"((?:\\\\\"|[^\"])*)\"|([0-9.]+)|(true|false|null))");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String key = matcher.group(1);
            String strVal = matcher.group(2);
            String numVal = matcher.group(3);
            String boolVal = matcher.group(4);

            String value = "";
            if (strVal != null) {
                value = strVal.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
            } else if (numVal != null) {
                value = numVal;
            } else if (boolVal != null) {
                value = boolVal.equals("null") ? "" : boolVal;
            }
            map.put(key, value);
        }
        return map;
    }

    /**
     * Converts a JSON string representing a ServiceRecord payload to a ServiceRecord object.
     */
    public static ServiceRecord parseServiceRecord(String json) {
        Map<String, String> map = parseJsonMap(json);
        if (map.isEmpty()) return null;

        return new ServiceRecord(
                map.getOrDefault("vehicleNumber", ""),
                map.getOrDefault("vehicleType", ""),
                map.getOrDefault("brand", ""),
                map.getOrDefault("model", ""),
                map.getOrDefault("manufacturingYear", ""),
                map.getOrDefault("customerName", ""),
                map.getOrDefault("phone", ""),
                map.getOrDefault("email", ""),
                map.getOrDefault("address", ""),
                map.getOrDefault("serviceId", ""),
                map.getOrDefault("serviceDate", ""),
                map.getOrDefault("serviceType", ""),
                map.getOrDefault("problemDescription", ""),
                map.getOrDefault("partsReplaced", ""),
                map.getOrDefault("serviceCost", "0.00"),
                map.getOrDefault("mechanicName", ""),
                map.getOrDefault("nextServiceDate", ""),
                map.getOrDefault("serviceStatus", "Pending")
        );
    }
}
