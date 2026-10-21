package com.vehicleservice.model;

/**
 * ServiceRecord represents a comprehensive vehicle service transaction.
 * It maps to three HBase Column Families:
 * 1. vehicle: vehicleNumber, vehicleType, brand, model, manufacturingYear
 * 2. customer: customerName, phone, email, address
 * 3. service: serviceId, serviceDate, serviceType, problemDescription,
 *             partsReplaced, serviceCost, mechanicName, nextServiceDate, serviceStatus
 *
 * The HBase Row Key is structured as: vehicleNumber + "_" + serviceId
 * Example: "TN38AB1234_S001"
 */
public class ServiceRecord {

    // HBase Row Key
    private String rowKey;

    // CF 1: vehicle
    private String vehicleNumber;
    private String vehicleType;
    private String brand;
    private String model;
    private String manufacturingYear;

    // CF 2: customer
    private String customerName;
    private String phone;
    private String email;
    private String address;

    // CF 3: service
    private String serviceId;
    private String serviceDate;
    private String serviceType;
    private String problemDescription;
    private String partsReplaced;
    private String serviceCost;
    private String mechanicName;
    private String nextServiceDate;
    private String serviceStatus;

    // Default Constructor
    public ServiceRecord() {
    }

    // Full Constructor
    public ServiceRecord(String vehicleNumber, String vehicleType, String brand, String model, String manufacturingYear,
                         String customerName, String phone, String email, String address,
                         String serviceId, String serviceDate, String serviceType, String problemDescription,
                         String partsReplaced, String serviceCost, String mechanicName, String nextServiceDate, String serviceStatus) {
        this.vehicleNumber = (vehicleNumber != null) ? vehicleNumber.trim().toUpperCase() : "";
        this.vehicleType = (vehicleType != null) ? vehicleType.trim() : "";
        this.brand = (brand != null) ? brand.trim() : "";
        this.model = (model != null) ? model.trim() : "";
        this.manufacturingYear = (manufacturingYear != null) ? manufacturingYear.trim() : "";

        this.customerName = (customerName != null) ? customerName.trim() : "";
        this.phone = (phone != null) ? phone.trim() : "";
        this.email = (email != null) ? email.trim() : "";
        this.address = (address != null) ? address.trim() : "";

        this.serviceId = (serviceId != null) ? serviceId.trim().toUpperCase() : "";
        this.serviceDate = (serviceDate != null) ? serviceDate.trim() : "";
        this.serviceType = (serviceType != null) ? serviceType.trim() : "";
        this.problemDescription = (problemDescription != null) ? problemDescription.trim() : "";
        this.partsReplaced = (partsReplaced != null) ? partsReplaced.trim() : "";
        this.serviceCost = (serviceCost != null) ? serviceCost.trim() : "0.00";
        this.mechanicName = (mechanicName != null) ? mechanicName.trim() : "";
        this.nextServiceDate = (nextServiceDate != null) ? nextServiceDate.trim() : "";
        this.serviceStatus = (serviceStatus != null) ? serviceStatus.trim() : "Pending";

        this.rowKey = computeRowKey(this.vehicleNumber, this.serviceId);
    }

    public static String computeRowKey(String vehicleNumber, String serviceId) {
        String vNum = (vehicleNumber != null) ? vehicleNumber.trim().toUpperCase() : "";
        String sId = (serviceId != null) ? serviceId.trim().toUpperCase() : "";
        return vNum + "_" + sId;
    }

    // Getters and Setters
    public String getRowKey() {
        if (rowKey == null || rowKey.isEmpty()) {
            rowKey = computeRowKey(vehicleNumber, serviceId);
        }
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = (vehicleNumber != null) ? vehicleNumber.trim().toUpperCase() : "";
        this.rowKey = computeRowKey(this.vehicleNumber, this.serviceId);
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturingYear() {
        return manufacturingYear;
    }

    public void setManufacturingYear(String manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = (serviceId != null) ? serviceId.trim().toUpperCase() : "";
        this.rowKey = computeRowKey(this.vehicleNumber, this.serviceId);
    }

    public String getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(String serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getPartsReplaced() {
        return partsReplaced;
    }

    public void setPartsReplaced(String partsReplaced) {
        this.partsReplaced = partsReplaced;
    }

    public String getServiceCost() {
        return serviceCost;
    }

    public void setServiceCost(String serviceCost) {
        this.serviceCost = serviceCost;
    }

    public String getMechanicName() {
        return mechanicName;
    }

    public void setMechanicName(String mechanicName) {
        this.mechanicName = mechanicName;
    }

    public String getNextServiceDate() {
        return nextServiceDate;
    }

    public void setNextServiceDate(String nextServiceDate) {
        this.nextServiceDate = nextServiceDate;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    @Override
    public String toString() {
        return "ServiceRecord{" +
                "rowKey='" + getRowKey() + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", customerName='" + customerName + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", serviceDate='" + serviceDate + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", serviceCost='" + serviceCost + '\'' +
                ", mechanicName='" + mechanicName + '\'' +
                ", serviceStatus='" + serviceStatus + '\'' +
                '}';
    }
}
