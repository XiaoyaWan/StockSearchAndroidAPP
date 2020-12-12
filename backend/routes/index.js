var express = require('express');
var path    = require('path');
var router = express.Router();

/* GET home page. */
router.get('/search', function (req, res) {
  var query = '';
  if (req.url.indexOf('?')!=-1) {
    var arr = req.url.split('?');
    var url=arr[0];
    var arr2 = arr[1].split('&');
    for (var i = 0; i < arr2.length; i++) {
      var arr3 = arr2[i].split('=');
      query = arr3[1];
    }
  }else{
    var url=req.url;
  }
  var request = require('request');
  var requestOptions = {
    'url': 'https://api.tiingo.com/tiingo/utilities/search?query='+query+'&token=cbff2b4a40e74a1e30f8d611d1944ee95b264edc',
    'headers': {
        'Content-Type': 'application/json'
        }
  };
  request(requestOptions,
    function(error, response, body) {
        res.send(body);
    }
  );

});

router.get('/details', function (req, res) {
  var query = '';
  if (req.url.indexOf('?')!=-1) {
    var arr = req.url.split('?');
    var url=arr[0];//地址
    var arr2 = arr[1].split('&');
    for (var i = 0; i < arr2.length; i++) {
      var arr3 = arr2[i].split('=');
      query = arr3[1];
    }
  }else{
    var url=req.url;
  }

  var request = require('request');
  var requestOptions = {
        'url': 'https://api.tiingo.com/tiingo/daily/'+query+'?token=cbff2b4a40e74a1e30f8d611d1944ee95b264edc',
        'headers': {
            'Content-Type': 'application/json'
            }
        };
  request(requestOptions,
    function(error, response, body) {
        res.send(body);
    }
  );
});

router.get('/detailssmachart', function (req, res, next) {
  var query = '';
  if (req.url.indexOf('?')!=-1) {
    var arr = req.url.split('?');
    var url=arr[0];
    var arr2 = arr[1].split('&');
    for (var i = 0; i < arr2.length; i++) {
      var arr3 = arr2[i].split('=');
      query = arr3[1];
    }
  }else{
    var url=req.url;
  }

  var date = new Date();
  var strDate = ( date.getFullYear() - 2 )+'-'+( date.getMonth() + 1 )+'-'+ date.getDate();
  var request = require('request');
    var requestOptions = {
        'url': 'https://api.tiingo.com/tiingo/daily/'+query+'/prices?startDate='+strDate+'&token=cbff2b4a40e74a1e30f8d611d1944ee95b264edc',
        'headers': {
            'Content-Type': 'application/json'
            }
        };
  request(requestOptions,
    function(error, response, body) {
      if(error){
        next(error);
      }else{
        res.send(body);
      }
    }
  );
});

//fresh last price auto
router.get('/detailsrefresh', function (req, res, next) {

  var query = '';
  if (req.url.indexOf('?')!=-1) {
    var arr = req.url.split('?');
    var url=arr[0];
    var arr2 = arr[1].split('&');
    for (var i = 0; i < arr2.length; i++) {
      var arr3 = arr2[i].split('=');
      query = arr3[1];
    }
  }else{
    var url=req.url;
  }

  var request = require('request');
  var requestOptions = {
      'url': 'https://api.tiingo.com/iex/?tickers='+query+'&token=cbff2b4a40e74a1e30f8d611d1944ee95b264edc',
      'headers': {
          'Content-Type': 'application/json'
       }
  };

  request(requestOptions,
    function(error, response, body) {
      console.log('refresh');
      if(error){
          next(error);
      }else {
        try{
            var result = JSON.parse(body.slice(1, body.length - 1));
            result['change'] = parseFloat(result['last']) - parseFloat(result['prevClose']);
            result['changePerc'] = (parseFloat(result['last']) - parseFloat(result['prevClose'])) / parseFloat(result['prevClose']) * 100;
            var date1 = new Date();
            var date2 = new Date(result['timestamp']);

            // console.log(date2.toLocaleString('en-us',{ hour12: false}));
            var str1 = date1.toLocaleString('us-PT',{ hour12: false});
            var str2 = date2.toLocaleString('us-PT',{ hour12: false});

            result['timestamp'] = str2.split(',')[0].split('/')[2]+'-'+str2.split(',')[0].split('/')[0]+'-'+str2.split(',')[0].split('/')[1]+str2.split(',')[1];
            result['currentTimestamp'] = str1.split(',')[0].split('/')[2]+'-'+str1.split(',')[0].split('/')[0]+'-'+str1.split(',')[0].split('/')[1]+str1.split(',')[1];

            // var strDate = '';

            if (((date1 - date2) / 1000) < 60) {
                result['market'] = '+';
                result['marketTime'] = '';
                strDate = result['currentTimestamp'].split(' ')[0];
            } else {
                result['market'] = '-';
                result['marketTime'] = date2.toISOString();
                strDate = result['timestamp'].split(' ')[0];
            }

            // result['timestamp'] = date2.toISOString();
            // result['currentTimestamp'] =  date1.toISOString();

            // var request = require('request');
            // var requestOptions = {
            //     'url': 'https://api.tiingo.com/iex/'+query+'/prices?startDate='+strDate+'&resampleFreq=4min&token=cbff2b4a40e74a1e30f8d611d1944ee95b264edc',
            //     'headers': { 'Content-Type': 'application/json' } };
            // request(requestOptions,
            //     function(error, response, body) {
            //         result['detailschart'] = JSON.parse(body);
                    res.send(JSON.stringify(result));
            //     }
            // );
        }catch (e) {
            next(e);
        }
      }
    }
  );
});

