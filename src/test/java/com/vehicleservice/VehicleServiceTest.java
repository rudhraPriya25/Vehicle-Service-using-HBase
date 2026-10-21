package com.vehicleservice;

import com.vehicleservice.config.HBaseConfig;
import com.vehicleservice.dao.VehicleServiceDAO;
import com.vehicleservice.model.ServiceRecord;
import com.vehicleservice.util.SampleDataLoader;

import java.io.IOException;
import java.util.List;

/**
 * Direct Test Suite verifying all required HBase operations:
 * - Table Creation
 * - Put (Insert)
 * - Get (Read single)
 * - Scan (Read multiple)
 * - Prefix Scan (Service History)
 * - Put (Update)
 * - Delete (Remove)
 * - Validation & Edge Cases
 */
public class VehicleServiceTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("        RUNNING DIRECT HBASE TEST SUITE FOR VEHICLE SERVICE       ");
        System.out.println("=================================================================");

        VehicleServiceDAO dao = new VehicleServiceDAO();

        test("1. Test HBase Connection & Table Creation", () -> {
            dao.createTableIfNotExists();
            assertTrue(HBaseConfig.isHBaseConnected(), "HBase should be connected and responding.");
        });

        test("2. Test Sample Data Population (10 records)", () -> {
            int loaded = SampleDataLoader.loadSampleData(dao);
            assertTrue(loaded >= 10, "Should load at least 10 sample records.");
        });

        test("3. Test Scan All Records (HBase Table Scan)", () -> {
            List<ServiceRecord> records = dao.getAllRecords();
            assertTrue(records.size() >= 10, "Scan should return at least 10 records. Found: " + records.size());
            System.out.println("   [INFO] Scanned " + records.size() + " total records from HBase.");
        });

        test("4. Test Get Record by Row Key (HBase Get)", () -> {
            ServiceRecord record = dao.getRecordByRowKey("TN38AB1234_S001");
            assertTrue(record != null, "Record TN38AB1234_S001 should exist.");
            assertEquals("TN38AB1234", record.getVehicleNumber(), "Vehicle Number matches");
            assertEquals("Arun Kumar", record.getCustomerName(), "Customer Name matches");
            assertEquals("S001", record.getServiceId(), "Service ID matches");
            assertEquals("Hyundai", record.getBrand(), "Brand matches");
        });

        test("5. Test Service History by Vehicle Number (HBase Prefix Scan)", () -> {
            List<ServiceRecord> history = dao.getServiceHistory("TN38AB1234");
            assertTrue(history.size() >= 2, "Vehicle TN38AB1234 should have at least 2 service records. Found: " + history.size());
            for (ServiceRecord r : history) {
                assertEquals("TN38AB1234", r.getVehicleNumber(), "History record vehicle number");
            }
        });

        test("6. Test Insert New Record (HBase Put)", () -> {
            ServiceRecord testRecord = new ServiceRecord(
                    "TN99ZZ9999", "Supercar", "Porsche", "911 GT3 RS", "2024",
                    "Vikram Seth", "9944001122", "vikram.seth@gmail.com", "99, Aero City, Chennai",
                    "S100", "2026-10-01", "Track Tuning & Inspection", "Pre-track day preparation",
                    "Performance Brake Fluid, Carbon Ceramic Brake Inspection", "35000.00", "Master Tech John",
                    "2027-04-01", "Completed"
            );
            dao.saveRecord(testRecord);

            ServiceRecord fetched = dao.getRecordByRowKey("TN99ZZ9999_S100");
            assertTrue(fetched != null, "Newly inserted record should be found in HBase.");
            assertEquals("Porsche", fetched.getBrand(), "Brand matches");
            assertEquals("35000.00", fetched.getServiceCost(), "Service cost matches");
        });

        test("7. Test Update Record (HBase Put Overwrite)", () -> {
            ServiceRecord fetched = dao.getRecordByRowKey("TN99ZZ9999_S100");
            assertTrue(fetched != null, "Target record must exist.");
            fetched.setServiceCost("38500.00");
            fetched.setServiceStatus("Delivered");
            fetched.setPartsReplaced("Performance Brake Fluid, Carbon Ceramic Brake Inspection, Track Alignment");

            dao.saveRecord(fetched);

            ServiceRecord updated = dao.getRecordByRowKey("TN99ZZ9999_S100");
            assertEquals("38500.00", updated.getServiceCost(), "Updated service cost matches");
            assertEquals("Delivered", updated.getServiceStatus(), "Updated status matches");
        });

        test("8. Test Delete Record (HBase Delete)", () -> {
            boolean deleted = dao.deleteRecord("TN99ZZ9999_S100");
            assertTrue(deleted, "Delete should return true.");

            ServiceRecord nonExistent = dao.getRecordByRowKey("TN99ZZ9999_S100");
            assertTrue(nonExistent == null, "Record should no longer exist in HBase after deletion.");
        });

        test("9. Test Non-Existing Vehicle Query", () -> {
            List<ServiceRecord> emptyHistory = dao.getServiceHistory("NON_EXISTENT_VEHICLE");
            assertTrue(emptyHistory.isEmpty(), "Non-existing vehicle history must be empty.");
            ServiceRecord emptyGet = dao.getRecordByRowKey("NON_EXISTENT_ROW_KEY");
            assertTrue(emptyGet == null, "Non-existing row key Get must return null.");
        });

        System.out.println("=================================================================");
        System.out.println(" TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("=================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void test(String name, TestCase testCase) {
        try {
            System.out.print("[TEST] " + name + " ... ");
            testCase.run();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED! " + t.getMessage());
            t.printStackTrace();
            failed++;
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + msg);
        }
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion Failed: " + msg + " (Expected: " + expected + ", Actual: " + actual + ")");
    }

    @FunctionalInterface
    interface TestCase {
        void run() throws Throwable;
    }
}
