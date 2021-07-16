const app = require('express')();
const server = require('http').createServer(app);
// const io = require('socket.io')(server);
const io = require('socket.io')(server, {pingTimeout: 500, pingInterval: 1500});

const firebase = require('firebase-admin');

function get_my_colored_text(text) {
    text = text + ' ';
    const reset = '\x1b[0m';
    const foreground = '\x1b[30m';
    const background = '\x1b[47m';
    return `${foreground+background+text+reset}`
}

function print(log) {
    console.log(get_my_colored_text(log));
}

function print_with_phone(phone, log) {
    print(phone + " : " + log);
}

const PORT = process.env.PORT || 3000;
print('PORT: ' + PORT);

firebase.initializeApp({
    credential: firebase.credential.cert(require('./serviceAccountKey.json')),
    databaseURL: 'https://devta-amessage.firebaseio.com'
});

app.get('/', (req, res) => {
    // res.send('Hello World!! - Node.js');
    res.sendFile(__dirname + '/index.html');
});

io.on('connection', (socket) => {
    let phone = null;
    
    socket.on('data', (data) => {
        print('Data: ' + data.data);
    });

    socket.on('phone', (d_phone) => {
        phone = d_phone;
        print_with_phone(phone, 'Connection Event');
        let database_ref = firebase.database().ref().child('Users/' + phone);
        database_ref.set('Connected').then();
    });

    socket.on('disconnect', () => {
        print_with_phone(phone, 'Disconnect Event');

        if (phone) {
            let database_ref = firebase.database().ref().child('Users/' + phone);
            database_ref.set('Disconnected').then();
        }
    });
});

server.listen(PORT, () => {
    print(get_my_colored_text('==================== Server\'s Started!! ===================='));
});
