# Vehicle Service Record Management using HBase

A full-stack, distributed NoSQL academic mini-project demonstrating how **Apache HBase** can be used as the primary database for managing vehicle service transactions, customer records, and vehicle maintenance histories.

---

## 1. Project Objective

The objective of this project is to build a reliable, beginner-friendly **Vehicle Service Record Management System** powered by **Apache HBase**.

The system enables a service center team to:
1. **Register New Vehicles & Customer Profiles.**
2. **Record Detailed Service Job Cards** (Service Type, Problem, Parts Replaced, Cost, Mechanic, Status).
3. **Search Vehicle History by Vehicle Number** with chronological timeline presentation.
4. **Scan & View All Historical Records** in a filterable data table.
5. **Update Existing Service Records** (HBase Put overwrite).
6. **Delete Service Records** (HBase Delete with tombstone handling).
7. **Monitor Real-Time Service Center Analytics** on an interactive Dashboard.

---

## 2. Technology Stack

- **Primary Database:** Apache HBase 2.6.7 (Distributed Column-Oriented NoSQL)
- **Cluster Coordination:** Apache ZooKeeper (Port 2181)
- **File System / Storage:** Hadoop / Local HFiles (`C:\hbase-2.6.7-hadoop3\data`)
- **Backend API & Application Server:** Java 17+ (Native lightweight HTTP Server, zero heavy framework overhead)
- **HBase Client API:** `org.apache.hadoop.hbase.client.*`
- **Frontend User Interface:** HTML5, CSS3 (Modern Responsive UI), Vanilla JavaScript

---

## 3. HBase Schema & Architecture

### A. Table Specification
- **Table Name:** `vehicle_service`
- **Row Key Design:** `vehicleNumber_serviceId` (e.g., `TN38AB1234_S001`, `TN38AB1234_S002`, `TN38CD5678_S001`)

### B. Column Families & Columns

1. **`vehicle` (Vehicle Mechanical Details)**
   - `vehicle:vehicleNumber`
   - `vehicle:vehicleType` (Car, SUV, Sedan, EV, Motorcycle, Truck)
   - `vehicle:brand` (Hyundai, Honda, Tata, Toyota, Yamaha)
   - `vehicle:model` (Creta, City, Nexon EV, Fortuner, R15)
   - `vehicle:manufacturingYear`

2. **`customer` (Owner & Contact Profile)**
   - `customer:customerName`
   - `customer:phone`
   - `customer:email`
   - `customer:address`

3. **`service` (Transactional Service Details)**
   - `service:serviceId` (S001, S002)
   - `service:serviceDate`
   - `service:serviceType`
   - `service:problemDescription`
   - `service:partsReplaced`
   - `service:serviceCost`
   - `service:mechanicName`
   - `service:nextServiceDate`
   - `service:serviceStatus` (Completed, In Progress, Pending, Delivered)

---

## 4. End-to-End Architectural Flow

```text
User / Web Browser (HTML/CSS/JS)
              ↓ HTTP / REST API (JSON)
Java Application Server (SimpleHttpServer)
              ↓ HBase Java Client API
Apache ZooKeeper (Port 2181)
              ↓ Cluster State & Routing
Apache HBase Master & RegionServer (Ports 16000, 16020)
              ↓ MemStore (RAM) + WAL
Hadoop / Local Disk (HFiles)
```

---

## 5. Project Directory Structure

