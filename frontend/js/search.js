async function performSearch(vehicleNumber) {
  if (!vehicleNumber || !vehicleNumber.trim()) {
    App.showToast('Please enter a vehicle number.', 'error');
    return;
  }

  const vNum = vehicleNumber.trim().toUpperCase();
  document.getElementById('search-vehicle-input').value = vNum;

  const btnSearch = document.getElementById('btn-search');
  btnSearch.disabled = true;
  btnSearch.innerHTML = '<span>⏳</span> Searching HBase...';

  const resultsWrapper = document.getElementById('results-wrapper');
  const noResultsBox = document.getElementById('no-results-box');

  resultsWrapper.style.display = 'none';
  noResultsBox.style.display = 'none';

  try {
    const res = await App.apiRequest(`/history?vehicleNumber=${encodeURIComponent(vNum)}`);
    const data = res.data;

    // Populate Vehicle & Customer Profile
    document.getElementById('res-vnum').textContent = data.vehicleNumber || vNum;
    document.getElementById('res-vtype').textContent = data.vehicleType || 'N/A';
    document.getElementById('res-brand').textContent = data.brand || 'N/A';
    document.getElementById('res-model').textContent = data.model || 'N/A';
    document.getElementById('res-year').textContent = data.manufacturingYear || 'N/A';
    document.getElementById('res-total-services').textContent = `${data.totalServices} Service(s)`;

    document.getElementById('res-cname').textContent = data.customerName || 'N/A';
    document.getElementById('res-phone').textContent = data.phone || 'N/A';
    document.getElementById('res-email').textContent = data.email || 'N/A';
    document.getElementById('res-address').textContent = data.address || 'N/A';

    // Render Timeline
    const timelineContainer = document.getElementById('timeline-container');
    timelineContainer.innerHTML = '';

    let totalCost = 0;

    data.history.forEach((record, index) => {
      const cost = parseFloat(record.serviceCost) || 0;
      totalCost += cost;

      const item = document.createElement('div');
      item.className = 'timeline-item';
      item.innerHTML = `
        <div class="timeline-dot"></div>
        <div class="timeline-card">
          <div class="timeline-header">
            <div>
              <span style="font-size: 0.8rem; font-weight: 700; color: var(--primary); background: var(--primary-light); padding: 2px 8px; border-radius: 4px; margin-right: 8px;">
                ${record.serviceId}
              </span>
              <strong style="font-size: 1.05rem;">${record.serviceType}</strong>
            </div>
            <div style="display: flex; align-items: center; gap: 10px;">
              <span class="timeline-date">📅 ${record.serviceDate}</span>
              ${App.getStatusBadge(record.serviceStatus)}
            </div>
          </div>
          
          <div style="font-size: 0.88rem; color: var(--text-main); margin-top: 8px; display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px;">
            <div><strong>Mechanic:</strong> ${record.mechanicName || 'Not Assigned'}</div>
            <div><strong>Cost:</strong> <span style="font-weight: 700; color: #059669;">${App.formatCurrency(record.serviceCost)}</span></div>
            <div><strong>Next Service:</strong> ${record.nextServiceDate || 'Not specified'}</div>
          </div>

          ${record.problemDescription ? `
            <div style="margin-top: 10px; font-size: 0.85rem; background: #f8fafc; padding: 8px 12px; border-radius: 6px; border-left: 3px solid var(--primary);">
              <strong>Reported Problem:</strong> ${record.problemDescription}
            </div>
          ` : ''}

          ${record.partsReplaced ? `
            <div style="margin-top: 6px; font-size: 0.85rem; color: var(--text-muted);">
              <strong>Parts Replaced:</strong> ${record.partsReplaced}
            </div>
          ` : ''}

          <div style="margin-top: 12px; font-size: 0.75rem; color: var(--text-light); text-align: right;">
            HBase Row Key: <code>${record.rowKey}</code>
          </div>
        </div>
      `;
      timelineContainer.appendChild(item);
    });

    document.getElementById('res-summary-cost').textContent = `Total Spent: ${App.formatCurrency(totalCost)}`;
    resultsWrapper.style.display = 'block';

  } catch (err) {
    document.getElementById('no-results-msg').textContent = `No service records found in HBase for Vehicle Number: "${vNum}".`;
    noResultsBox.style.display = 'block';
  } finally {
    btnSearch.disabled = false;
    btnSearch.innerHTML = '<span>🔍</span> Search History (SCAN)';
  }
}

function quickSearch(vehicleNum) {
  performSearch(vehicleNum);
}

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('search-form');
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const vNum = document.getElementById('search-vehicle-input').value;
    performSearch(vNum);
  });

  // Check URL query param e.g. search.html?v=TN38AB1234
  const urlParams = new URLSearchParams(window.location.search);
  const vParam = urlParams.get('v') || urlParams.get('vehicleNumber');
  if (vParam) {
    performSearch(vParam);
  }
});
