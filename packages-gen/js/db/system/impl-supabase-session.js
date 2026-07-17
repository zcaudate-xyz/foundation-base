const xtd = require("@xtalk/lang/common-data.js")

const addon = require("@xtalk/net/addon-supabase.js")

const http_fetch = require("@xtalk/net/http-fetch.js")

const xts = require("@xtalk/lang/common-string.js")

const http_util = require("@xtalk/net/http-util.js")

function get_session(impl){
  return xtd.get_in(impl,["state","session"]);
}

function set_session(impl,session){
  xtd.set_in(impl,["state","session"],session);
  return session;
}

function refresh_session(impl){
  let {client} = impl;
  let session = xtd.get_in(impl,["state","session"]);
  let refresh_token = xtd.get_in(session,"refresh_token");
  if(null == refresh_token){
    return Promise.resolve().then(function (){
      return null;
    });
  }
  else{
    return http_fetch.request_http(
      client,
      addon.cmd_token_refresh({"refresh_token":refresh_token},{})
    ).then(function (response){
      return set_session(impl,http_util.get_body_data(response));
    }).catch(function (err){
      return err;
    });
  }
}

function auto_refresh_interval(impl){
  let expires_in = xtd.get_in(impl,["state","session","expires_in"]);
  if(null != expires_in){
    return ((expires_in * 1000) >= 60000) ? ((expires_in * 1000) - 60000) : 1000;
  }
  else{
    return xtd.get_in(impl,["opts","auto_refresh_interval"]) || 300000;
  }
}

function auto_refresh_fn(impl,delay,refresh_id){
  let current_id = xtd.get_in(impl,["state","auto_refresh","current"]);
  if(current_id == refresh_id){
    refresh_session(impl).catch(function (err){
      return err;
    }).then(function (_){
      return new Promise(function (resolve,reject){
        setTimeout(function (){
          new Promise(function (inner_resolve){
            inner_resolve((function (){
              return auto_refresh_fn(impl,auto_refresh_interval(impl),refresh_id);
            })());
          }).then(function (value){
            resolve(value);
          }).catch(function (err){
            reject(err);
          });
        },delay);
      });
    });
    return refresh_id;
  }
  else{
    return current_id;
  }
}

function auto_refresh_stop(impl){
  let current_id = xtd.get_in(impl,["state","auto_refresh","current"]);
  xtd.set_in(impl,["state","auto_refresh","current"],null);
  return current_id;
}

function auto_refresh_start(impl){
  let current_id = xtd.get_in(impl,["state","auto_refresh","current"]);
  if(null != current_id){
    return current_id;
  }
  let refresh_id = xts.str_rand(8);
  xtd.set_in(impl,["state","auto_refresh","current"],refresh_id);
  return auto_refresh_fn(impl,auto_refresh_interval(impl),refresh_id);
}

module.exports = {
  ["get_session"]:get_session,
  ["set_session"]:set_session,
  ["refresh_session"]:refresh_session,
  ["auto_refresh_interval"]:auto_refresh_interval,
  ["auto_refresh_fn"]:auto_refresh_fn,
  ["auto_refresh_stop"]:auto_refresh_stop,
  ["auto_refresh_start"]:auto_refresh_start
}