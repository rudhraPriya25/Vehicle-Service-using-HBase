package com.vehicleservice.dao;

import com.vehicleservice.config.HBaseConfig;
import com.vehicleservice.model.ServiceRecord;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * VehicleServiceDAO handles all Apache HBase data access operations:
 * - DDL: CREATE TABLE, DROP TABLE, CHECK TABLE
 * - DML: PUT (Insert/Update), GET (Single Record), SCAN (All Records / Service History), DELETE
 */
public class VehicleServiceDAO {

    // Column Qualifiers
    // CF: vehicle
    public static final byte[] Q_VEHICLE_NUMBER = "vehicleNumber".getBytes();
    public static final byte[] Q_VEHICLE_TYPE = "vehicleType".getBytes();
    public static final byte[] Q_BRAND = "brand".getBytes();
    public static final byte[] Q_MODEL = "model".getBytes();
    public static final byte[] Q_MANUFACTURING_YEAR = "manufacturingYear".getBytes();

    // CF: customer
    public static final byte[] Q_CUSTOMER_NAME = "customerName".getBytes();
    public static final byte[] Q_PHONE = "phone".getBytes();
    public static final byte[] Q_EMAIL = "email".getBytes();
    public static final byte[] Q_ADDRESS = "address".getBytes();

    // CF: service
    public static final byte[] Q_SERVICE_ID = "serviceId".getBytes();
    public static final byte[] Q_SERVICE_DATE = "serviceDate".getBytes();
    public static final byte[] Q_SERVICE_TYPE = "serviceType".getBytes();
    public static final byte[] Q_PROBLEM_DESCRIPTION = "problemDescription".getBytes();
    public static final byte[] Q_PARTS_REPLACED = "partsReplaced".getBytes();
    public static final byte[] Q_SERVICE_COST = "serviceCost".getBytes();
    public static final byte[] Q_MECHANIC_NAME = "mechanicName".getBytes();
    public static final byte[] Q_NEXT_SERVICE_DATE = "nextServiceDate".getBytes();
    public static final byte[] Q_SERVICE_STATUS = "serviceStatus".getBytes();

    /**
     * Creates the 'vehicle_service' table with column families (vehicle, customer, service) if it doesn't already exist.
     */
    public synchronized void createTableIfNotExists() throws IOException {
        try (Admin admin = HBaseConfig.getAdmin()) {
            TableName tableName = HBaseConfig.TABLE_NAME;
            if (!admin.tableExists(tableName)) {
                TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(tableName);

                ColumnFamilyDescriptor cfVehicle = ColumnFamilyDescriptorBuilder.newBuilder(HBaseConfig.CF_VEHICLE).build();
                ColumnFamilyDescriptor cfCustomer = ColumnFamilyDescriptorBuilder.newBuilder(HBaseConfig.CF_CUSTOMER).build();
                ColumnFamilyDescriptor cfService = ColumnFamilyDescriptorBuilder.newBuilder(HBaseConfig.CF_SERVICE).build();

                tableDescriptorBuilder.setColumnFamily(cfVehicle);
                tableDescriptorBuilder.setColumnFamily(cfCustomer);
                tableDescriptorBuilder.setColumnFamily(cfService);

                admin.createTable(tableDescriptorBuilder.build());
                System.out.println("HBase Table '" + HBaseConfig.TABLE_NAME_STR + "' created successfully with Column Families: vehicle, customer, service.");
            } else {
                System.out.println("HBase Table '" + HBaseConfig.TABLE_NAME_STR + "' already exists.");
            }
        }
    }

