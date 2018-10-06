const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.checkNetwork = functions.https.onRequest((req, res) => {
    let text = req.get('text');
    res.send(text);
});
