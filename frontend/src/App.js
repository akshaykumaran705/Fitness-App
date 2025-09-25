import logo from './logo.svg';
import './App.css';
import Login from './login';
import Register from './Register';
import Dashboard from "./Dashboard";
import React, {useEffect,useState} from 'react';
import {auth} from './firebase';
import {getAuth, onAuthStateChanged, signOut} from 'firebase/auth';

function App() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showLogin, setShowLogin] = useState(true);

    // This hook listens for Firebase auth state changes
    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
            setUser(currentUser);
            setLoading(false);
        });
        // Cleanup subscription on unmount
        return () => unsubscribe();
    }, []);

    if (loading) {
        return <div className="loading-screen">Loading...</div>;
    }

    return (
        <div className="App">
            {user ? (
                // If user is logged in, render the Dashboard
                <Dashboard user={user} />
            ) : (
                // If user is logged out, show either Login or Register
                <div className="auth-container">
                    {showLogin
                        ? <Login onSwitchToRegister={() => setShowLogin(false)} />
                        : <Register onSwitchToLogin={() => setShowLogin(true)} />
                    }
                </div>
            )}
        </div>
    );
}

export default App;