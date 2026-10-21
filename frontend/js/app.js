/**
 * Global App & API Utility
 */
const API_BASE = '/api';

const App = {
  // Show Toast Notification
  showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';

    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  // Generic Fetch API
  async apiRequest(endpoint, options = {}) {
    try {
      const res = await fetch(`${API_BASE}${endpoint}`, {
        headers: {
          'Content-Type': 'application/json',
          ...(options.headers || {})
        },
        ...options
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.message || `Request failed with status ${res.status}`);
      }
      return data;
    } catch (err) {
      console.error(`API Error [${endpoint}]:`, err);
      throw err;
    }
  },

  // Check HBase Connection Status
  async checkHBaseStatus() {
    const statusElem = document.getElementById('hbase-status-badge');
    if (!statusElem) return;

    try {
      const res = await this.apiRequest('/status');
      if (res.status === 'connected') {
        statusElem.innerHTML = `<span class="status-dot"></span> HBase Connected`;
        statusElem.style.background = 'rgba(16, 185, 129, 0.15)';
        statusElem.style.color = '#34d399';
      } else {
        statusElem.innerHTML = `<span class="status-dot"></span> HBase Disconnected`;
        statusElem.style.background = 'rgba(239, 68, 68, 0.15)';
        statusElem.style.color = '#f87171';
      }
    } catch (err) {
      statusElem.innerHTML = `<span class="status-dot"></span> HBase Offline`;
      statusElem.style.background = 'rgba(239, 68, 68, 0.15)';
      statusElem.style.color = '#f87171';
    }
  },

  // Load Sample Data Handler
  async loadSampleData() {
    try {
      const res = await this.apiRequest('/sample-data', { method: 'POST' });
      this.showToast(res.message || 'Sample data loaded into HBase!', 'success');
      setTimeout(() => window.location.reload(), 1200);
    } catch (err) {
      this.showToast('Failed to load sample data: ' + err.message, 'error');
    }
  },

  // Format currency
  formatCurrency(val) {
    const num = parseFloat(val) || 0;
    return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  },

  // Get status badge HTML
  getStatusBadge(status) {
    const s = (status || 'Pending').toLowerCase();
    if (s.includes('complete')) return `<span class="badge badge-completed">Completed</span>`;
    if (s.includes('progress')) return `<span class="badge badge-in-progress">In Progress</span>`;
    if (s.includes('deliver')) return `<span class="badge badge-delivered">Delivered</span>`;
    return `<span class="badge badge-pending">Pending</span>`;
  }
};

document.addEventListener('DOMContentLoaded', () => {
  App.checkHBaseStatus();
});
