let allRecordsData = [];

async function loadAllRecords() {
  const tbody = document.getElementById('records-tbody');
  tbody.innerHTML = `<tr><td colspan="11" style="text-align: center; color: var(--text-muted); padding: 30px;">Scanning HBase table 'vehicle_service'...</td></tr>`;

  try {
    const res = await App.apiRequest('/records');
    allRecordsData = res.data || [];
    renderRecordsTable(allRecordsData);
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="11" style="text-align: center; color: var(--danger); padding: 30px;">Error reading from HBase: ${err.message}</td></tr>`;
  }
}

function renderRecordsTable(records) {
  const tbody = document.getElementById('records-tbody');
  const countElem = document.getElementById('record-count');
  countElem.textContent = records.length;

  if (records.length === 0) {
    tbody.innerHTML = `<tr><td colspan="11" style="text-align: center; color: var(--text-muted); padding: 30px;">No matching service records found in HBase.</td></tr>`;
    return;
  }

  tbody.innerHTML = records.map(r => `
    <tr>
      <td><code style="font-size: 0.78rem; background: #f1f5f9; padding: 2px 6px; border-radius: 4px;">${r.rowKey}</code></td>
      <td>
        <a href="search.html?v=${encodeURIComponent(r.vehicleNumber)}" style="color: var(--primary); font-weight: 700; text-decoration: none;" title="Click to view full service history">
          ${r.vehicleNumber} 🔗
        </a>
      </td>
      <td>
        <strong>${r.customerName}</strong><br>
        <small style="color: var(--text-muted);">${r.phone || ''}</small>
      </td>
      <td>
        ${r.brand} ${r.model}<br>
        <small style="color: var(--text-muted);">${r.vehicleType || ''} (${r.manufacturingYear || ''})</small>
      </td>
      <td><span style="font-weight: 700; color: var(--primary);">${r.serviceId}</span></td>
      <td>${r.serviceDate}</td>
      <td>${r.serviceType}</td>
      <td><strong>${App.formatCurrency(r.serviceCost)}</strong></td>
      <td>${r.mechanicName || '--'}</td>
      <td>${App.getStatusBadge(r.serviceStatus)}</td>
      <td style="text-align: right; white-space: nowrap;">
        <button class="btn btn-secondary btn-sm" onclick="openEditModal('${r.rowKey}')" title="Edit Record">
          ✏️
        </button>
        <button class="btn btn-danger btn-sm" onclick="deleteRecord('${r.rowKey}')" title="Delete from HBase">
          🗑️
        </button>
      </td>
    </tr>
  `).join('');
}

function filterRecords() {
  const query = document.getElementById('table-filter-input').value.toLowerCase().trim();
  const statusFilter = document.getElementById('status-filter').value;

  const filtered = allRecordsData.filter(r => {
    const matchesStatus = (statusFilter === 'ALL') || (r.serviceStatus && r.serviceStatus.toLowerCase() === statusFilter.toLowerCase());
    if (!matchesStatus) return false;

    if (!query) return true;

    const searchable = [
      r.rowKey, r.vehicleNumber, r.customerName, r.phone, r.email,
      r.brand, r.model, r.serviceId, r.serviceType, r.mechanicName,
      r.problemDescription, r.partsReplaced
    ].join(' ').toLowerCase();

    return searchable.includes(query);
  });

  renderRecordsTable(filtered);
}

// Open Edit Modal
function openEditModal(rowKey) {
  const record = allRecordsData.find(r => r.rowKey === rowKey);
  if (!record) {
    App.showToast('Record not found in cache.', 'error');
    return;
  }

  document.getElementById('edit-rowKey').value = record.rowKey;
  document.getElementById('edit-vehicleNumber').value = record.vehicleNumber;
  document.getElementById('edit-serviceId').value = record.serviceId;

  document.getElementById('edit-badge-vnum').textContent = record.vehicleNumber;
  document.getElementById('edit-badge-sid').textContent = record.serviceId;
  document.getElementById('edit-badge-rkey').textContent = record.rowKey;

  document.getElementById('edit-vehicleType').value = record.vehicleType || '';
  document.getElementById('edit-brand').value = record.brand || '';
  document.getElementById('edit-model').value = record.model || '';
  document.getElementById('edit-manufacturingYear').value = record.manufacturingYear || '';

  document.getElementById('edit-customerName').value = record.customerName || '';
  document.getElementById('edit-phone').value = record.phone || '';
  document.getElementById('edit-email').value = record.email || '';
  document.getElementById('edit-address').value = record.address || '';

  document.getElementById('edit-serviceDate').value = record.serviceDate || '';
  document.getElementById('edit-serviceType').value = record.serviceType || '';
  document.getElementById('edit-serviceCost').value = record.serviceCost || '0.00';
  document.getElementById('edit-mechanicName').value = record.mechanicName || '';
  document.getElementById('edit-serviceStatus').value = record.serviceStatus || 'Pending';
  document.getElementById('edit-nextServiceDate').value = record.nextServiceDate || '';
  document.getElementById('edit-problemDescription').value = record.problemDescription || '';
  document.getElementById('edit-partsReplaced').value = record.partsReplaced || '';

  document.getElementById('edit-modal').classList.add('active');
}

function closeEditModal() {
  document.getElementById('edit-modal').classList.remove('active');
}

// Delete Record
async function deleteRecord(rowKey) {
  if (!confirm(`Are you sure you want to permanently delete record '${rowKey}' from HBase?`)) {
    return;
  }

  try {
    const res = await App.apiRequest(`/records?rowKey=${encodeURIComponent(rowKey)}`, {
      method: 'DELETE'
    });
    App.showToast(res.message || 'Record deleted from HBase.', 'success');
    await loadAllRecords();
  } catch (err) {
    App.showToast('Failed to delete from HBase: ' + err.message, 'error');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  loadAllRecords();

  document.getElementById('table-filter-input').addEventListener('input', filterRecords);
  document.getElementById('status-filter').addEventListener('change', filterRecords);

  const editForm = document.getElementById('edit-form');
  editForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const payload = {
      rowKey: document.getElementById('edit-rowKey').value,
      vehicleNumber: document.getElementById('edit-vehicleNumber').value,
      serviceId: document.getElementById('edit-serviceId').value,
      vehicleType: document.getElementById('edit-vehicleType').value,
      brand: document.getElementById('edit-brand').value.trim(),
      model: document.getElementById('edit-model').value.trim(),
      manufacturingYear: document.getElementById('edit-manufacturingYear').value.trim(),
      customerName: document.getElementById('edit-customerName').value.trim(),
      phone: document.getElementById('edit-phone').value.trim(),
      email: document.getElementById('edit-email').value.trim(),
      address: document.getElementById('edit-address').value.trim(),
      serviceDate: document.getElementById('edit-serviceDate').value,
      serviceType: document.getElementById('edit-serviceType').value.trim(),
      serviceCost: parseFloat(document.getElementById('edit-serviceCost').value || 0).toFixed(2),
      mechanicName: document.getElementById('edit-mechanicName').value.trim(),
      serviceStatus: document.getElementById('edit-serviceStatus').value,
      nextServiceDate: document.getElementById('edit-nextServiceDate').value,
      problemDescription: document.getElementById('edit-problemDescription').value.trim(),
      partsReplaced: document.getElementById('edit-partsReplaced').value.trim()
    };

    const saveBtn = document.getElementById('btn-save-edit');
    saveBtn.disabled = true;
    saveBtn.innerHTML = '<span>⏳</span> Updating in HBase...';

    try {
      const res = await App.apiRequest('/records', {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      App.showToast(res.message || 'Record updated successfully in HBase!', 'success');
      closeEditModal();
      await loadAllRecords();
    } catch (err) {
      App.showToast('Failed to update HBase: ' + err.message, 'error');
    } finally {
      saveBtn.disabled = false;
      saveBtn.innerHTML = '<span>💾</span> Save Changes (PUT)';
    }
  });
});
