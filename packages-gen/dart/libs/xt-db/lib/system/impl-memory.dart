import 'package:xtalk_lang/common-protocol.dart' as proto;
import 'package:xtalk_db/system/memory-graph.dart' as graph;
import 'package:xtalk_db/system/memory-util.dart' as util;
import 'package:xtalk_db/system/impl-common.dart' as impl_common;
import 'package:xtalk_db/text/base-flatten.dart' as f;
import 'dart:async';






ImplMemory(rows, schema, lookup, listeners, metadata) {
  (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["xt.db.system.impl_memory/ImplMemory"] = true;
  proto.register_protocol_impl(impl_common.ISourceLocal["on"],"xt.db.system.impl_memory/ImplMemory",<dynamic, dynamic>{
    "clear_db":clear_db,
    "pull":pull,
    "record_add":record_add,
    "record_delete":record_delete,
    "process_add_event":process_add_event,
    "process_remove_event":process_remove_event
  });
  proto.register_protocol_impl(
    impl_common.ISourceRemote["on"],
    "xt.db.system.impl_memory/ImplMemory",
    <dynamic, dynamic>{"pull_async":pull_async,"rpc_call_async":rpc_call_async}
  );
  proto.register_protocol_impl(impl_common.ISourceListener["on"],"xt.db.system.impl_memory/ImplMemory",<dynamic, dynamic>{
    "add_db_listener":impl_common.add_db_listener_default,
    "remove_db_listener":impl_common.remove_db_listener_default,
    "get_db_listener":impl_common.get_db_listener_default
  });
  return <dynamic, dynamic>{
    "::":"xt.db.system.impl_memory/ImplMemory",
    "::/protocols":<dynamic>[
        impl_common.ISourceLocal["on"],
        impl_common.ISourceRemote["on"],
        impl_common.ISourceListener["on"]
      ],
    "::/protocol-impls":<dynamic, dynamic>{
        impl_common.ISourceLocal["on"]:<dynamic, dynamic>{
          "clear_db":clear_db,
          "pull":pull,
          "record_add":record_add,
          "record_delete":record_delete,
          "process_add_event":process_add_event,
          "process_remove_event":process_remove_event
        },
        impl_common.ISourceRemote["on"]:<dynamic, dynamic>{"pull_async":pull_async,"rpc_call_async":rpc_call_async},
        impl_common.ISourceListener["on"]:<dynamic, dynamic>{
          "add_db_listener":impl_common.add_db_listener_default,
          "remove_db_listener":impl_common.remove_db_listener_default,
          "get_db_listener":impl_common.get_db_listener_default
        }
      },
    "rows":rows,
    "schema":schema,
    "lookup":lookup,
    "listeners":listeners,
    "metadata":metadata
  };
}

pull(impl, tree) {
  var opts = impl["opts"];
  var rows = impl["rows"];
  var schema = impl["schema"];
  return graph.pull(rows,schema,tree,opts);
}

pull_async(impl, tree) {
  return Future.sync(() {
    return pull(impl,tree);
  });
}

record_add(impl, table_name, records) {
  var opts = impl["opts"];
  var rows = impl["rows"];
  var schema = impl["schema"];
  return util.add_bulk(rows,schema,<dynamic, dynamic>{table_name:records});
}

record_delete(impl, table_name, ids) {
  var opts = impl["opts"];
  var rows = impl["rows"];
  var schema = impl["schema"];
  return util.remove_bulk(rows,schema,table_name,ids);
}

process_add_event(impl, data) {
  var rows = impl["rows"];
  var schema = impl["schema"];
  return util.add_bulk(rows,schema,data);
}

process_remove_event(impl, data) {
  var lookup = impl["lookup"];
  var rows = impl["rows"];
  var schema = impl["schema"];
  var ordered = f.flatten_bulk_ids(schema,lookup,data);
  var arr_52126 = ordered;
  for(var i52127 = 0; i52127 < arr_52126.length; ++i52127){
    var entry = arr_52126[i52127];
    var value_52148 = entry;
    var table_name = value_52148[0];
    var ids = value_52148[1];
    util.remove_bulk(rows,schema,table_name,ids);
  };
  return xt.lang.common_data.arr_map(ordered,(arr) {
    return arr[0];
  });
}

clear_db(impl) {
  var rows = impl["rows"];
  var arr_52149 = List<dynamic>.from(( rows ).keys);
  for(var i52150 = 0; i52150 < arr_52149.length; ++i52150){
    var table_key = arr_52149[i52150];
    rows.remove(table_key);
  };
  return null;
}

rpc_call_async(_impl, _rpc_spec, _args) {
  throw "db.impl.memory does not support rpc_call_async";
}

impl_memory(schema, lookup) {
  return ImplMemory(
    <dynamic, dynamic>{},
    schema,
    lookup,
    <dynamic, dynamic>{},
    <dynamic, dynamic>{}
  );
}