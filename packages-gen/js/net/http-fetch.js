const protocol = require("@xtalk/lang/common-protocol.js")

const util = require("@xtalk/net/http-util.js")

var IHttpClient = protocol.create_protocol_fn("xt.net.http_fetch/IHttpClient",{
  "request_http":{"name":"request_http","arglist":["client","input"]}
});

function request_http(client,input){
  let method_fn = protocol.protocol_method(client,"xt.net.http_fetch/IHttpClient","request_http");
  return method_fn(client,input);
}

var REQUEST_FIELDS = ["method","url","query","headers","body","timeout","opts"];

var RESPONSE_FIELDS = ["status","headers","body","error"];

function prepare_url(client,input){
  let {path,url} = input;
  if(!(null == url)){
    return url;
  }
  let {defaults} = client;
  let {basepath,host,port,secured} = defaults;
  return "http" + (secured ? "s" : "") + "://" + host + ":" + String(port || 80) + (basepath || "") + (path || "");
}

function prepare_input(client,input){
  let {defaults} = client;
  let {body,method} = input;
  let headers = Object.assign(Object.assign({},defaults["headers"]),input["headers"]);
  let output = {
    "url":prepare_url(client,input),
    "method":method || "GET",
    "headers":headers
  };
  if(null != body){
    output = Object.assign(output,{"body":body});
  }
  return output;
}

function wrap_prepare_input(handler){
  return function (client,input){
    let prepped = prepare_input(client,input);
    return handler(client,prepped);
  };
}

function wrap_normalise(handler){
  return function (client,input){
    return handler(client,input).then(function (response){
      return util.response_normalize(response);
    });
  };
}

function then_normalise(promise){
  return promise.then(util.response_normalize);
}

function prepare_middleware(client,handler){
  let {middleware} = client;
  for(let wrapper of middleware || []){
    handler = wrapper(handler);
  };
  return handler;
}

function prepare_handler(client,handler){
  return wrap_normalise(prepare_middleware(client,wrap_prepare_input(handler)));
}

module.exports = {
  ["IHttpClient"]:IHttpClient,
  ["request_http"]:request_http,
  ["REQUEST_FIELDS"]:REQUEST_FIELDS,
  ["RESPONSE_FIELDS"]:RESPONSE_FIELDS,
  ["prepare_url"]:prepare_url,
  ["prepare_input"]:prepare_input,
  ["wrap_prepare_input"]:wrap_prepare_input,
  ["wrap_normalise"]:wrap_normalise,
  ["then_normalise"]:then_normalise,
  ["prepare_middleware"]:prepare_middleware,
  ["prepare_handler"]:prepare_handler
}