    /**
     * Inserts or updates a service record in HBase using Put.
     * Row Key structure: vehicleNumber_serviceId (e.g. TN38AB1234_S001)
     */
    public boolean saveRecord(ServiceRecord record) throws IOException {
        if (record == null || record.getVehicleNumber().isEmpty() || record.getServiceId().isEmpty()) {
            throw new IllegalArgumentException("Vehicle Number and Service ID are required to construct the HBase Row Key.");
        }

        createTableIfNotExists();

        String rowKeyStr = record.getRowKey();
        byte[] rowKeyBytes = Bytes.toBytes(rowKeyStr);

        Put put = new Put(rowKeyBytes);

        // Populate CF: vehicle
        put.addColumn(HBaseConfig.CF_VEHICLE, Q_VEHICLE_NUMBER, Bytes.toBytes(record.getVehicleNumber()));
        put.addColumn(HBaseConfig.CF_VEHICLE, Q_VEHICLE_TYPE, Bytes.toBytes(record.getVehicleType() != null ? record.getVehicleType() : ""));
        put.addColumn(HBaseConfig.CF_VEHICLE, Q_BRAND, Bytes.toBytes(record.getBrand() != null ? record.getBrand() : ""));
        put.addColumn(HBaseConfig.CF_VEHICLE, Q_MODEL, Bytes.toBytes(record.getModel() != null ? record.getModel() : ""));
        put.addColumn(HBaseConfig.CF_VEHICLE, Q_MANUFACTURING_YEAR, Bytes.toBytes(record.getManufacturingYear() != null ? record.getManufacturingYear() : ""));

        // Populate CF: customer
        put.addColumn(HBaseConfig.CF_CUSTOMER, Q_CUSTOMER_NAME, Bytes.toBytes(record.getCustomerName() != null ? record.getCustomerName() : ""));
        put.addColumn(HBaseConfig.CF_CUSTOMER, Q_PHONE, Bytes.toBytes(record.getPhone() != null ? record.getPhone() : ""));
        put.addColumn(HBaseConfig.CF_CUSTOMER, Q_EMAIL, Bytes.toBytes(record.getEmail() != null ? record.getEmail() : ""));
        put.addColumn(HBaseConfig.CF_CUSTOMER, Q_ADDRESS, Bytes.toBytes(record.getAddress() != null ? record.getAddress() : ""));

        // Populate CF: service
        put.addColumn(HBaseConfig.CF_SERVICE, Q_SERVICE_ID, Bytes.toBytes(record.getServiceId()));
        put.addColumn(HBaseConfig.CF_SERVICE, Q_SERVICE_DATE, Bytes.toBytes(record.getServiceDate() != null ? record.getServiceDate() : ""));
        put.addColumn(HBaseConfig.CF_SERVICE, Q_SERVICE_TYPE, Bytes.toBytes(record.getServiceType() != null ? record.getServiceType() : ""));
        put.addColumn(HBaseConfig.CF_SERVICE, Q_PROBLEM_DESCRIPTION, Bytes.toBytes(record.getProblemDescription() != null ? record.getProblemDescription() : ""));
        put.addColumn(HBaseConfig.CF_SERVICE, Q_PARTS_REPLACED, Bytes.toBytes(record.getPartsReplaced() != null ? record.getPartsReplaced() : ""));
        put.addColumn(HBaseConfig.CF_SERVICE, Q_SERVICE_COST, Bytes.toBytes(record.getServiceCost() != null ? record.getServiceCost() : "0.00"));
        put.addColumn(HBaseConfig.CF_SERVICE, Q_MECHANIC_NAME, Bytes.toBytes(record.getMechanicName() != null ? record.getMechanicName() : ""));
        put.addColumn(HBaseConfig.CF_SERVICE, Q_NEXT_SERVICE_DATE, Bytes.toBytes(record.getNextServiceDate() != null ? record.getNextServiceDate() : ""));
        put.addColumn(HBaseConfig.CF_SERVICE, Q_SERVICE_STATUS, Bytes.toBytes(record.getServiceStatus() != null ? record.getServiceStatus() : "Pending"));

        try (Table table = HBaseConfig.getTable()) {
            table.put(put);
        }

        return true;
    }

    /**
     * Retrieves a single service record by Row Key using HBase Get.
     */
    public ServiceRecord getRecordByRowKey(String rowKey) throws IOException {
        if (rowKey == null || rowKey.trim().isEmpty()) {
            return null;
        }

        createTableIfNotExists();

        Get get = new Get(Bytes.toBytes(rowKey.trim().toUpperCase()));
        try (Table table = HBaseConfig.getTable()) {
            Result result = table.get(get);
            if (result == null || result.isEmpty()) {
                return null;
            }
            return mapResultToRecord(result);
        }
    }

    /**
     * Retrieves a single record by Vehicle Number and Service ID.
     */
    public ServiceRecord getRecord(String vehicleNumber, String serviceId) throws IOException {
        return getRecordByRowKey(ServiceRecord.computeRowKey(vehicleNumber, serviceId));
    }

    /**
     * Retrieves all service records from HBase using Table Scan.
     */
    public List<ServiceRecord> getAllRecords() throws IOException {
        createTableIfNotExists();
        List<ServiceRecord> records = new ArrayList<>();

        Scan scan = new Scan();
        try (Table table = HBaseConfig.getTable();
             ResultScanner scanner = table.getScanner(scan)) {
            for (Result result : scanner) {
                if (result != null && !result.isEmpty()) {
                    ServiceRecord record = mapResultToRecord(result);
                    if (record != null) {
                        records.add(record);
                    }
                }
            }
        }

        // Sort records reverse chronologically by serviceDate / serviceId for clean presentation
        records.sort((r1, r2) -> {
            int dateCmp = r2.getServiceDate().compareTo(r1.getServiceDate());
            if (dateCmp != 0) return dateCmp;
            return r2.getServiceId().compareTo(r1.getServiceId());
        });

        return records;
    }

    /**
     * Retrieves all service history records for a specific vehicle using HBase Prefix Scan.
     * Prefix scan leverages HBase row key indexing: [vehicleNumber_...]
     */
    public List<ServiceRecord> getServiceHistory(String vehicleNumber) throws IOException {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            return new ArrayList<>();
        }

