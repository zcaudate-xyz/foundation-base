// xt.lang.base-repl/return-encode [14] 
export function return_encode(out,id,key){
  let type_fn = function (x){
    let name = typeof x;
    return (name == "object") ? (x ? x.constructor.name : name) : name;
  };
  let tb = typeof out;
  if("function" == tb){
    return JSON.stringify({
      "id":id,
      "key":key,
      "type":"raw",
      "return":"function",
      "value":out.toString()
    });
  }
  else if("object" != tb){
    return JSON.stringify({"id":id,"key":key,"type":"data","return":tb,"value":out});
  }
  else if(null == out){
    return JSON.stringify(
      {"id":id,"key":key,"type":"data","return":"nil","value":out}
    );
  }
  else{
    let ts = type_fn(out);
    try{
      if((ts == "Object") || (ts == "Array")){
        return JSON.stringify({"id":id,"key":key,"type":"data","value":out});
      }
      else{
        return JSON.stringify({
          "id":id,
          "key":key,
          "type":"raw",
          "return":ts,
          "value":out.toString()
        });
      }
    }
    catch(e){
      return JSON.stringify({
        "id":id,
        "key":key,
        "type":"raw",
        "return":ts,
        "value":out.toString()
      });
    }
  };
}

// xt.lang.base-repl/return-wrap [20] 
export function return_wrap(f){
  try{
    let out = f();
    return return_encode(out);
  }
  catch(e){
    let err = ("string" == (typeof e)) ? e : {"message":e["message"],"stack":e["stack"]};
    return JSON.stringify({"type":"error","value":err});
  };
}

// xt.lang.base-repl/return-eval [26] 
export function return_eval(s){
  return return_wrap(function (){
    return eval(s);
  });
}

// xt.lang.base-repl/return-callbacks [32] 
export function return_callbacks(callbacks,key){
  let result_fn = function (result){
    if(callbacks[key] != null){
      return callbacks[key](result);
    }
    else{
      return result;
    }
  };
  return result_fn;
}

// xt.lang.base-repl/socket-connect-base [59] 
export function socket_connect_base(host,port,opts,cb){
  let net = eval("require('net')");
  let rl = eval("require('readline')");
  let conn = new net.Socket();
  return conn.connect(port,host,function (){
    cb(null,conn);
  });
}

// xt.lang.base-repl/socket-connect [65] 
export function socket_connect(host,port,opts){
  let success_fn = return_callbacks(opts,"success");
  let error_fn = return_callbacks(opts,"error");
  return socket_connect_base(host,port,opts,function (err,conn){
    if(err){
      return error_fn(err);
    }
    else{
      return success_fn(conn);
    }
  });
}

// xt.lang.base-repl/notify-socket-handler [82] 
export function notify_socket_handler(conn,out){
  conn.write(out + "\n");
  conn.end();
}

// xt.lang.base-repl/notify-socket [89] 
export function notify_socket(host,port,value,id,key,opts){
  let out = return_encode(value,id,key);
  return socket_connect(host,port,{
    "success":function (conn){
        notify_socket_handler(conn,out);
      }
  });
}

// xt.lang.base-repl/notify-socket-http-handler [99] 
export function notify_socket_http_handler(conn,host,port,opts,output){
  let {path} = opts || {};
  let envelope = "POST " + (path || "/") + " HTTP/1.0\r\n" + "Host: " + host + ":" + String(port) + "\r\n" + "Content-Length: " + String(output.length) + "\r\n" + "\r\n" + output;
  conn.write(envelope);
  conn.end();
}

// xt.lang.base-repl/notify-socket-http [112] 
export function notify_socket_http(host,port,value,id,key,opts){
  let output = return_encode(value,id,key);
  return socket_connect(host,port,{
    "success":function (conn){
        notify_socket_http_handler(conn,host,port,opts,output);
      }
  });
}

// xt.lang.base-repl/notify-http [122] 
export function notify_http(host,port,value,id,key,opts){
  try{
    let {path,scheme} = opts || {};
    fetch(
      (scheme || "http") + "://" + host + ":" + port + "/" + (path || ""),
      {"method":"POST","body":return_encode(value,id,key)}
    );
    return ["async"];
  }
  catch(e){
    return ["unable to connect"];
  };
}