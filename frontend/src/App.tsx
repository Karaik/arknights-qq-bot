import Dashboard from './pages/Dashboard/Dashboard';
import Login from './pages/Login/Login';
import './App.css';

function App() {
  return (
    <main className="app-shell">
      <header>
        <h1>Arknights Unified Control Panel</h1>
        <p>Shared workspace for backend, frontend, and QQ bot operators.</p>
      </header>

      <div className="app-content">
        <Login />
        <Dashboard />
      </div>
    </main>
  );
}

export default App;
