import React,{ useState, useEffect } from "react";
import {getAuth,signOut} from "firebase/auth";
import Chart from 'chart.js/auto';

function Dashboard({ user }){
    const [activityStatus, setActivityStatus] = useState(null);
    const [error, setError] = useState('');
    const auth = getAuth();

    useEffect(() => {
        const fetchActivityStatus = async ()=>{
            if(!user) return;

            try {
                const token = await user.getIdToken();
                const response = await fetch('http://localhost:8082/api/activities/status',{
                    headers:{
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (!response.ok){
                    throw new Error(`Server responded with status: ${response.status}`);
                }
                const data = await response.json();
                setActivityStatus(data);
                setError('');
            } catch (err){
                console.error("Failed to fetch activity status",err);
                setError('Could not connect to the activity service');
            }
        };
        fetchActivityStatus();
        const intervalId = setInterval(fetchActivityStatus,5000);
        return () => clearInterval(intervalId);
    }, [user]);
useEffect(() => {
    const ctx = document.getElementById('activityChart');
    if(ctx){
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Sitting', 'Walking', 'Standing', 'Running'],
                datasets: [{
                    label: 'Time in Minutes',
                    data: [240, 90, 60, 15],
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.8)',
                        'rgba(54, 162, 235, 0.8)',
                        'rgba(255, 206, 86, 0.8)',
                        'rgba(75, 192, 192, 0.8)',
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
            }
        });
}
},[]);

const handleLogout = () =>{
       signOut(auth);
};
const getActivityIcon = (activity) =>{
    const icons ={
        'WALKING': '🚶‍♂️',
        'SITTING': '🧘',
        'STANDING': '🧍',
        'RUNNING': '🏃‍♀️',
        'UNKNOWN': '❓'
    };
    return icons[activity] || '❓';
};
return (
    <div className={"p-4 md:p-8 max-w-7xl mx-auto"}>
        <header className={"flex justify-between items-center mb-8"}>
            <div>
                <h1 className={"text-3xl font-bold"}>Your Wellness Dashboard</h1>
                <p className={"text-gray-600"}>Signed in as {user.email}</p>

            </div>
            <button onClick={handleLogout} className={"px-4 py-2 text-sm font-medium rounded-lg text-indigo-600 hover:bg-indigo-100"}>Logout</button>
        </header>
        <main className={"grid grid-cols-1 lg:grid-cols-3 gap-8"}>
            <div className="lg:col-span-1 space-y-8">
                <div className="bg-white p-6 rounded-3xl shadow-md text-center">
                    <h3 className="text-lg font-medium text-gray-500">Current Activity</h3>
                    <div className="my-6 flex flex-col items-center">
                        <div className="text-6xl mb-4">
                            {activityStatus ? getActivityIcon(activityStatus.lastDetectedActivity):'...'}
            </div>
                        <p className="text-4xl font-bold">
                        {error ? 'Error' : (activityStatus ? activityStatus.lastDetectedActivity : 'Connecting...')}
                    </p>
                        <p className={"text-sm text-gray-600"}>
                            Last Updated: {activityStatus ? new Date(activityStatus.timestamp).toLocaleTimeString():'never'}
                        </p>
                    </div>
                    <div className="bg-white p-6 rounded-3xl shadow-md">
                        <h3 className="text-lg font-medium text-gray-500 mb-2">💡 AI Wellness Tip</h3>
                        <p className="text-gray-700">
                            {error ? error : (activityStatus ? activityStatus.latestRecommendation : 'Awaiting activity data...')}
                        </p>
                    </div>
                </div>
            </div>
                <div className="lg:col-span-2 space-y-8">
                    <div className="bg-white p-6 rounded-3xl shadow-md">
                        <h3 className="text-lg font-medium text-gray-900 mb-4">Today's Activity Breakdown</h3>
                        <div className="h-64">
                            <canvas id="activityChart"></canvas>
                        </div>
                    </div>
                </div>
        </main>
    </div>
);
}
export default Dashboard;