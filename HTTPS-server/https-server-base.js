// Built-in HTTPS support
const https = require("https");
// Handling GET request (npm install express)
const express = require("express");
// Load of files from the local file system
var fs = require('fs'); 

const PORT = 4433;
const app = express();

// Get request for resource /
app.get("/", function (req, res) {
    console.log(
        req.socket.remoteAddress
        //+ ' ' + req.socket.getPeerCertificate().subject.CN
        + ' ' + req.method
        + ' ' + req.url);
    res.send("<html><body>Secure Hello World with node.js</body></html>");
});


// configure TLS handshake
const options = {
    key: fs.readFileSync('<server private key PEM>'),
    cert: fs.readFileSync('<server certificate PEM>'),
    //ca: fs.readFileSync('<server trustbase PEM (root CA)>'), 
    //requestCert: true, 
    //rejectUnauthorized: true
};

// Create HTTPS server
https.createServer(options, app).listen(PORT, 
    function (req, res) {
        console.log("Server started at port " + PORT);
    }
);
