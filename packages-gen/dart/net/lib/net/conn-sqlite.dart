import 'package:sqlite3/sqlite3.dart' as sqlite;

import 'package:xtalk_net/conn-sql.dart' as conn_sql;

DartSqliteClient(defaults, raw) {
  if(!(() {
    var dart_truthy__41992 = (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["dart.net.conn_sqlite/DartSqliteClient"];
    return (null != dart_truthy__41992) && (false != dart_truthy__41992);
  })()){
    (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["dart.net.conn_sqlite/DartSqliteClient"] = true;
    xt.lang.common_protocol.register_protocol_impl(conn_sql.ISqlClient["on"],"dart.net.conn_sqlite/DartSqliteClient",<dynamic, dynamic>{
      "connect":client_connect,
      "disconnect":client_disconnect,
      "query":client_query,
      "query_async":client_query_async
    });
  }
  return <dynamic, dynamic>{
    "::":"dart.net.conn_sqlite/DartSqliteClient",
    "::/protocols":<dynamic>[conn_sql.ISqlClient["on"]],
    "defaults":defaults,
    "raw":raw
  };
}

query_returns_rowsp(query) {
  var sql = query.trimLeft().toLowerCase();
  return sql.startsWith("select") || sql.startsWith("pragma") || sql.startsWith("with") || sql.startsWith("values") || sql.startsWith("explain");
}

error_output(err) {
  if(null == err){
    return null;
  }
  return err.toString();
}

callback_return(callback, err, result) {
  if(null != callback){
    return callback(error_output(err),result);
  }
  if(null != err){
    throw err;
  }
  return result;
}

decode_json_scalar(value) {
  if(("String" == (value.runtimeType).toString()) && (value.startsWith("[") || value.startsWith("{") || (value == "true") || (value == "false") || (value == "null"))){
    return jsonDecode(value);
  }
  else{
    return value;
  }
}

raw_query(db, query) {
  if((() {
    var dart_truthy__41993 = query_returns_rowsp(query);
    return (null != dart_truthy__41993) && (false != dart_truthy__41993);
  })()){
    var result = db.select(query);
    var columns = result.columnNames;
    var values = null;
    values = <dynamic>[];
    var iter_41994 = result.iterator;
    while(iter_41994.moveNext()){
      var row = iter_41994.current;
      var row_values = null;
      row_values = <dynamic>[];
      var arr_42007 = columns;
      for(var i = 0; i < arr_42007.length; ++i){
        var _ = arr_42007[i];
        row_values.add(row[i]);
      };
      values.add(row_values);
    };
    if((1 == values.length) && (1 == values[0].length)){
      return decode_json_scalar(values[0][0]);
    }
    if(columns.length > 0){
      return <dynamic>[<dynamic, dynamic>{"columns":columns,"values":values}];
    }
    return values;
  }
  else{
    db.execute(query);
    return <dynamic>[];
  }
}

client_connect(client, opts) {
  var defaults = client["defaults"];
  var env = opts ?? <dynamic, dynamic>{};
  var memory = env["memory"];
  var filename = env["filename"];
  if((null == memory) && (null == filename)){
    memory = true;
  }
  var db = null;
  if((null != memory) && (false != memory)){
    db = sqlite.sqlite3.openInMemory();
  }
  else{
    db = sqlite.sqlite3.open(filename);
  }
  client["raw"] = db;
  return client;
}

client_disconnect(client) {
  var raw = client["raw"];
  raw.dispose();
  return true;
}

client_query(client, query) {
  var raw = client["raw"];
  return raw_query(raw,query);
}

client_query_async(client, query) {
  return Future.sync(() {
    return client_query(client,query);
  });
}

create(defaults) {
  return DartSqliteClient(defaults ?? <dynamic, dynamic>{},null);
}