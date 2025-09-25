import logo from './logo.svg';
import './App.css';
import Login from './login';
import Register from './Register';
import React, {useEffect,useState} from 'react';
import {auth} from './firebase';
import {onAuthStateChanged, signOut} from 'firebase/auth';

function App(){
    const [user,setUser] = useState(null);
    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth,(currentUser)=>{
            setUser(currentUser);
        });
        return()=> unsubscribe();
        },[]);
    const handleLogout = () =>{
        signOut(auth);
    };
    return(
        <div style={{textAlign:'center',padding:'30px'}}>
            <h1>Fitness App Login</h1>
            {user?(
                <>
            <p>Welcome,{user.email}</p>
                <button onClick={handleLogout}>Logout</button>
            </>
            ):(
                <>
                    <Register/>
                    <hr/>
                    <Login/>
                </>
            )}
        </div>
    );
}
export default App;