import React, { useState, useEffect, useRef } from 'react';
import { getAuth, signOut } from 'firebase/auth';
import Chart from 'chart.js/auto';

function Dashboard({ user }) {
    const [activityStatus, setActivityStatus] = useState(null);
    const [error, setError] = useState('');
    const chartRef = useRef(null);
    const chartInstance = useRef(null);
    const auth = getAuth();

    useEffect(() => {
        const fetchActivityStatus = async () => {
            if (!user) return;
            try {
                // CORRECTED SYNTAX: The options object is the second argument INSIDE fetch()
                const response = await fetch('http://localhost:8082/api/activities/status');

                if (!response.ok) {
                    throw new Error(`Server error: ${response.status}`);
                }

                const data = await response.json();
                setActivityStatus(data);
                setError('');
            } catch (err) {
                console.error("Failed to fetch activity status:", err);
                setError('Could not connect to activity service.');
            }
        };

        fetchActivityStatus();
        const intervalId = setInterval(fetchActivityStatus, 5000);
        return () => clearInterval(intervalId);
    }, [user]);

    useEffect(() => {
        if (chartRef.current) {
            if (chartInstance.current) {
                chartInstance.current.destroy();
            }
            const ctx = chartRef.current.getContext('2d');
            chartInstance.current = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: ['Sitting', 'Walking', 'Standing', 'Running'],
                    datasets: [{
                        label: 'Time (Minutes)',
                        data: [240, 90, 60, 15], // Mock data
                        backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0']
                    }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }
        return () => {
            if (chartInstance.current) chartInstance.current.destroy();
        };
    }, []);

    const handleLogout = () => signOut(auth);

    const getActivityIcon = (activity) => {
        const icons = { 'WALKING': '🚶‍♂️', 'SITTING': '🧘', 'STANDING': '🧍', 'RUNNING': '🏃‍♀️', 'UNKNOWN': '❓' };
        return icons[activity] || '❓';
    };

    return (
        <div className="dashboard">
            <header className="dashboard-header">
                <div>
                    <h1>Your Wellness Dashboard</h1>
                    <p>Signed in as {user.email}</p>
                </div>
                <button onClick={handleLogout} className="button-secondary">Logout</button>
            </header>
            <main className="dashboard-grid">
                <div className="live-status-column">
                    <div className="card">
                        <h3>Current Activity</h3>
                        <div className="activity-display">
                            <div className="activity-icon">
                                {activityStatus ? getActivityIcon(activityStatus.lastDetectedActivity) : '...'}
                            </div>
                            <p className="activity-name">
                                {error ? 'Error' : (activityStatus ? activityStatus.lastDetectedActivity : 'Connecting...')}
                            </p>
                        </div>
                        <p className="timestamp">
                            Last updated: {activityStatus ? new Date(activityStatus.timestamp).toLocaleTimeString() : 'never'}
                        </p>
                    </div>
                    <div className="card">
                        <h3>💡 AI Wellness Tip</h3>
                        <p className="ai-recommendation">
                            {error ? error : (activityStatus ? activityStatus.latestRecommendation : 'Awaiting activity data...')}
                        </p>
                    </div>
                </div>
                <div className="history-column">
                    <div className="card">
                        <h3>Today's Activity Breakdown</h3>
                        <div className="chart-container">
                            <canvas ref={chartRef}></canvas>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default Dashboard;

