const xt_lib = require("@xtalk/lang/common-lib.js")

function notify_with_promise(notify_fn,host,port,value,id,key,opts){
  if(!(value instanceof Promise)){
    return notify_fn(host,port,value,id,key,opts);
  }
  else{
    value.then(function (out){
      return notify_with_promise(notify_fn,host,port,out,id,key,opts);
    }).catch(function (err){
      let out = ((err instanceof Error) ? err["message"] : null) ? ((err instanceof Error) ? err["message"] : null) : err;
      return notify_fn(host,port,out,id,key,opts);
    });
  }
}

function socket_connect_base(host,port,opts,cb){
  let net = eval("require('net')");
  let conn = new net.Socket();
  return conn.connect(port,host,function (){
    cb(null,conn);
  })
}

function socket_connect(host,port,opts){
  let success_fn = xt_lib.wrap_callback(opts,"success");
  let error_fn = xt_lib.wrap_callback(opts,"error");
  let callback_fn = function (err,out){
    if(null != err){
      return error_fn.apply(null,[err]);
    }
    else{
      return success_fn.apply(null,[out]);
    }
  };
  return socket_connect_base(host,port,opts,callback_fn);
}

function notify_socket_handler(conn,out){
  conn.write(out + "\n");
  return conn.end();
}

function notify_socket(host,port,value,id,key,opts){
  let out = xt_lib.return_encode(value,id,key);
  return socket_connect(host,port,{
    "success":function (conn){
        return notify_socket_handler(conn,out);
      }
  });
}

function notify_socket_full(host,port,value,id,key,opts){
  return notify_with_promise(notify_socket,host,port,value,id,key,opts);
}

function notify_socket_http_handler(conn,host,port,opts,output){
  conn.write(
    "POST " + ((null == ((null == opts) ? null : opts["path"])) ? "/" : ((null == opts) ? null : opts["path"])) + " HTTP/1.0\r\n" + "Host: " + host + ":" + String(port) + "\r\n" + "Content-Length: " + String(output.length) + "\r\n" + "\r\n" + output
  );
  return conn.end();
}

function notify_socket_http(host,port,value,id,key,opts){
  let output = xt_lib.return_encode(value,id,key);
  return socket_connect(host,port,{
    "success":function (conn){
        return notify_socket_http_handler(conn,host,port,opts,output);
      }
  });
}

function notify_http(host,port,value,id,key,opts){
  try{
    let {path,scheme} = opts || {};
    fetch(
      (scheme || "http") + "://" + host + ":" + port + "/" + (path || ""),
      {"method":"POST","body":xt_lib.return_encode(value,id,key)}
    );
    return ["async"];
  }
  catch(e){
    return ["unable to connect"];
  }
}

function notify_http_full(host,port,value,id,key,opts){
  return notify_with_promise(notify_http,host,port,value,id,key,opts);
}

module.exports = {
  ["notify_with_promise"]:notify_with_promise,
  ["socket_connect_base"]:socket_connect_base,
  ["socket_connect"]:socket_connect,
  ["notify_socket_handler"]:notify_socket_handler,
  ["notify_socket"]:notify_socket,
  ["notify_socket_full"]:notify_socket_full,
  ["notify_socket_http_handler"]:notify_socket_http_handler,
  ["notify_socket_http"]:notify_socket_http,
  ["notify_http"]:notify_http,
  ["notify_http_full"]:notify_http_full
}