        createTableIfNotExists();
        String normalizedVNum = vehicleNumber.trim().toUpperCase();
        String prefix = normalizedVNum + "_";

        List<ServiceRecord> history = new ArrayList<>();

        Scan scan = new Scan();
        scan.setFilter(new PrefixFilter(Bytes.toBytes(prefix)));

        try (Table table = HBaseConfig.getTable();
             ResultScanner scanner = table.getScanner(scan)) {
            for (Result result : scanner) {
                if (result != null && !result.isEmpty()) {
                    ServiceRecord record = mapResultToRecord(result);
                    if (record != null) {
                        history.add(record);
                    }
                }
            }
        }

        // Sort history chronologically
        history.sort(Comparator.comparing(ServiceRecord::getServiceDate).thenComparing(ServiceRecord::getServiceId));

        return history;
    }

    /**
     * Deletes a record from HBase using Row Key and HBase Delete.
     */
    public boolean deleteRecord(String rowKey) throws IOException {
        if (rowKey == null || rowKey.trim().isEmpty()) {
            return false;
        }

        createTableIfNotExists();

        Delete delete = new Delete(Bytes.toBytes(rowKey.trim().toUpperCase()));
        try (Table table = HBaseConfig.getTable()) {
            table.delete(delete);
        }
        return true;
    }

    /**
     * Helper method to map an HBase Result to a ServiceRecord object.
     */
    private ServiceRecord mapResultToRecord(Result result) {
        if (result == null || result.isEmpty()) {
            return null;
        }

        String rowKey = Bytes.toString(result.getRow());

        // CF: vehicle
        String vehicleNumber = Bytes.toString(result.getValue(HBaseConfig.CF_VEHICLE, Q_VEHICLE_NUMBER));
        String vehicleType = Bytes.toString(result.getValue(HBaseConfig.CF_VEHICLE, Q_VEHICLE_TYPE));
        String brand = Bytes.toString(result.getValue(HBaseConfig.CF_VEHICLE, Q_BRAND));
        String model = Bytes.toString(result.getValue(HBaseConfig.CF_VEHICLE, Q_MODEL));
        String manufacturingYear = Bytes.toString(result.getValue(HBaseConfig.CF_VEHICLE, Q_MANUFACTURING_YEAR));

        // CF: customer
        String customerName = Bytes.toString(result.getValue(HBaseConfig.CF_CUSTOMER, Q_CUSTOMER_NAME));
        String phone = Bytes.toString(result.getValue(HBaseConfig.CF_CUSTOMER, Q_PHONE));
        String email = Bytes.toString(result.getValue(HBaseConfig.CF_CUSTOMER, Q_EMAIL));
        String address = Bytes.toString(result.getValue(HBaseConfig.CF_CUSTOMER, Q_ADDRESS));

        // CF: service
        String serviceId = Bytes.toString(result.getValue(HBaseConfig.CF_SERVICE, Q_SERVICE_ID));
        String serviceDate = Bytes.toString(result.getValue(HBaseConfig.CF_SERVICE, Q_SERVICE_DATE));
        String serviceType = Bytes.toString(result.getValue(HBaseConfig.CF_SERVICE, Q_SERVICE_TYPE));
        String problemDescription = Bytes.toString(result.getValue(HBaseConfig.CF_SERVICE, Q_PROBLEM_DESCRIPTION));
        String partsReplaced = Bytes.toString(result.getValue(HBaseConfig.CF_SERVICE, Q_PARTS_REPLACED));
        String serviceCost = Bytes.toString(result.getValue(HBaseConfig.CF_SERVICE, Q_SERVICE_COST));
        String mechanicName = Bytes.toString(result.getValue(HBaseConfig.CF_SERVICE, Q_MECHANIC_NAME));
        String nextServiceDate = Bytes.toString(result.getValue(HBaseConfig.CF_SERVICE, Q_NEXT_SERVICE_DATE));
        String serviceStatus = Bytes.toString(result.getValue(HBaseConfig.CF_SERVICE, Q_SERVICE_STATUS));

        // If vehicleNumber or serviceId missing from column value, fallback to parsing row key
        if ((vehicleNumber == null || vehicleNumber.isEmpty()) && rowKey.contains("_")) {
            String[] parts = rowKey.split("_", 2);
            vehicleNumber = parts[0];
            if (serviceId == null || serviceId.isEmpty()) {
                serviceId = parts[1];
            }
        }

        ServiceRecord record = new ServiceRecord(
                vehicleNumber, vehicleType, brand, model, manufacturingYear,
                customerName, phone, email, address,
                serviceId, serviceDate, serviceType, problemDescription,
                partsReplaced, serviceCost, mechanicName, nextServiceDate, serviceStatus
        );
        record.setRowKey(rowKey);
        return record;
    }
}
