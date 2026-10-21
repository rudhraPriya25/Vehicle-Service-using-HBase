# System Architecture: Vehicle Service Record Management using HBase

## 1. System Overview

The **Vehicle Service Record Management System** is a distributed NoSQL-backed enterprise web application designed to track, store, manage, and query vehicle service records and customer maintenance histories. It leverages **Apache HBase** as the primary distributed, scalable, column-oriented database running on Hadoop/HDFS.

---

## 2. End-to-End Architectural Data Flow

```text
+-------------------------------------------------------------+
|                      End User / Browser                     |
|            (HTML5 / CSS3 Modern Dashboard / JS)             |
+-------------------------------------------------------------+
                              |
                              | HTTP / REST (JSON)
                              v
+-------------------------------------------------------------+
|                     Java Application Server                 |
|   (SimpleHttpServer / REST API Endpoints / DAO Controller)  |
+-------------------------------------------------------------+
                              |
                              | HBase Java Client API
                              v
+-------------------------------------------------------------+
|                   Apache ZooKeeper Quorum                   |
|         (Port 2181 - Cluster Coordination & Metadata)        |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                     Apache HBase Master                     |
|           (Port 16000 / Web UI: 16010 / DDL Admin)          |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                  HBase HRegionServer(s)                     |
|          (Port 16020 - Put, Get, Scan, Delete DML)          |
|    MemStore (RAM)  -->  BlockCache  -->  HFiles (Disk)      |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                     Hadoop File System                      |
|                  (HDFS / Local File System)                 |
+-------------------------------------------------------------+
```

---

## 3. Detailed Component Breakdown

### A. Web Frontend (UI Layer)
- **Technology:** HTML5, CSS3 (Responsive Design System), Vanilla JavaScript.
- **Role:** Provides user-friendly interfaces for Dashboard analytics, Service Registration forms, Instant Vehicle History search, and interactive Records Management (Edit & Delete).
- **Communication:** Sends asynchronous HTTP REST requests (`GET`, `POST`, `PUT`, `DELETE`) with JSON payloads to the backend Java server.

### B. Java Application Server & REST API Layer
- **Technology:** Java 17+, `com.sun.net.httpserver`, custom `JsonUtil`.
- **Role:** Handles incoming HTTP requests, performs server-side field validation, maps JSON entities to Java model objects (`ServiceRecord`), and coordinates with the DAO layer.
- **Key Endpoints:**
  - `GET /api/status`: HBase connection health check.
  - `GET /api/dashboard`: Summary statistics and recent transactions.
  - `GET /api/records`: Full table scan or single record retrieval.
  - `POST /api/records`: Add new vehicle and service record (HBase Put).
  - `PUT /api/records`: Update existing service record (HBase Put overwrite).
  - `DELETE /api/records`: Remove service record (HBase Delete).
  - `GET /api/history`: Vehicle service history via prefix scan.
  - `POST /api/sample-data`: Batch insertion of realistic sample datasets.

### C. HBase DAO & Client API Layer
- **Technology:** `org.apache.hadoop.hbase.client.*` (`Connection`, `Admin`, `Table`, `Put`, `Get`, `Scan`, `Delete`).
- **Role:** Encapsulates all HBase interactions:
  - Establishes connection via ZooKeeper (`127.0.0.1:2181`).
  - DDL Operations: Creates `vehicle_service` table with column families `vehicle`, `customer`, `service`.
  - DML Operations: Performs single-row Gets, batch Scans, Range/Prefix Scans, and column mutations.

### D. Apache HBase (NoSQL Database Layer)
- **Technology:** Apache HBase 2.6.7.
- **Role:** Distributed, versioned, column-oriented database modelled after Google Bigtable.
  - **HMaster:** Manages schema changes, table creation, region assignment, and failover.
  - **HRegionServer:** Serves read and write requests for regions. Writes new puts to WAL (Write-Ahead Log) and in-memory `MemStore` before flushing to persistent `HFiles`.
  - **ZooKeeper:** Manages master election, cluster state, and client routing to region servers.

---

## 4. Key HBase Concepts Explained

### 1. Why Apache HBase instead of Relational Databases (MySQL/Oracle)?
- **Massive Scalability:** Relational databases hit vertical scaling limits when managing millions or billions of vehicle IoT logs, service records, and sensor events. HBase scales horizontally across commodity clusters.
- **Sparse & Flexible Schema:** Vehicle service details often vary widely (e.g. Electric Vehicle BMS checks vs Heavy Truck clutch repairs). HBase does not store empty/null columns on disk.
- **High Write Throughput:** HBase writes directly to an in-memory `MemStore` and append-only WAL, delivering sub-millisecond write speeds.

### 2. Why Row Key Design is Critical in HBase
HBase has **only one index: the primary Row Key**. All data in HBase is stored in sorted lexicographical order by Row Key.
- Our Row Key: `vehicleNumber_serviceId` (e.g. `TN38AB1234_S001`).
- **Advantage:** By placing `vehicleNumber` as the prefix, all service events for any specific vehicle are physically stored contiguously on the same region and block.
- **Fast History Scan:** When querying the service history for `TN38AB1234`, HBase performs a localized prefix scan from `TN38AB1234_` to `TN38AB1234_\uffff`, skipping the rest of the database with zero full-table scan overhead!

### 3. Column Families
In HBase, column families are physical storage units. Columns belonging to the same column family are stored together in dedicated `StoreFiles` (HFiles):
- `vehicle`: Vehicle metadata (Vehicle Number, Type, Brand, Model, Year).
- `customer`: Customer profile (Name, Phone, Email, Address).
- `service`: Transactional service data (Service ID, Date, Cost, Parts, Mechanic, Status).

---

## 5. CRUD Operation Mapping in HBase

| Operation | SQL Equivalent | HBase Java API | Description |
| :--- | :--- | :--- | :--- |
| **Create Table** | `CREATE TABLE ...` | `Admin.createTable()` | Creates `vehicle_service` with CFs (`vehicle`, `customer`, `service`). |
| **Insert Record** | `INSERT INTO ...` | `Table.put(Put)` | Writes cell values to specified column families with row key. |
| **Read Record** | `SELECT * WHERE id=?` | `Table.get(Get)` | Retrieves a single row by its exact Row Key. |
| **Scan Table** | `SELECT * FROM ...` | `Table.getScanner(Scan)` | Scans multiple rows sequentially. |
| **Prefix Scan** | `SELECT * WHERE vNum=?` | `Scan.setFilter(PrefixFilter)` | Fast retrieval of all services for a specific vehicle. |
| **Update Record** | `UPDATE ... WHERE id=?` | `Table.put(Put)` | Overwrites column qualifier cells under the same Row Key. |
| **Delete Record** | `DELETE WHERE id=?` | `Table.delete(Delete)` | Appends a tombstone marker to delete the row/cell in HBase. |
