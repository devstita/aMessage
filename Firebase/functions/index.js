// DEVELOPING: Develop Keeping HTTP (or Socket) Connection (Session) Algorithm (with https://firebase.google.com/docs/functions/networking)

const http = require('http');
const functions = require('firebase-functions');

const agent = new http.Agent({keepAlive: true});

exports.ForeverHTTPConnection = functions.https.onRequest((request, response) => {
    const req = http.request({
        host: 'https://us-central1-devta-amessage.cloudfunctions.net',
        port: 80,
        path: '/',
        method: 'GET',
        agent: agent, // ☆ Core Point ☆
    }, (res) => {
        let rawData = '';
        res.setEncoding('utf8');
        res.on('data', (chunk => { rawData += chunk }));
        res.on('end', () => {
            response.status(200).send(`Data: ${rawData}`);
        });
    });
    req.on('error', e => {
        response.status(500).send(`Error: ${e.message}`);
    })
});