router.get('/news', function (req, res) {
  var query = '';
  if (req.url.indexOf('?')!=-1) {
    var arr = req.url.split('?');
    var url=arr[0];//地址
    var arr2 = arr[1].split('&');
    for (var i = 0; i < arr2.length; i++) {
      var arr3 = arr2[i].split('=');
      query = arr3[1];
    }
  }else{
    var url=req.url;
  }

  var request = require('request');
  var requestOptions = {
        'url': 'https://newsapi.org/v2/everything?apiKey=d7269a6021df45d2b990079e739db5de&q='+query,
        'headers': {
            'Content-Type': 'application/json'
            }
        };
  request(requestOptions,
    function(error, response, body) {
        // console.log(body);
        var temp = JSON.parse(body).articles;
        var result = [];
        for(let i = 0; i < Math.min(10, temp.length); i++){
            // if(temp[i].urlToImage == null || temp[i].title == null || temp[i].title == null || temp[i].url == null
            //     || temp[i].description == null || temp[i].source.name == null || temp[i].publishedAt == null){
            // }else{
                result.push(temp[i]);
            // }
        }
        res.send(JSON.stringify(result));
        // res.send(JSON.parse(body));
    }
  );
});

router.get('/simpledata', function (req, res, next) {
    console.log("simpledata");
  var query = '';
  if (req.url.indexOf('?')!=-1) {
    var arr = req.url.split('?');
    var url=arr[0];
    var arr2 = arr[1].split('&');
    for (var i = 0; i < arr2.length; i++) {
      var arr3 = arr2[i].split('=');
      query = arr3[1];
    }
  }else{
    var url=req.url;
  }


  var request = require('request');
  var requestOptions = {
      'url': 'https://api.tiingo.com/iex/?tickers='+query+'&token=cbff2b4a40e74a1e30f8d611d1944ee95b264edc',
      'headers': {
          'Content-Type': 'application/json'
       }
  };

  request(requestOptions,
    function(error, response, body) {
      if(error){
          next(error);
      }else {
        try{
            var result = JSON.parse(body);
            var finalRes = [];
            for( var j = 0; j < result.length; j++) {
                const finalResult = {};
                finalResult['ticker'] = result[j]['ticker'];
                finalResult['last'] = result[j]['last'];
                finalResult['change'] = (parseFloat(result[j]['last']) - parseFloat(result[j]['prevClose'])).toFixed(2);
                const i = parseFloat(result[j]['last']) - parseFloat(result[j]['prevClose']);
                if (i > 0) {
                    finalResult['trend'] = '+';
                } else if (i < 0) {
                    finalResult['trend'] = '-';
                } else {
                    finalResult['trend'] = '0';
                }
                finalResult['changePerc'] = ((parseFloat(result[j]['last']) - parseFloat(result[j]['prevClose'])) / parseFloat(result[j]['prevClose']) * 100).toFixed(2);

                var date1 = new Date();
                var date2 = new Date(result[j]['timestamp']);
                if (((date1 - date2) / 1000) < 60) {
                    finalResult['market'] = '+';
                } else {
                    finalResult['market'] = '-';
                }

                finalRes.push(finalResult);
            }

            res.send(JSON.stringify(finalRes));
        }catch (e) {
            next(e);
        }
      }
    }
  );
});

module.exports = router;
