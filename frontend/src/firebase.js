import { getAuth } from 'firebase/auth';
// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
    apiKey: "AIzaSyBTv75yJDpgALg1vD6jWeZij2yXOwIk30o",
    authDomain: "fitness-app-b9035.firebaseapp.com",
    projectId: "fitness-app-b9035",
    storageBucket: "fitness-app-b9035.firebasestorage.app",
    messagingSenderId: "642050301480",
    appId: "1:642050301480:web:71c6ba6a0537d7e88c9f1d",
    measurementId: "G-QDFLJ4EZHK"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);