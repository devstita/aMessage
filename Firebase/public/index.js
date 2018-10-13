window.onbeforeunload = () => {
    console.log('on before unload');
};

function connected(phone) {
    firebase.database().ref().child('TestServer/UserNetworkStatus/' + phone).set('Connected');
}

function disconnected(phone) {
    firebase.database().ref().child('TestServer/UserNetworkStatus/' + phone).set('Disconnected');
}

var req = new XMLHttpRequest();
console.log(req.getAllResponseHeaders());
