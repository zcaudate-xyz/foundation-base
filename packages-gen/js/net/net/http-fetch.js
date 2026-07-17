const protocol = require("@xtalk/lang/common-protocol.js")

const fetch = require("@xtalk/net/http-fetch.js")

function HttpFetchClient(defaults,middleware){
  if(!globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["js.net.http_fetch/HttpFetchClient"]){
    globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["js.net.http_fetch/HttpFetchClient"] = true;
    protocol.register_protocol_impl(
      fetch.IHttpClient["on"],
      "js.net.http_fetch/HttpFetchClient",
      {"request_http":request_http}
    );
  }
  return {
    "::":"js.net.http_fetch/HttpFetchClient",
    "::/protocols":[fetch.IHttpClient["on"]],
    "defaults":defaults,
    "middleware":middleware
  };
}

function request_http_raw(client,input){
  let {body,headers,method,url} = input;
  return fetch(url,{"method":method,"headers":headers,"body":body}).then(function (res){
    return res.text().then(function (text){
      return {"status":res["status"],"headers":res["headers"],"body":text};
    });
  });
}

function request_http(client,input){
  let handler = fetch.prepare_middleware(client,request_http_raw);
  return handler(client,input);
}

function create(defaults,middleware){
  return HttpFetchClient(defaults,middleware || [fetch.wrap_prepare_input]);
}

module.exports = {
  ["request_http_raw"]:request_http_raw,
  ["request_http"]:request_http,
  ["HttpFetchClient"]:HttpFetchClient,
  ["create"]:create
}