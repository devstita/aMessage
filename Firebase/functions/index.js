const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp({
    apiKey: "AIzaSyCwT69D8ZQxIrUZStNmGXlaUvxfKc5KGFA",
    authDomain: "devta-amessage.firebaseapp.com",
    databaseURL: "https://devta-amessage.firebaseio.com",
    projectId: "devta-amessage",
    storageBucket: "devta-amessage.appspot.com",
    messagingSenderId: "1060284614573"
});

exports.CheckClientNetwork = functions.https.onRequest((request, response) => {
    const phone = request.query.phone;
    firebase.database().ref().child('TestServer/UserNetworkStatus').child(phone).set('Connected', (error) => {
        if (!error) {
            // Network Connected Signal Done!!
        } else {
            // Network Connected Signal Fail..
        }
    }).then(() => {
        console.log('Network Connected Signal Done!!');
    });

    request.on('close', () => {
        firebase.database().ref().child('TestServer/UserNetworkStatus').child(phone).set('Disconnected', (error) => {
            if (!error) {
                // Network Disconnected Signal Done!!
            } else {
                // Network Disconnected Signal Fail..
            }
        }).then(() => {
            console.log('Network Disconnected Signal Done!!');
        });
    });
});
