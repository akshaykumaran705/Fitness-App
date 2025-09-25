import React,{useState} from 'react';
import {createUserWithEmailAndPassword} from 'firebase/auth';
import {auth} from './firebase';
import Login from "./login";
import App from "./App";

//const auth = getAuth();
function Register() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [msg, setMsg] = useState('');
    const handlesRegister = async (e) => {
        e.preventDefault();
        try {
            await createUserWithEmailAndPassword(auth, email, password)
            setMsg('Registered Successfully');
        } catch (error) {
            setMsg(error.message);
        }
    };
    return (
        <div style={{padding: '20px'}}>
            <h2>Register User</h2>
            <p>Already have an account?
            <button onClick={App.onSwitchToLogin} className={"text-indigo-600 hover:underline"}>Login here</button>
            </p>
            <form onSubmit={handlesRegister}>
                <input type={"email"} placeholder={"Email"} onChange={(e) => setEmail(e.target.value)}/><br/><br/>
                <input type={"password"} placeholder={"Password"}
                       onChange={(e) => setPassword(e.target.value)}/><br/><br/>
                <button type={"submit"}>Register</button>
            </form>
        </div>
    );
}
export default Register;