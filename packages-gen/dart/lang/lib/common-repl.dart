import 'package:xtalk_lang/common-lib.dart' as xt_lib;

notify_with_promise(notify_fn, host, port, value, id, key, opts) {
  if(!(() {
    var dart_truthy__40018 = (null != value) && (("Future" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("Future<"));
    return (null != dart_truthy__40018) && (false != dart_truthy__40018);
  })()){
    return Function.apply(
      (notify_fn as Function),
      <dynamic>[host,port,value,id,key,opts]
    );
  }
  else{
    (() async { try { return await ((Future.sync(() => ((Future.sync(() => value)) as Future<dynamic>).then((value) async { return await Function.apply((out) {
      return notify_with_promise(notify_fn,host,port,out,id,key,opts);
    },<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
      var out = ((null != (((err is Map) && ("xt.exception" == ((err as Map)["__type__"]))) ? ((err as Map)["message"]) : null)) && (false != (((err is Map) && ("xt.exception" == ((err as Map)["__type__"]))) ? ((err as Map)["message"]) : null))) ? (((err is Map) && ("xt.exception" == ((err as Map)["__type__"]))) ? ((err as Map)["message"]) : null) : err;
      return Function.apply((notify_fn as Function),<dynamic>[host,port,out,id,key,opts]);
    },<dynamic>[err])); } })();
  }
}

socket_connect_base(host, port, opts, cb) {
  return Socket.connect(host,port).then((conn) {
    return Function.apply(cb,<dynamic>[null,conn]);
  }).catchError((err) {
    return Function.apply(cb,<dynamic>[err,null]);
  });
}

socket_connect(host, port, opts) {
  var success_fn = xt_lib.wrap_callback(opts,"success");
  var error_fn = xt_lib.wrap_callback(opts,"error");
  var callback_fn = (err, out) {
    if(null != err){
      return Function.apply(error_fn,<dynamic>[err]);
    }
    else{
      return Function.apply(success_fn,<dynamic>[out]);
    }
  };
  return socket_connect_base(host,port,opts,callback_fn);
}

notify_socket_handler(conn, out) {
  conn.write(out + "\n");
  return conn.flush().then((_) {
    conn.destroy();
    return null;
  });
}

notify_socket(host, port, value, id, key, opts) {
  var out = xt_lib.return_encode(value,id,key);
  return socket_connect(host,port,<dynamic, dynamic>{
    "success":(conn) {
        return notify_socket_handler(conn,out);
      }
  });
}

notify_socket_full(host, port, value, id, key, opts) {
  return notify_with_promise(notify_socket,host,port,value,id,key,opts);
}

notify_socket_http_handler(conn, host, port, opts, output) {
  conn.write(
    "POST " + ((null == ((null == opts) ? null : opts["path"])) ? "/" : ((null == opts) ? null : opts["path"])) + " HTTP/1.0\r\n" + "Host: " + host + ":" + (port).toString() + "\r\n" + "Content-Length: " + (output.length).toString() + "\r\n" + "\r\n" + output
  );
  return conn.flush().then((_) {
    conn.destroy();
    return null;
  });
}

notify_socket_http(host, port, value, id, key, opts) {
  var output = xt_lib.return_encode(value,id,key);
  return socket_connect(host,port,<dynamic, dynamic>{
    "success":(conn) {
        return notify_socket_http_handler(conn,host,port,opts,output);
      }
  });
}

notify_http(host, port, value, id, key, opts) {
  return notify_socket_http(host,port,value,id,key,opts);
}

notify_http_full(host, port, value, id, key, opts) {
  return notify_with_promise(notify_http,host,port,value,id,key,opts);
}