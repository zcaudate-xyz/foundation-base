import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_db/text/base-schema.dart' as base_schema;

import 'package:xtalk_lang/common-protocol.dart' as proto;

import 'package:xtalk_db/text/sql-raw.dart' as raw;

import 'package:xtalk_db/text/sql-manage.dart' as manage;

import 'package:xtalk_db/text/sql-table.dart' as sql_table;

import 'package:xtalk_db/system/impl-common.dart' as impl_common;

import 'package:xtalk_db/text/base-flatten.dart' as f;

import 'package:xtalk_db/text/sql-util.dart' as sql_util;

import 'package:xtalk_db/text/sql-graph.dart' as sql_graph;

import 'package:xtalk_net/conn-sql.dart' as conn_sql;

ImplSqlite(client, schema, lookup, listeners, opts, metadata) {
  if(!(() {
    var dart_truthy__42197 = (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["xt.db.system.impl_sqlite/ImplSqlite"];
    return (null != dart_truthy__42197) && (false != dart_truthy__42197);
  })()){
    (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["xt.db.system.impl_sqlite/ImplSqlite"] = true;
    proto.register_protocol_impl(impl_common.ISourceLocal["on"],"xt.db.system.impl_sqlite/ImplSqlite",<dynamic, dynamic>{
      "clear_db":clear_db,
      "pull":pull,
      "record_add":record_add,
      "record_delete":record_delete,
      "process_add_event":process_add_event,
      "process_remove_event":process_remove_event
    });
    proto.register_protocol_impl(
      impl_common.ISourceRemote["on"],
      "xt.db.system.impl_sqlite/ImplSqlite",
      <dynamic, dynamic>{"pull_async":pull_async,"rpc_call_async":rpc_call_async}
    );
    proto.register_protocol_impl(impl_common.ISourceListener["on"],"xt.db.system.impl_sqlite/ImplSqlite",<dynamic, dynamic>{
      "add_db_listener":impl_common.add_db_listener_default,
      "remove_db_listener":impl_common.remove_db_listener_default,
      "get_db_listener":impl_common.get_db_listener_default
    });
  }
  return <dynamic, dynamic>{
    "::":"xt.db.system.impl_sqlite/ImplSqlite",
    "::/protocols":<dynamic>[
        impl_common.ISourceLocal["on"],
        impl_common.ISourceRemote["on"],
        impl_common.ISourceListener["on"]
      ],
    "client":client,
    "schema":schema,
    "lookup":lookup,
    "listeners":listeners,
    "opts":opts,
    "metadata":metadata
  };
}

pull(impl, tree) {
  var client = impl["client"];
  var opts = impl["opts"];
  var schema = impl["schema"];
  return conn_sql.query(client,sql_graph.select(schema,tree,opts));
}

pull_async(impl, tree) {
  return Future.sync(() {
    return pull(impl,tree);
  });
}

record_add(impl, table_name, records) {
  var client = impl["client"];
  var lookup = impl["lookup"];
  var opts = impl["opts"];
  var schema = impl["schema"];
  var input = sql_table.prepare_add_input(<dynamic, dynamic>{table_name:records},schema,lookup,opts);
  if("" == input){
    return null;
  }
  return conn_sql.query(client,input);
}

record_delete(impl, table_name, ids) {
  var client = impl["client"];
  var opts = impl["opts"];
  var statements = xtd.arr_map(ids,(id) {
    return raw.raw_delete(table_name,<dynamic, dynamic>{"id":id},opts);
  });
  return conn_sql.query(client,statements.join("\n\n"));
}

process_add_event(impl, data) {
  var client = impl["client"];
  var lookup = impl["lookup"];
  var opts = impl["opts"];
  var schema = impl["schema"];
  var flat = f.flatten_bulk(schema,data);
  conn_sql.query(client,sql_table.prepare_add_input(data,schema,lookup,opts));
  return xtd.arr_keep(base_schema.table_order(lookup),(table_name) {
    return flat.containsKey(table_name) ? table_name : null;
  });
}

process_remove_event(impl, data) {
  var client = impl["client"];
  var lookup = impl["lookup"];
  var opts = impl["opts"];
  var schema = impl["schema"];
  var ordered = f.flatten_bulk_ids(schema,lookup,data);
  conn_sql.query(
    client,
    sql_table.prepare_remove_input(data,schema,lookup,opts)
  );
  return xtd.arr_map(ordered,(arr) {
    return arr[0];
  });
}

clear_db(impl) {
  var client = impl["client"];
  var lookup = impl["lookup"];
  var opts = impl["opts"];
  var schema = impl["schema"];
  conn_sql.query(
    client,
    manage.table_drop_all(schema,lookup,opts).join("\n\n")
  );
  conn_sql.query(
    client,
    manage.table_create_all(schema,lookup,opts).join("\n\n")
  );
  return impl;
}

rpc_call_async(_impl, _rpc_spec, _args) {
  throw "ImplSqlite does not support rpc_call_async";
}

impl_sqlite(client, schema, lookup) {
  return ImplSqlite(client,schema,lookup,<dynamic, dynamic>{},<dynamic, dynamic>{
    "return_join_fn":(arr) {
        return "json_group_array(json_object(" + arr.join(", ") + "))";
      },
    "strict":false,
    "wrapper_fn":(s, indent) {
        return (indent < 2) ? s : ("(\n" + xt.lang.common_string.pad_lines(s,2," ") + ")");
      },
    "querystr_fn":sql_util.encode_query_string,
    "types":sql_util.SQLITE,
    "operators":<dynamic, dynamic>{"ilike":"LIKE"},
    "values":<dynamic, dynamic>{"cast":false,"replace":sql_util.SQLITE_FN},
    "return_link_fn":(s, link_name) {
        return "'" + link_name + "', " + s;
      },
    "return_format_fn":(input, nest_fn, column_fn, opts) {
        if(("Map" == (input.runtimeType).toString()) || (input.runtimeType).toString().startsWith("_Map") || (input.runtimeType).toString().startsWith("LinkedMap")){
          return "'" + input["as"] + "', " + input["expr"];
        }
        else if((input.runtimeType).toString().startsWith("List") || (input.runtimeType).toString().startsWith("_GrowableList")){
          return Function.apply((nest_fn as Function),<dynamic>[input]);
        }
        else if("String" == (input.runtimeType).toString()){
          return "'" + input + "', " + Function.apply((column_fn as Function),<dynamic>[input]);
        }
        else{
          throw "Invalid input - " + (input).toString();
        }
      },
    "coerce":<dynamic, dynamic>{
        "boolean":(v) {
            if(("int" == (v.runtimeType).toString()) || ("double" == (v.runtimeType).toString()) || ("num" == (v.runtimeType).toString())){
              return 1 == v;
            }
            return v;
          },
        "jsonb":(expr) {
            return jsonDecode(expr);
          },
        "map":(expr) {
            return jsonDecode(expr);
          },
        "array":(expr) {
            return jsonDecode(expr);
          }
      },
    "return_count_fn":() {
        return "json_array(json_object('count',count" + "(*)))";
      },
    "column_fn":sql_util.default_quote_fn,
    "table_fn":sql_util.default_quote_fn
  },<dynamic, dynamic>{});
}

impl_sqlite_init(impl) {
  var client = impl["client"];
  var lookup = impl["lookup"];
  var opts = impl["opts"];
  var schema = impl["schema"];
  return ((Future.sync(() => conn_sql.connect(client,<dynamic, dynamic>{}))) as Future<dynamic>).then((value) async { return await Function.apply((client) {
    conn_sql.query(
      client,
      manage.table_create_all(schema,lookup,opts).join("\n\n")
    );
    return impl;
  },<dynamic>[value]); });
}