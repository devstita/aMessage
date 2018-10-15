app = require('express')();
const port = process.env.PORT;

app.get('/', (req, res) => {
    res.send('Hello World!! - Node.js');
});
app.listen(port);
