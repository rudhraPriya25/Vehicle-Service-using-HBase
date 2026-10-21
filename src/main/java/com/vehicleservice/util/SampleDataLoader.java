package com.vehicleservice.util;

import com.vehicleservice.dao.VehicleServiceDAO;
import com.vehicleservice.model.ServiceRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SampleDataLoader populates HBase with realistic vehicle service records.
 */
public class SampleDataLoader {

    public static List<ServiceRecord> getSampleRecords() {
        List<ServiceRecord> list = new ArrayList<>();

        // 1. TN38AB1234 - Arun Kumar - Hyundai Creta (Service 1)
        list.add(new ServiceRecord(
                "TN38AB1234", "SUV", "Hyundai", "Creta 1.5 SX", "2023",
                "Arun Kumar", "9876543210", "arun.kumar@gmail.com", "12, Gandhi Puram, Coimbatore",
                "S001", "2025-10-15", "Periodic Maintenance", "Engine oil change and general inspection",
                "Synthetic Engine Oil (4L), Oil Filter, Air Filter", "5800.00", "Rajesh Sharma",
                "2026-04-15", "Completed"
        ));

        // 2. TN38AB1234 - Arun Kumar - Hyundai Creta (Service 2)
        list.add(new ServiceRecord(
                "TN38AB1234", "SUV", "Hyundai", "Creta 1.5 SX", "2023",
                "Arun Kumar", "9876543210", "arun.kumar@gmail.com", "12, Gandhi Puram, Coimbatore",
                "S002", "2026-04-10", "Brake Overhaul & AC Service", "Squeaking brake noise and AC cooling drop",
                "Front Brake Pads, Brake Fluid, Cabin AC Filter, AC Gas Refill", "8450.00", "Senthil Nathan",
                "2026-10-10", "Completed"
        ));

        // 3. TN38CD5678 - Priya Soundar - Honda City (Service 1)
        list.add(new ServiceRecord(
                "TN38CD5678", "Sedan", "Honda", "City ZX i-VTEC", "2022",
                "Priya Soundar", "9843212345", "priya.soundar@yahoo.com", "45, RS Puram West, Coimbatore",
                "S001", "2025-11-20", "Wheel & Suspension Alignment", "Vibration on steering at highway speeds",
                "Wheel Balancing Weights, Front Tie Rod Bushings", "3200.00", "Karthik Subramanian",
                "2026-05-20", "Completed"
        ));

        // 4. TN38CD5678 - Priya Soundar - Honda City (Service 2)
        list.add(new ServiceRecord(
                "TN38CD5678", "Sedan", "Honda", "City ZX i-VTEC", "2022",
                "Priya Soundar", "9843212345", "priya.soundar@yahoo.com", "45, RS Puram West, Coimbatore",
                "S002", "2026-05-18", "Major 40K KM Service", "Scheduled comprehensive service and spark plug check",
                "Engine Oil, Iridium Spark Plugs, Coolant Flush, Transmission Oil", "11200.00", "Rajesh Sharma",
                "2026-11-18", "In Progress"
        ));

        // 5. TN37EF9012 - Vignesh Balan - Tata Nexon EV (Service 1)
        list.add(new ServiceRecord(
                "TN37EF9012", "Electric SUV", "Tata", "Nexon EV Max", "2024",
                "Vignesh Balan", "9789123456", "vignesh.balan@outlook.com", "78, Race Course Road, Coimbatore",
                "S001", "2025-12-05", "First EV Scheduled Checkup", "BMS firmware update and battery coolant check",
                "BMS Coolant Top-up, High-Voltage Wiring Inspection", "2400.00", "Venkatesh R",
                "2026-06-05", "Completed"
        ));

        // 6. TN37EF9012 - Vignesh Balan - Tata Nexon EV (Service 2)
        list.add(new ServiceRecord(
                "TN37EF9012", "Electric SUV", "Tata", "Nexon EV Max", "2024",
                "Vignesh Balan", "9789123456", "vignesh.balan@outlook.com", "78, Race Course Road, Coimbatore",
                "S002", "2026-06-02", "Tire Rotation & Suspension Lubrication", "Mild thud sound over speed bumps",
                "Suspension Bush Lubricant, Anti-Rust Underbody Coating", "4600.00", "Venkatesh R",
                "2026-12-02", "Pending"
        ));

        // 7. TN39GH3456 - Mohammed Farooq - Toyota Fortuner (Service 1)
        list.add(new ServiceRecord(
                "TN39GH3456", "SUV 4x4", "Toyota", "Fortuner 2.8 4x4 AT", "2021",
                "Mohammed Farooq", "9894056789", "farooq.transports@gmail.com", "23, Tiruppur Main Road, Tiruppur",
                "S001", "2025-09-12", "Heavy Duty Service", "Clutch pedal hard and transmission check",
                "Clutch Plate Assembly, Release Bearing, Gear Oil", "24500.00", "Murugan Vel",
                "2026-03-12", "Completed"
        ));

        // 8. TN39GH3456 - Mohammed Farooq - Toyota Fortuner (Service 2)
        list.add(new ServiceRecord(
                "TN39GH3456", "SUV 4x4", "Toyota", "Fortuner 2.8 4x4 AT", "2021",
                "Mohammed Farooq", "9894056789", "farooq.transports@gmail.com", "23, Tiruppur Main Road, Tiruppur",
                "S002", "2026-03-25", "Brake Disc Skimming & General Check", "Brake pedal pulsation during sudden braking",
                "Front Disc Rotors Skimmed, Heavy-Duty Brake Pads", "7800.00", "Murugan Vel",
                "2026-09-25", "Delivered"
        ));

        // 9. TN40JK7890 - Deepa Ramesh - Yamaha R15 (Service 1)
        list.add(new ServiceRecord(
                "TN40JK7890", "Motorcycle", "Yamaha", "YZF R15 V4", "2023",
                "Deepa Ramesh", "9751098765", "deepa.ramesh@gmail.com", "15, Mettupalayam Road, Pollachi",
                "S001", "2025-08-14", "General Bike Service", "Chain slackness and engine oil replacement",
                "Yamalube 10W40 Oil, Chain Sprocket Lube, Spark Plug", "1850.00", "Senthil Nathan",
                "2026-02-14", "Completed"
        ));

        // 10. TN40JK7890 - Deepa Ramesh - Yamaha R15 (Service 2)
        list.add(new ServiceRecord(
                "TN40JK7890", "Motorcycle", "Yamaha", "YZF R15 V4", "2023",
                "Deepa Ramesh", "9751098765", "deepa.ramesh@gmail.com", "15, Mettupalayam Road, Pollachi",
                "S002", "2026-02-28", "Chain Sprocket Kit Replacement", "Excessive chain noise and gear slipping",
                "Complete Brass Chain Sprocket Kit, Rear Brake Shoe", "3400.00", "Karthik Subramanian",
                "2026-08-28", "Completed"
        ));

        return list;
    }

    public static int loadSampleData(VehicleServiceDAO dao) throws IOException {
        List<ServiceRecord> samples = getSampleRecords();
        int count = 0;
        for (ServiceRecord record : samples) {
            dao.saveRecord(record);
            count++;
        }
        return count;
    }
}
