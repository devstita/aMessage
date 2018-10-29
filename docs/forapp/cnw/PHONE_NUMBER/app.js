const http = require('http');
const net = require('net');

var hostName = '127.0.0.1';
var port = 3000;

net.createServer(function(client) {
    console.log('Client connected: ' + client.address)
});

// http.createServer(function(req, res) {
//     res.writeHead(200, {'Content-Type': 'text/plain'});
//     res.end('Hello World!!\n');
// }).listen(port, hostName);

console.log('Server is running at http://' + hostName + ":" + port);
