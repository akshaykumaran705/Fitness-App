import React, {useState} from "react";
import {signInWithEmailAndPassword} from "firebase/auth";
import {auth} from "./firebase";

function Login(){
    const [email,setEmail] = useState('');
    const [password,setPassword] = useState('');
    const [msg,setMsg] = useState('');

    const handleLogin = async (e) =>{
        e.preventDefault();
        try {
            await signInWithEmailAndPassword(auth,email,password);
            setMsg('Login Successful');
        }
        catch (error){
            setMsg(error.message);
        }
    };
    return(
        <div style={{padding:'20px'}}>
            <h2>Login</h2>
            <form onSubmit={handleLogin}>
                <input type={"email"} placeholder={"Email"} onChange={(e)=>setEmail(e.target.value)}/><br/><br/>
                <input type={"password"} placeholder={"Password"} onChange={(e)=>setPassword(e.target.value)}/><br/><br/>
                <button type={"submit"}>Login</button>
            </form>
        </div>
    );
}
export default Login;
