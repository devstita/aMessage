const functions = require('firebase-functions');
const firebase = require('firebase-admin');

// admin.initializeApp({
//     credential: admin.credential.cert(require('serviceAccountKey.json')),
//     apiKey: "AIzaSyCwT69D8ZQxIrUZStNmGXlaUvxfKc5KGFA",
//     authDomain: "devta-amessage.firebaseapp.com",
//     databaseURL: "https://devta-amessage.firebaseio.com",
//     projectId: "devta-amessage",
//     storageBucket: "devta-amessage.appspot.com",
//     messagingSenderId: "1060284614573"
// });
firebase.initializeApp();

exports.CheckClientNetwork = functions.https.onRequest((request, response) => {
    const phone = request.query.phone;
    response.send('<!DOCTYPE HTML><html><head><meta charset="utf-8"><title></title></head><body><h1 style="color: #947bff;">Response</h1></body></html>');

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

    // request.connection.addListener('close', () => {
    //     firebase.database().ref().child('TestServer/UserNetworkStatus').child(phone).set('Disconnected', (error) => {
    //         if (!error) {
    //             // Network Disconnected Signal Done!!
    //         } else {
    //             // Network Disconnected Signal Fail..
    //         }
    //     }).then(() => {
    //         console.log('Network Disconnected Signal Done!!');
    //     });
    // });
});
