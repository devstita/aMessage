// DEVELOPING: Develop Keeping HTTP (or Socket) Connection (Session) Algorithm (with https://firebase.google.com/docs/functions/networking)

// const http = require('http');
// const net = require('net');
const functions = require('firebase-functions');

exports.ForeverHTTPConnection = functions.https.onRequest((request, response) => {
    response.on("close", () => {

    });
});
