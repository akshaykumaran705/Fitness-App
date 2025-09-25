import React, { useState } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from './firebase';

// Accept the onSwitchToRegister function as a prop
function Login({ onSwitchToRegister }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await signInWithEmailAndPassword(auth, email, password);
            // onAuthStateChanged in App.js will handle showing the dashboard
        } catch (err) {
            setError('Failed to sign in. Please check your credentials.');
            console.error(err);
        }
    };

    return (
        <div className="card auth-form">
            <h2>Sign in to your account</h2>
            <form onSubmit={handleLogin}>
                {error && <p style={{ color: 'red', textAlign: 'center' }}>{error}</p>}
                <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Email address"
                    required
                />
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Password"
                    required
                />
                <button type="submit" className="button-primary">Sign in</button>
            </form>
            <p>
                Don't have an account?{' '}
                <button onClick={onSwitchToRegister} className="form-switcher">
                    Register here
                </button>
            </p>
        </div>
    );
}

export default Login;

