const API_BASE_URL = '/api';

export const api = {
  // Medicine endpoints
  getMedicines: async () => {
    const response = await fetch(`${API_BASE_URL}/medicines`);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to fetch medicines');
    return data.data;
  },

  getMedicineById: async (id) => {
    const response = await fetch(`${API_BASE_URL}/medicines/${id}`);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to fetch medicine');
    return data.data;
  },

  getExpiredMedicines: async () => {
    const response = await fetch(`${API_BASE_URL}/medicines/expired`);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to fetch expired medicines');
    return data.data;
  },

  getLowStockMedicines: async () => {
    const response = await fetch(`${API_BASE_URL}/medicines/low-stock`);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to fetch low stock medicines');
    return data.data;
  },

  addMedicine: async (medicine) => {
    const response = await fetch(`${API_BASE_URL}/medicines`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(medicine),
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to add medicine');
    return data.data;
  },

  updateMedicine: async (id, medicine) => {
    const response = await fetch(`${API_BASE_URL}/medicines/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(medicine),
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to update medicine');
    return data.data;
  },

  deleteMedicine: async (id) => {
    const response = await fetch(`${API_BASE_URL}/medicines/${id}`, {
      method: 'DELETE',
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to delete medicine');
    return data;
  },

  searchMedicines: async (name) => {
    const response = await fetch(`${API_BASE_URL}/medicines/search?name=${encodeURIComponent(name)}`);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to search medicines');
    return data.data;
  },

  // Sale endpoints
  getSales: async () => {
    const response = await fetch(`${API_BASE_URL}/sales`);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to fetch sales');
    return data.data;
  },

  createSale: async (sale) => {
    const response = await fetch(`${API_BASE_URL}/sales`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(sale),
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to create sale');
    return data.data;
  },

  getTotalRevenue: async () => {
    const response = await fetch(`${API_BASE_URL}/sales/revenue`);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Failed to fetch revenue');
    return data.data;
  },
};

export default api;