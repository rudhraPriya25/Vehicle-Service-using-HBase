# Database Schema: Vehicle Service Record Management using HBase

## 1. Table Specification

- **Table Name:** `vehicle_service`
- **Primary Database:** Apache HBase 2.6.7 (NoSQL Distributed Column Store)
- **Coordination:** Apache ZooKeeper (Port 2181)
- **Underlying Storage:** Hadoop HFiles

---

## 2. Row Key Design

### Structure
```
[VehicleNumber]_[ServiceID]
```

### Examples
- `TN38AB1234_S001`
- `TN38AB1234_S002`
- `TN38CD5678_S001`
- `TN37EF9012_S001`
- `TN39GH3456_S001`
- `TN40JK7890_S001`

### Design Rationale
1. **Natural Clustering:** Rows in HBase are sorted lexicographically by Row Key. All service events for any specific vehicle are naturally stored together in contiguous physical blocks.
2. **Range Scans:** Searching service history by `vehicleNumber` translates directly to an optimal prefix scan (`TN38AB1234_*`), avoiding full table scans.
3. **Uniqueness:** Concatenating `vehicleNumber` with `serviceId` guarantees uniqueness across all historical transactions.

---

## 3. Column Families & Qualifiers

The table contains three Column Families: `vehicle`, `customer`, and `service`.

### Column Family 1: `vehicle`
Stores static and mechanical specifications of the automobile.

| Column Qualifier | Data Type | Description | Example Value |
| :--- | :--- | :--- | :--- |
| `vehicle:vehicleNumber` | String | Registration plate number | `TN38AB1234` |
| `vehicle:vehicleType` | String | Category of automobile | `SUV`, `Sedan`, `EV`, `Motorcycle` |
| `vehicle:brand` | String | Vehicle manufacturer | `Hyundai`, `Tata`, `Honda`, `Toyota` |
| `vehicle:model` | String | Model variant | `Creta 1.5 SX`, `Nexon EV Max` |
| `vehicle:manufacturingYear`| String | Year of manufacture | `2023` |

### Column Family 2: `customer`
Stores owner contact and billing information.

| Column Qualifier | Data Type | Description | Example Value |
| :--- | :--- | :--- | :--- |
| `customer:customerName` | String | Full name of vehicle owner | `Arun Kumar` |
| `customer:phone` | String | 10-digit contact number | `9876543210` |
| `customer:email` | String | Customer email address | `arun.kumar@gmail.com` |
| `customer:address` | String | Residential address or city | `12, Gandhi Puram, Coimbatore` |

### Column Family 3: `service`
Stores transactional service job card information.

| Column Qualifier | Data Type | Description | Example Value |
| :--- | :--- | :--- | :--- |
| `service:serviceId` | String | Job card identifier | `S001`, `S002` |
| `service:serviceDate` | String | Date service was performed | `2025-10-15` |
| `service:serviceType` | String | Nature of service task | `Periodic Maintenance`, `Brake Overhaul` |
| `service:problemDescription` | String | Reported issue/symptoms | `Engine oil change and general inspection` |
| `service:partsReplaced` | String | Itemized list of parts replaced | `Synthetic Oil (4L), Oil Filter, Air Filter` |
| `service:serviceCost` | String | Total invoice cost (INR) | `5800.00` |
| `service:mechanicName` | String | Assigned technician name | `Rajesh Sharma` |
| `service:nextServiceDate` | String | Next scheduled service date | `2026-04-15` |
| `service:serviceStatus` | String | Current job status | `Completed`, `In Progress`, `Pending`, `Delivered` |

---

## 4. Complete Example HBase Row Representation

```
ROW: TN38AB1234_S001
 COLUMN FAMILIES & CELLS:
  vehicle:brand                     timestamp=1791434399000, value=Hyundai
  vehicle:manufacturingYear         timestamp=1791434399000, value=2023
  vehicle:model                     timestamp=1791434399000, value=Creta 1.5 SX
  vehicle:vehicleNumber             timestamp=1791434399000, value=TN38AB1234
  vehicle:vehicleType               timestamp=1791434399000, value=SUV
  customer:address                  timestamp=1791434399000, value=12, Gandhi Puram, Coimbatore
  customer:customerName             timestamp=1791434399000, value=Arun Kumar
  customer:email                    timestamp=1791434399000, value=arun.kumar@gmail.com
  customer:phone                    timestamp=1791434399000, value=9876543210
  service:mechanicName              timestamp=1791434399000, value=Rajesh Sharma
  service:nextServiceDate           timestamp=1791434399000, value=2026-04-15
  service:partsReplaced             timestamp=1791434399000, value=Synthetic Engine Oil (4L), Oil Filter, Air Filter
  service:problemDescription        timestamp=1791434399000, value=Engine oil change and general inspection
  service:serviceCost               timestamp=1791434399000, value=5800.00
  service:serviceDate               timestamp=1791434399000, value=2025-10-15
  service:serviceId                 timestamp=1791434399000, value=S001
  service:serviceStatus             timestamp=1791434399000, value=Completed
  service:serviceType               timestamp=1791434399000, value=Periodic Maintenance
```

---

## 5. DDL & DML HBase Shell Syntax

### Create Table
```hbase
create 'vehicle_service', 'vehicle', 'customer', 'service'
```

### Insert / Update Row
```hbase
put 'vehicle_service', 'TN38AB1234_S001', 'vehicle:brand', 'Hyundai'
put 'vehicle_service', 'TN38AB1234_S001', 'customer:customerName', 'Arun Kumar'
put 'vehicle_service', 'TN38AB1234_S001', 'service:serviceCost', '5800.00'
```

### Get Single Row
```hbase
get 'vehicle_service', 'TN38AB1234_S001'
```

### Scan All Rows
```hbase
scan 'vehicle_service'
```

### Delete Row
```hbase
deleteall 'vehicle_service', 'TN38AB1234_S001'
```
