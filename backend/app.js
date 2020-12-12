var express = require('express');
var http    = require('http');
var path    = require('path');
var routes  = require('./routes/index');

var app = express();


app.use(express.static(path.join(__dirname,'./angular/dist/angular')));
app.use('/', routes);

app.get('*', function(req, res, next) {
  res.sendFile(path.join(__dirname,'./angular/dist/angular/index.html'));
});

const port = process.env.PORT || 3000;
app.set('port',port);

http.createServer(app).listen(port);
