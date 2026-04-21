import React, { useState, useEffect } from 'react';
import api from './services/api';

const styles = {
  container: { minHeight: '100vh', backgroundColor: '#f5f5f5' },
  header: { backgroundColor: '#2c3e50', color: 'white', padding: '20px', textAlign: 'center' },
  headerTitle: { fontSize: '28px', fontWeight: 'bold', margin: 0 },
  headerSubtitle: { fontSize: '14px', marginTop: '5px', opacity: 0.8 },
  nav: { backgroundColor: '#34495e', display: 'flex', justifyContent: 'center', padding: '10px' },
  navButton: { backgroundColor: 'transparent', border: 'none', color: 'white', padding: '10px 20px', margin: '0 5px', cursor: 'pointer', fontSize: '14px', borderRadius: '4px' },
  navButtonActive: { backgroundColor: '#3498db' },
  content: { padding: '20px', maxWidth: '1200px', margin: '0 auto' },
  card: { backgroundColor: 'white', borderRadius: '8px', padding: '20px', marginBottom: '20px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' },
  cardTitle: { fontSize: '20px', fontWeight: 'bold', marginBottom: '15px', color: '#2c3e50' },
  formGroup: { marginBottom: '15px' },
  label: { display: 'block', marginBottom: '5px', fontWeight: '500', color: '#555' },
  input: { width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #ddd', fontSize: '14px' },
  button: { backgroundColor: '#3498db', color: 'white', border: 'none', padding: '12px 24px', borderRadius: '4px', cursor: 'pointer', fontSize: '14px', marginRight: '10px' },
  buttonSecondary: { backgroundColor: '#95a5a6' },
  buttonDanger: { backgroundColor: '#e74c3c' },
  table: { width: '100%', borderCollapse: 'collapse' },
  th: { backgroundColor: '#34495e', color: 'white', padding: '12px', textAlign: 'left' },
  td: { padding: '12px', borderBottom: '1px solid #ddd' },
  trEven: { backgroundColor: '#f9f9f9' },
  badge: { padding: '4px 8px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold' },
  badgeSuccess: { backgroundColor: '#27ae60', color: 'white' },
  badgeDanger: { backgroundColor: '#e74c3c', color: 'white' },
  badgeWarning: { backgroundColor: '#f39c12', color: 'white' },
  error: { backgroundColor: '#fadbd8', color: '#e74c3c', padding: '12px', borderRadius: '4px', marginBottom: '15px' },
  success: { backgroundColor: '#d5f4e6', color: '#27ae60', padding: '12px', borderRadius: '4px', marginBottom: '15px' },
  statsGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '20px', marginBottom: '20px' },
  statCard: { backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', textAlign: 'center' },
  statNumber: { fontSize: '32px', fontWeight: 'bold', color: '#3498db' },
  statLabel: { fontSize: '14px', color: '#7f8c8d', marginTop: '5px' },
  empty: { textAlign: 'center', padding: '40px', color: '#7f8c8d' },
};

function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [medicines, setMedicines] = useState([]);
  const [sales, setSales] = useState([]);
  const [expiredMedicines, setExpiredMedicines] = useState([]);
  const [lowStockMedicines, setLowStockMedicines] = useState([]);
  const [revenue, setRevenue] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Form states
  const [medicineForm, setMedicineForm] = useState({
    name: '', quantity: '', price: '', expiryDate: '', description: '', manufacturer: '', lowStockThreshold: 10
  });
  const [saleForm, setSaleForm] = useState({ medicineId: '', quantity: '' });

  useEffect(() => {
    loadData();
  }, [activeTab]);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      if (activeTab === 'dashboard' || activeTab === 'medicines') {
        const meds = await api.getMedicines();
        setMedicines(meds);
        const expired = await api.getExpiredMedicines();
        setExpiredMedicines(expired);
        const lowStock = await api.getLowStockMedicines();
        setLowStockMedicines(lowStock);
      }
      if (activeTab === 'dashboard' || activeTab === 'sales') {
        const salesData = await api.getSales();
        setSales(salesData);
        try {
          const rev = await api.getTotalRevenue();
          setRevenue(rev || 0);
        } catch (e) {
          setRevenue(0);
        }
      }
    } catch (err) {
      setError(err.message);
    }
    setLoading(false);
  };

  const handleAddMedicine = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');
    try {
      await api.addMedicine({
        ...medicineForm,
        quantity: parseInt(medicineForm.quantity),
        price: parseFloat(medicineForm.price),
        lowStockThreshold: parseInt(medicineForm.lowStockThreshold) || 10
      });
      setSuccess('Medicine added successfully!');
      setMedicineForm({ name: '', quantity: '', price: '', expiryDate: '', description: '', manufacturer: '', lowStockThreshold: 10 });
      loadData();
    } catch (err) {
      setError(err.message);
    }
    setLoading(false);
  };

  const handleCreateSale = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');
    try {
      await api.createSale({
        medicineId: parseInt(saleForm.medicineId),
        quantity: parseInt(saleForm.quantity)
      });
      setSuccess('Sale recorded successfully!');
      setSaleForm({ medicineId: '', quantity: '' });
      loadData();
    } catch (err) {
      setError(err.message);
    }
    setLoading(false);
  };

  const handleDeleteMedicine = async (id) => {
    if (!window.confirm('Are you sure you want to delete this medicine?')) return;
    setLoading(true);
    try {
      await api.deleteMedicine(id);
      setSuccess('Medicine deleted successfully!');
      loadData();
    } catch (err) {
      setError(err.message);
    }
    setLoading(false);
  };

  const renderDashboard = () => (
    <div>
      <div style={styles.statsGrid}>
        <div style={styles.statCard}>
          <div style={styles.statNumber}>{medicines.length}</div>
          <div style={styles.statLabel}>Total Medicines</div>
        </div>
        <div style={styles.statCard}>
          <div style={styles.statNumber}>{sales.length}</div>
          <div style={styles.statLabel}>Total Sales</div>
        </div>
        <div style={styles.statCard}>
          <div style={{ ...styles.statNumber, color: revenue > 0 ? '#27ae60' : '#7f8c8d' }}>
            ${typeof revenue === 'number' ? revenue.toFixed(2) : '0.00'}
          </div>
          <div style={styles.statLabel}>Total Revenue</div>
        </div>
        <div style={styles.statCard}>
          <div style={{ ...styles.statNumber, color: '#e74c3c' }}>{expiredMedicines.length}</div>
          <div style={styles.statLabel}>Expired</div>
        </div>
        <div style={styles.statCard}>
          <div style={{ ...styles.statNumber, color: '#f39c12' }}>{lowStockMedicines.length}</div>
          <div style={styles.statLabel}>Low Stock</div>
        </div>
      </div>

      {lowStockMedicines.length > 0 && (
        <div style={styles.card}>
          <div style={styles.cardTitle}>⚠️ Low Stock Alert</div>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Name</th>
                <th style={styles.th}>Quantity</th>
                <th style={styles.th}>Threshold</th>
              </tr>
            </thead>
            <tbody>
              {lowStockMedicines.map((med, idx) => (
                <tr key={med.id} style={idx % 2 === 0 ? styles.trEven : {}}>
                  <td style={styles.td}>{med.name}</td>
                  <td style={styles.td}>
                    <span style={{ ...styles.badge, ...styles.badgeWarning }}>{med.quantity}</span>
                  </td>
                  <td style={styles.td}>{med.lowStockThreshold}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {expiredMedicines.length > 0 && (
        <div style={styles.card}>
          <div style={styles.cardTitle}>🚫 Expired Medicines</div>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Name</th>
                <th style={styles.th}>Expiry Date</th>
                <th style={styles.th}>Quantity</th>
              </tr>
            </thead>
            <tbody>
              {expiredMedicines.map((med, idx) => (
                <tr key={med.id} style={idx % 2 === 0 ? styles.trEven : {}}>
                  <td style={styles.td}>{med.name}</td>
                  <td style={styles.td}>{med.expiryDate}</td>
                  <td style={styles.td}>
                    <span style={{ ...styles.badge, ...styles.badgeDanger }}>{med.quantity}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );

  const renderMedicines = () => (
    <div>
      <div style={styles.card}>
        <div style={styles.cardTitle}>➕ Add New Medicine</div>
        {error && <div style={styles.error}>{error}</div>}
        {success && <div style={styles.success}>{success}</div>}
        <form onSubmit={handleAddMedicine}>
          <div style={styles.formGroup}>
            <label style={styles.label}>Medicine Name *</label>
            <input
              style={styles.input}
              value={medicineForm.name}
              onChange={(e) => setMedicineForm({ ...medicineForm, name: e.target.value })}
              required
            />
          </div>
          <div style={{ ...styles.formGroup, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div>
              <label style={styles.label}>Quantity *</label>
              <input
                style={styles.input}
                type="number"
                min="1"
                value={medicineForm.quantity}
                onChange={(e) => setMedicineForm({ ...medicineForm, quantity: e.target.value })}
                required
              />
            </div>
            <div>
              <label style={styles.label}>Price *</label>
              <input
                style={styles.input}
                type="number"
                min="0.01"
                step="0.01"
                value={medicineForm.price}
                onChange={(e) => setMedicineForm({ ...medicineForm, price: e.target.value })}
                required
              />
            </div>
          </div>
          <div style={{ ...styles.formGroup, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div>
              <label style={styles.label}>Expiry Date *</label>
              <input
                style={styles.input}
                type="date"
                value={medicineForm.expiryDate}
                onChange={(e) => setMedicineForm({ ...medicineForm, expiryDate: e.target.value })}
                required
              />
            </div>
            <div>
              <label style={styles.label}>Low Stock Threshold</label>
              <input
                style={styles.input}
                type="number"
                min="1"
                value={medicineForm.lowStockThreshold}
                onChange={(e) => setMedicineForm({ ...medicineForm, lowStockThreshold: e.target.value })}
              />
            </div>
          </div>
          <div style={{ ...styles.formGroup, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div>
              <label style={styles.label}>Manufacturer</label>
              <input
                style={styles.input}
                value={medicineForm.manufacturer}
                onChange={(e) => setMedicineForm({ ...medicineForm, manufacturer: e.target.value })}
              />
            </div>
            <div>
              <label style={styles.label}>Description</label>
              <input
                style={styles.input}
                value={medicineForm.description}
                onChange={(e) => setMedicineForm({ ...medicineForm, description: e.target.value })}
              />
            </div>
          </div>
          <button style={styles.button} type="submit" disabled={loading}>
            {loading ? 'Adding...' : 'Add Medicine'}
          </button>
        </form>
      </div>

      <div style={styles.card}>
        <div style={styles.cardTitle}>📦 All Medicines ({medicines.length})</div>
        {medicines.length === 0 ? (
          <div style={styles.empty}>No medicines in inventory. Add some above.</div>
        ) : (
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>ID</th>
                <th style={styles.th}>Name</th>
                <th style={styles.th}>Qty</th>
                <th style={styles.th}>Price</th>
                <th style={styles.th}>Expiry</th>
                <th style={styles.th}>Status</th>
                <th style={styles.th}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {medicines.map((med, idx) => (
                <tr key={med.id} style={idx % 2 === 0 ? styles.trEven : {}}>
                  <td style={styles.td}>{med.id}</td>
                  <td style={styles.td}>{med.name}</td>
                  <td style={styles.td}>{med.quantity}</td>
                  <td style={styles.td}>${parseFloat(med.price).toFixed(2)}</td>
                  <td style={styles.td}>{med.expiryDate}</td>
                  <td style={styles.td}>
                    {med.isExpired ? (
                      <span style={{ ...styles.badge, ...styles.badgeDanger }}>Expired</span>
                    ) : med.isLowStock ? (
                      <span style={{ ...styles.badge, ...styles.badgeWarning }}>Low Stock</span>
                    ) : (
                      <span style={{ ...styles.badge, ...styles.badgeSuccess }}>OK</span>
                    )}
                  </td>
                  <td style={styles.td}>
                    <button
                      style={{ ...styles.button, padding: '6px 12px', fontSize: '12px', backgroundColor: '#e74c3c' }}
                      onClick={() => handleDeleteMedicine(med.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );

  const renderSales = () => {
    const availableMedicines = medicines.filter(m => !m.isExpired && m.quantity > 0);
    
    return (
      <div>
        <div style={styles.card}>
          <div style={styles.cardTitle}>💰 New Sale (POS)</div>
          {error && <div style={styles.error}>{error}</div>}
          {success && <div style={styles.success}>{success}</div>}
          <form onSubmit={handleCreateSale}>
            <div style={{ ...styles.formGroup, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={styles.label}>Select Medicine *</label>
                <select
                  style={styles.input}
                  value={saleForm.medicineId}
                  onChange={(e) => setSaleForm({ ...saleForm, medicineId: e.target.value })}
                  required
                >
                  <option value="">Select a medicine</option>
                  {availableMedicines.map(med => (
                    <option key={med.id} value={med.id}>
                      {med.name} - ${parseFloat(med.price).toFixed(2)} (Stock: {med.quantity})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label style={styles.label}>Quantity *</label>
                <input
                  style={styles.input}
                  type="number"
                  min="1"
                  value={saleForm.quantity}
                  onChange={(e) => setSaleForm({ ...saleForm, quantity: e.target.value })}
                  required
                />
              </div>
            </div>
            <button style={styles.button} type="submit" disabled={loading}>
              {loading ? 'Processing...' : 'Process Sale'}
            </button>
          </form>
        </div>

        <div style={styles.card}>
          <div style={styles.cardTitle}>📊 Sales History ({sales.length})</div>
          <div style={{ marginBottom: '15px', fontSize: '18px', fontWeight: 'bold', color: '#27ae60' }}>
            Total Revenue: ${typeof revenue === 'number' ? revenue.toFixed(2) : '0.00'}
          </div>
          {sales.length === 0 ? (
            <div style={styles.empty}>No sales yet.</div>
          ) : (
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>ID</th>
                  <th style={styles.th}>Medicine</th>
                  <th style={styles.th}>Qty</th>
                  <th style={styles.th}>Unit Price</th>
                  <th style={styles.th}>Total</th>
                  <th style={styles.th}>Date</th>
                </tr>
              </thead>
              <tbody>
                {sales.map((sale, idx) => (
                  <tr key={sale.id} style={idx % 2 === 0 ? styles.trEven : {}}>
                    <td style={styles.td}>{sale.id}</td>
                    <td style={styles.td}>{sale.medicine?.name || 'Unknown'}</td>
                    <td style={styles.td}>{sale.quantity}</td>
                    <td style={styles.td}>${parseFloat(sale.unitPrice).toFixed(2)}</td>
                    <td style={styles.td}>${parseFloat(sale.totalPrice).toFixed(2)}</td>
                    <td style={styles.td}>{sale.createdAt ? new Date(sale.createdAt).toLocaleString() : 'N/A'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    );
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h1 style={styles.headerTitle}>🏥 Pharmacy Management System</h1>
        <p style={styles.headerSubtitle}>Production-Ready POS & Inventory Management</p>
      </header>

      <nav style={styles.nav}>
        <button
          style={{ ...styles.navButton, ...(activeTab === 'dashboard' ? styles.navButtonActive : {}) }}
          onClick={() => setActiveTab('dashboard')}
        >
          📊 Dashboard
        </button>
        <button
          style={{ ...styles.navButton, ...(activeTab === 'medicines' ? styles.navButtonActive : {}) }}
          onClick={() => setActiveTab('medicines')}
        >
          💊 Medicines
        </button>
        <button
          style={{ ...styles.navButton, ...(activeTab === 'sales' ? styles.navButtonActive : {}) }}
          onClick={() => setActiveTab('sales')}
        >
          💵 Sales
        </button>
      </nav>

      <main style={styles.content}>
        {loading && <div style={styles.card}>Loading...</div>}
        {activeTab === 'dashboard' && renderDashboard()}
        {activeTab === 'medicines' && renderMedicines()}
        {activeTab === 'sales' && renderSales()}
      </main>
    </div>
  );
}

export default App;