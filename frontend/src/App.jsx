import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';
import { Link } from "react-router-dom";

function App() {
    const [stats, setStats] = useState({
        total: 0,
        pending: 0,
        directorApproved: 0,
        approved: 0,
        rejected: 0,
        userRole: '',
        userName: ''
    });
    const [recentApplications, setRecentApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [refreshing, setRefreshing] = useState(false);


    const fetchData = async () => {
        try {
            setRefreshing(true);
            const [statsRes, recentRes] = await Promise.all([
                axios.get('/api/stats'),
                axios.get('/api/recent-applications')
            ]);
            setStats(statsRes.data);
            setRecentApplications(recentRes.data);
            setError(null);
        } catch (err) {
            console.error('Error fetching data:', err);
            if (err.response?.status === 401) {
                setError('Session expired. Please login again.');
                window.location.href = '/login';
            } else {
                setError('Failed to load dashboard data. Please make sure you are logged in.');
            }
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => {
        fetchData();

        // Refresh data every 30 seconds
        const interval = setInterval(fetchData, 30000);
        return () => clearInterval(interval);
    }, []);

    if (loading) {
        return (
            <div className="loading-container">
                <div className="spinner"></div>
                <p>Loading dashboard...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="error-container">
                <h2>Error</h2>
                <p>{error}</p>
                <a href="/dashboard" className="btn">Return to Dashboard</a>
            </div>
        );
    }

    return (
        <div className="App">
            <nav className="navbar">
                <Link to="localhost:8080/dashboard">Thymeleaf Dashboard</Link>
                <a href="/react-dashboard" className="active">Dashboard</a>
                <a href="/">Home</a>
                <a href="/logout" style={{ float: 'right' }}>Logout</a>
            </nav>

            <div className="container">
                <div className="welcome-card">
                    <h1> React Analytics Dashboard</h1>
                    <p>Welcome back, <strong>{stats.userName}</strong>! (Role: <span className="role-badge">{stats.userRole}</span>)</p>
                    <p>Real-time statistics about PhD applications</p>
                </div>

                {/* Stats Grid */}
                <div className="stats-grid">
                    <div className="stat-card total">
                        <div className="stat-number">{stats.total}</div>
                        <div className="stat-label">Total Applications</div>
                    </div>
                    <div className="stat-card pending">
                        <div className="stat-number">{stats.pending}</div>
                        <div className="stat-label">Pending Review</div>
                    </div>
                    <div className="stat-card director-approved">
                        <div className="stat-number">{stats.directorApproved}</div>
                        <div className="stat-label">Director Approved</div>
                    </div>
                    <div className="stat-card approved">
                        <div className="stat-number">{stats.approved}</div>
                        <div className="stat-label">Final Approved</div>
                    </div>
                    <div className="stat-card rejected">
                        <div className="stat-number">{stats.rejected}</div>
                        <div className="stat-label">Rejected</div>
                    </div>
                </div>

                {/* Recent Applications Table */}
                <div className="card">
                    <div className="card-header">
                        <h2>📋 Recent Applications</h2>
                        <button onClick={fetchData} className="btn-refresh" disabled={refreshing}>
                            {refreshing ? 'Refreshing...' : '⟳ Refresh'}
                        </button>
                    </div>

                    {recentApplications.length === 0 ? (
                        <p className="empty-message">No applications found.</p>
                    ) : (
                        <div className="table-responsive">
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Student</th>
                                    <th>Thesis Subject</th>
                                    <th>Director</th>
                                    <th>Status</th>
                                    <th>Date</th>
                                </tr>
                                </thead>
                                <tbody>
                                {recentApplications.map(app => (
                                    <tr key={app.id}>
                                        <td>{app.id}</td>
                                        <td>{app.studentName}</td>
                                        <td>{app.thesisSubject}</td>
                                        <td>{app.directorName}</td>
                                        <td>
                                                <span className={`status status-${app.status}`}>
                                                    {app.status}
                                                </span>
                                        </td>
                                        <td>{new Date(app.submissionDate).toLocaleDateString()}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>


            </div>
        </div>
    );
}

export default App;