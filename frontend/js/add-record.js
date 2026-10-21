document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('add-record-form');
  const serviceDateInput = document.getElementById('serviceDate');
  const nextServiceDateInput = document.getElementById('nextServiceDate');
  const btnQuickSample = document.getElementById('btn-quick-sample');

  // Set default service date to today
  const today = new Date().toISOString().split('T')[0];
  if (serviceDateInput && !serviceDateInput.value) {
    serviceDateInput.value = today;
  }

  // Quick Sample Auto-fill for live testing
  if (btnQuickSample) {
    btnQuickSample.addEventListener('click', () => {
      const rand = Math.floor(1000 + Math.random() * 9000);
      document.getElementById('vehicleNumber').value = `TN38XY${rand}`;
      document.getElementById('vehicleType').value = 'SUV';
      document.getElementById('brand').value = 'Mahindra';
      document.getElementById('model').value = 'XUV700 AX7';
      document.getElementById('manufacturingYear').value = '2023';

      document.getElementById('customerName').value = 'Kavitha Ramasamy';
      document.getElementById('phone').value = '9842154321';
      document.getElementById('email').value = 'kavitha.ramasamy@gmail.com';
      document.getElementById('address').value = '55, Avinashi Road, Peelamedu, Coimbatore';

      document.getElementById('serviceId').value = 'S001';
      document.getElementById('serviceDate').value = today;
      document.getElementById('serviceType').value = 'Periodic Maintenance';
      document.getElementById('serviceCost').value = '6250.00';
      document.getElementById('mechanicName').value = 'Rajesh Sharma';
      document.getElementById('serviceStatus').value = 'Completed';
      
      const nextDate = new Date();
      nextDate.setMonth(nextDate.getMonth() + 6);
      document.getElementById('nextServiceDate').value = nextDate.toISOString().split('T')[0];
      
      document.getElementById('problemDescription').value = 'Standard 20,000 KM Periodic Service and tire rotation';
      document.getElementById('partsReplaced').value = 'Engine Oil (6L), Oil Filter, AC Pollen Filter, Brake Cleaner';

      App.showToast('Sample details auto-filled into form!', 'info');
    });
  }

  // Handle Form Submission
  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const vehicleNumber = document.getElementById('vehicleNumber').value.trim().toUpperCase();
    const serviceId = document.getElementById('serviceId').value.trim().toUpperCase();
    const customerName = document.getElementById('customerName').value.trim();
    const serviceCost = document.getElementById('serviceCost').value.trim();

    // Client-side Validation
    if (!vehicleNumber) {
      App.showToast('Vehicle Number cannot be empty.', 'error');
      return;
    }
    if (!serviceId) {
      App.showToast('Service ID cannot be empty.', 'error');
      return;
    }
    if (!customerName) {
      App.showToast('Customer Name cannot be empty.', 'error');
      return;
    }
    if (isNaN(parseFloat(serviceCost)) || parseFloat(serviceCost) < 0) {
      App.showToast('Please enter a valid numeric service cost.', 'error');
      return;
    }

    const payload = {
      vehicleNumber: vehicleNumber,
      vehicleType: document.getElementById('vehicleType').value,
      brand: document.getElementById('brand').value.trim(),
      model: document.getElementById('model').value.trim(),
      manufacturingYear: document.getElementById('manufacturingYear').value.trim(),
      customerName: customerName,
      phone: document.getElementById('phone').value.trim(),
      email: document.getElementById('email').value.trim(),
      address: document.getElementById('address').value.trim(),
      serviceId: serviceId,
      serviceDate: document.getElementById('serviceDate').value,
      serviceType: document.getElementById('serviceType').value,
      problemDescription: document.getElementById('problemDescription').value.trim(),
      partsReplaced: document.getElementById('partsReplaced').value.trim(),
      serviceCost: parseFloat(serviceCost).toFixed(2),
      mechanicName: document.getElementById('mechanicName').value.trim(),
      nextServiceDate: document.getElementById('nextServiceDate').value,
      serviceStatus: document.getElementById('serviceStatus').value
    };

    const submitBtn = document.getElementById('btn-submit-record');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span>⏳</span> Saving to HBase...';

    try {
      const res = await App.apiRequest('/records', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      App.showToast(res.message || 'Record successfully written to HBase!', 'success');
      form.reset();
      serviceDateInput.value = today;
      setTimeout(() => {
        window.location.href = 'records.html';
      }, 1200);

    } catch (err) {
      App.showToast(err.message || 'Error communicating with HBase.', 'error');
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<span>💾</span> Save Record to HBase (PUT)';
    }
  });
});