```text
vehicle-service-hbase/
├── pom.xml                               # Maven Project Descriptor
├── README.md                             # Project Documentation & Guide
├── run.bat                               # Windows Batch Startup Script
├── run.ps1                               # PowerShell Startup Script
│
├── src/
│   ├── main/
│   │   ├── java/com/vehicleservice/
│   │   │   ├── Main.java                 # Entrypoint, Schema Init & Auto-loader
│   │   │   ├── config/
│   │   │   │   └── HBaseConfig.java      # ZooKeeper & HBase Connection Factory
│   │   │   ├── dao/
│   │   │   │   └── VehicleServiceDAO.java# DDL, Put, Get, Scan, Delete, History
│   │   │   ├── model/
│   │   │   │   └── ServiceRecord.java    # Data Model mapping to 3 Column Families
│   │   │   ├── server/
│   │   │   │   └── SimpleHttpServer.java # Lightweight REST API & Static Web Server
│   │   │   └── util/
│   │   │       ├── JsonUtil.java         # Fast JSON Serializer / Parser
│   │   │       └── SampleDataLoader.java # 10 Realistic Sample Vehicle Records
│   │   └── resources/
│   │       └── hbase-site.xml            # Classpath HBase XML Config
│   │
│   └── test/
│       └── java/com/vehicleservice/
│           └── VehicleServiceTest.java   # Automated 9-Point Direct HBase Test Suite
│
├── frontend/
│   ├── index.html                        # Dashboard Page
│   ├── add-record.html                   # Add Vehicle / Service Record Form
│   ├── search.html                       # Search Vehicle History by Number
│   ├── records.html                      # View All Records, Live Filter, Edit, Delete
│   ├── css/
│   │   └── style.css                     # Modern, Clean Responsive Styles
│   └── js/
│       ├── app.js                        # Global API Client, Toast Alerts, HBase Poller
│       ├── add-record.js                 # Form Validation & Submission
│       ├── search.js                     # Search & Timeline History Rendering
│       └── records.js                    # Records Table, Modal & CRUD Handlers
│
├── hbase/
│   ├── create_table.hbase                # HBase Shell script to create table
│   ├── sample_data.hbase                 # HBase Shell PUT commands (10 records)
│   └── delete_table.hbase                # HBase Shell script to disable and drop table
│
└── docs/
    ├── architecture.md                   # Full System Architecture Guide
    ├── database-schema.md                # Comprehensive HBase Schema Reference
    └── viva-questions.md                 # 27+ Beginner-Friendly Viva Q&A
```

---

## 6. How to Start and Run the Project

### Prerequisites
- Java 17+ installed (Adoptium OpenJDK 17 or Oracle JDK)
- HBase 2.6.7 installed at `C:\hbase-2.6.7-hadoop3`

### Step 1: Start HBase Master
In a PowerShell terminal, start the standalone HBase Master:
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
$env:HADOOP_HOME = "C:\hadoop"
$env:PATH = "$env:JAVA_HOME\bin;$env:HADOOP_HOME\bin;C:\hbase-2.6.7-hadoop3\bin;$env:PATH"

& "C:\hbase-2.6.7-hadoop3\bin\hbase.cmd" master start
```

### Step 2: Run the Application
In a new PowerShell window, run the application startup script:
```powershell
.\run.ps1
```
*(Or simply double click `run.bat`)*

The server will initialize the HBase schema, verify connection, auto-load 10 realistic sample records, and host the web application at:
👉 **`http://localhost:8080`**

---

## 7. How to Test & Verify via HBase Shell

You can interact with HBase directly using the HBase shell:

```powershell
& "C:\hbase-2.6.7-hadoop3\bin\hbase.cmd" shell
```

### 1. List all tables
```hbase
list
```

### 2. Describe table structure
```hbase
describe 'vehicle_service'
```

### 3. Scan all records
```hbase
scan 'vehicle_service'
```

### 4. Get a specific record by Row Key
```hbase
get 'vehicle_service', 'TN38AB1234_S001'
```

### 5. Insert / Update a record
```hbase
put 'vehicle_service', 'TN38AB1234_S001', 'service:serviceCost', '6200.00'
```

### 6. Delete a record
```hbase
deleteall 'vehicle_service', 'TN38AB1234_S001'
```

---

## 8. Automated Test Suite Execution

Run the complete 9-point direct HBase integration test suite:
```powershell
$javac = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin\javac.exe"
$java = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin\java.exe"
$cp = "target\classes;C:\hbase-2.6.7-hadoop3\lib\shaded-clients\hbase-shaded-client-2.6.7-hadoop3.jar;C:\hbase-2.6.7-hadoop3\lib\client-facing-thirdparty\*"
$opens = "--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/sun.net.dns=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED"

& $java $opens.Split(" ") -cp $cp "-Dhadoop.home.dir=C:\hadoop" com.vehicleservice.VehicleServiceTest
```
**Test Results:** 9 Passed, 0 Failed.

---

## 9. Web Application Features & Screens

1. **Dashboard (`/index.html`):** Displays real-time counts of Total Vehicles, Total Service Records, Total Revenue, Active Tasks, Recent Services list, and HBase Live Status.
2. **Add Service Record (`/add-record.html`):** 3-Section Form mapping directly into Column Families `vehicle`, `customer`, and `service` with validation and "Quick Sample Fill".
3. **Search & History (`/search.html`):** Search any Vehicle Number (e.g. `TN38AB1234`) to view comprehensive Vehicle Profile, Customer Info, and Chronological Service Timeline.
4. **View All Records (`/records.html`):** Filterable data table displaying all HBase records with interactive Edit Modal (HBase PUT update) and Delete Confirmation (HBase DELETE).
