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
    const auth = getAuth();
    useEffect(() =>{
        const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
            setUser(currentUser);
        });
        return () => unsubscribe();
    }, [auth]);
    if(loading) {
        return <div>Loading...</div>;
    }
    return(
        <div className="App bg-gray-100 text-gray-800 min-h-screen">
            {user ? (
                <Dashboard user={user} />
            ) : (
                showLogin ?
                    <Login onSwitchToRegister={() => setShowLogin(false)} /> :
                    <Register onSwitchToLogin={() => setShowLogin(true)} />
            )}
        </div>
    );
}
export default App;