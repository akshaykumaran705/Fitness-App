import React, { useState } from 'react';
import { createUserWithEmailAndPassword } from 'firebase/auth';
import { auth } from './firebase';

// Accept the onSwitchToLogin function as a prop
function Register({ onSwitchToLogin }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleRegister = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await createUserWithEmailAndPassword(auth, email, password);
            // onAuthStateChanged in App.js will handle showing the dashboard
        } catch (err) {
            setError('Failed to register. Please try again.');
            console.error(err);
        }
    };

    return (
        <div className="card auth-form">
            <h2>Create your account</h2>
            <form onSubmit={handleRegister}>
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
                <button type="submit" className="button-primary">Register</button>
            </form>
            <p>
                Already have an account?{' '}
                <button onClick={onSwitchToLogin} className="form-switcher">
                    Login here
                </button>
            </p>
        </div>
    );
}

export default Register;

