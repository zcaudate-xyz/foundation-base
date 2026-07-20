import 'package:postgres/postgres.dart' as pg;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_net/conn-sql.dart' as conn_sql;
import 'dart:async';




DartPostgresClient(defaults, raw) {
  (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["dart.net.conn_postgres/DartPostgresClient"] = true;
  xt.lang.common_protocol.register_protocol_impl(conn_sql.ISqlClient["on"],"dart.net.conn_postgres/DartPostgresClient",<dynamic, dynamic>{
    "connect":client_connect,
    "disconnect":client_disconnect,
    "query":client_query,
    "query_async":client_query_async
  });
  return <dynamic, dynamic>{
    "::":"dart.net.conn_postgres/DartPostgresClient",
    "::/protocols":<dynamic>[conn_sql.ISqlClient["on"]],
    "::/protocol-impls":<dynamic, dynamic>{
        conn_sql.ISqlClient["on"]:<dynamic, dynamic>{
          "connect":client_connect,
          "disconnect":client_disconnect,
          "query":client_query,
          "query_async":client_query_async
        }
      },
    "defaults":defaults,
    "raw":raw
  };
}

default_env() {
  return <dynamic, dynamic>{
    "host":"127.0.0.1",
    "port":5432,
    "user":"postgres",
    "password":"postgres",
    "database":"test"
  };
}

normalise_scalar_output(value) {
  if("String" == (value.runtimeType).toString()){
    var parsed = num.tryParse( value );
    return parsed ?? value;
  }
  return value;
}

normalise_query_output(rows) {
  if(0 == rows.length){
    return <dynamic>[];
  }
  else if((1 == rows.length) && (1 == rows[0].length)){
    return normalise_scalar_output(rows[0][0]);
  }
  else{
    return xtd.arr_map(rows,(row) {
      return row.toColumnMap();
    });
  }
}

client_connect(client, opts) {
  var defaults = client["defaults"];
  var env = xtd.obj_clone(default_env());
  xtd.obj_assign(env,defaults);
  xtd.obj_assign(env,opts ?? <dynamic, dynamic>{});
  var endpoint = pg.Endpoint.new(
    host:env["host"],
    port:env["port"],
    database:env["database"],
    username:env["user"],
    password:env["password"]
  );
  var settings = pg.ConnectionSettings(connectTimeout: const Duration(seconds: 5), sslMode: pg.SslMode.disable);
  return ((Future.sync(() => pg.Connection.open(endpoint,settings:settings))) as Future<dynamic>).then((value) async { return await Function.apply((session) {
    client["raw"] = session;
    return client;
  },<dynamic>[value]); });
}

client_disconnect(client) {
  var raw = client["raw"];
  raw.close();
  return true;
}

client_query(client, query) {
  var raw = client["raw"];
  return ((Future.sync(() => raw.execute(query))) as Future<dynamic>).then((value) async { return await Function.apply((rows) {
    return normalise_query_output(rows);
  },<dynamic>[value]); });
}

client_query_async(client, query) {
  return client_query(client,query);
}

create(defaults) {
  return DartPostgresClient(defaults ?? <dynamic, dynamic>{},null);
}