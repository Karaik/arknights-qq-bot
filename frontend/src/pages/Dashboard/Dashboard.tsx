import { useEffect, useState } from 'react';
import http from '../../api/http';
import './dashboard.css';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

const Dashboard = () => {
  const [backendStatus, setBackendStatus] = useState<string>('checking...');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    http
      .get<ApiResponse<string>>('/api/health')
      .then((res) => {
        setBackendStatus(res.data);
        setError(null);
      })
      .catch((err) => {
        setError(err.message);
        setBackendStatus('unavailable');
      });
  }, []);

  return (
    <section className="dashboard">
      <h2>Operations Dashboard</h2>
      <div className="card">
        <h3>Backend Connectivity</h3>
        <p>
          Status: <strong>{backendStatus}</strong>
        </p>
        {error ? <p className="error">Error: {error}</p> : <p>API health endpoint reachable.</p>}
      </div>
      <div className="card">
        <h3>Next steps</h3>
        <ul>
          <li>Display reports fetched from backend once schema is ready.</li>
          <li>Wire up authentication and role-based routing.</li>
          <li>Provide QQ group level control panels.</li>
        </ul>
      </div>
    </section>
  );
};

export default Dashboard;

