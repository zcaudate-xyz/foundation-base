import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_lang/common-protocol.dart' as proto;

var ISourceListener = Function.apply((proto.create_protocol_fn as Function),<dynamic>[
  "xt.db.system.impl_common/ISourceListener",
  <dynamic, dynamic>{
  "add_db_listener":<dynamic, dynamic>{
    "name":"add_db_listener",
    "arglist":<dynamic>["impl","listener_id","handle"]
  },
  "remove_db_listener":<dynamic, dynamic>{
    "name":"remove_db_listener",
    "arglist":<dynamic>["impl","listener_id"]
  },
  "get_db_listener":<dynamic, dynamic>{
    "name":"get_db_listener",
    "arglist":<dynamic>["impl","listener_id"]
  }
}
]);

add_db_listener(impl, listener_id, handle) {
  var method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_common/ISourceListener",
    "add_db_listener"
  );
  return Function.apply((method_fn as Function),<dynamic>[impl,listener_id,handle]);
}

remove_db_listener(impl, listener_id) {
  var method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_common/ISourceListener",
    "remove_db_listener"
  );
  return Function.apply((method_fn as Function),<dynamic>[impl,listener_id]);
}

get_db_listener(impl, listener_id) {
  var method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_common/ISourceListener",
    "get_db_listener"
  );
  return Function.apply((method_fn as Function),<dynamic>[impl,listener_id]);
}

var ISourceRemote = Function.apply((proto.create_protocol_fn as Function),<dynamic>[
  "xt.db.system.impl_common/ISourceRemote",
  <dynamic, dynamic>{
  "pull_async":<dynamic, dynamic>{"name":"pull_async","arglist":<dynamic>["impl","tree"]},
  "rpc_call_async":<dynamic, dynamic>{
    "name":"rpc_call_async",
    "arglist":<dynamic>["impl","rpc_spec","args"]
  }
}
]);

pull_async(impl, tree) {
  var method_fn = proto.protocol_method(impl,"xt.db.system.impl_common/ISourceRemote","pull_async");
  return Function.apply((method_fn as Function),<dynamic>[impl,tree]);
}

rpc_call_async(impl, rpc_spec, args) {
  var method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_common/ISourceRemote",
    "rpc_call_async"
  );
  return Function.apply((method_fn as Function),<dynamic>[impl,rpc_spec,args]);
}

var ISourceRealtime = Function.apply((proto.create_protocol_fn as Function),<dynamic>[
  "xt.db.system.impl_common/ISourceRealtime",
  <dynamic, dynamic>{
  "subscribe_db":<dynamic, dynamic>{
    "name":"subscribe_db",
    "arglist":<dynamic>["impl","conn_id","topics"]
  },
  "unsubscribe_db":<dynamic, dynamic>{
    "name":"unsubscribe_db",
    "arglist":<dynamic>["impl","conn_id","topics"]
  }
}
]);

subscribe_db(impl, conn_id, topics) {
  var method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_common/ISourceRealtime",
    "subscribe_db"
  );
  return Function.apply((method_fn as Function),<dynamic>[impl,conn_id,topics]);
}

unsubscribe_db(impl, conn_id, topics) {
  var method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_common/ISourceRealtime",
    "unsubscribe_db"
  );
  return Function.apply((method_fn as Function),<dynamic>[impl,conn_id,topics]);
}

var ISourceLocal = Function.apply((proto.create_protocol_fn as Function),<dynamic>[
  "xt.db.system.impl_common/ISourceLocal",
  <dynamic, dynamic>{
  "clear_db":<dynamic, dynamic>{"name":"clear_db","arglist":<dynamic>["impl"]},
  "pull":<dynamic, dynamic>{"name":"pull","arglist":<dynamic>["impl","tree"]},
  "record_add":<dynamic, dynamic>{
    "name":"record_add",
    "arglist":<dynamic>["impl","table_name","records"]
  },
  "record_delete":<dynamic, dynamic>{
    "name":"record_delete",
    "arglist":<dynamic>["impl","table_name","ids"]
  },
  "process_add_event":<dynamic, dynamic>{
    "name":"process_add_event",
    "arglist":<dynamic>["impl","data"]
  },
  "process_remove_event":<dynamic, dynamic>{
    "name":"process_remove_event",
    "arglist":<dynamic>["impl","data"]
  }
}
]);

clear_db(impl) {
  var method_fn = proto.protocol_method(impl,"xt.db.system.impl_common/ISourceLocal","clear_db");
  return Function.apply((method_fn as Function),<dynamic>[impl]);
}

pull(impl, tree) {
  var method_fn = proto.protocol_method(impl,"xt.db.system.impl_common/ISourceLocal","pull");
  return Function.apply((method_fn as Function),<dynamic>[impl,tree]);
}

record_add(impl, table_name, records) {
  var method_fn = proto.protocol_method(impl,"xt.db.system.impl_common/ISourceLocal","record_add");
  return Function.apply((method_fn as Function),<dynamic>[impl,table_name,records]);
}

record_delete(impl, table_name, ids) {
  var method_fn = proto.protocol_method(impl,"xt.db.system.impl_common/ISourceLocal","record_delete");
  return Function.apply((method_fn as Function),<dynamic>[impl,table_name,ids]);
}

process_add_event(impl, data) {
  var method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_common/ISourceLocal",
    "process_add_event"
  );
  return Function.apply((method_fn as Function),<dynamic>[impl,data]);
}

process_remove_event(impl, data) {
  var method_fn = proto.protocol_method(
    impl,
    "xt.db.system.impl_common/ISourceLocal",
    "process_remove_event"
  );
  return Function.apply((method_fn as Function),<dynamic>[impl,data]);
}

var ISourceLifecycle = Function.apply((proto.create_protocol_fn as Function),<dynamic>[
  "xt.db.system.impl_common/ISourceLifecycle",
  <dynamic, dynamic>{
  "stop_db":<dynamic, dynamic>{"name":"stop_db","arglist":<dynamic>["impl"]}
}
]);

stop_db(impl) {
  var method_fn = proto.protocol_method(impl,"xt.db.system.impl_common/ISourceLifecycle","stop_db");
  return Function.apply((method_fn as Function),<dynamic>[impl]);
}

add_db_listener_default(impl, listener_id, handle) {
  var listeners = impl["listeners"];
  listeners[listener_id] = handle;
  return listener_id;
}

remove_db_listener_default(impl, listener_id) {
  var listeners = impl["listeners"];
  listeners.remove(listener_id);
  return listener_id;
}

get_db_listener_default(impl, listener_id, handle) {
  var listeners = impl["listeners"];
  return listeners[listener_id];
}

sync_get_tables(payload) {
  var out = <dynamic, dynamic>{};
  var db_sync = payload["db/sync"];
  var db_remove = payload["db/remove"];
  if(("Map" == (db_sync.runtimeType).toString()) || (db_sync.runtimeType).toString().startsWith("_Map") || (db_sync.runtimeType).toString().startsWith("LinkedMap")){
    for(var table in db_sync.keys){
      out[table] = true;
    };
  }
  if(("Map" == (db_remove.runtimeType).toString()) || (db_remove.runtimeType).toString().startsWith("_Map") || (db_remove.runtimeType).toString().startsWith("LinkedMap")){
    for(var table in db_remove.keys){
      out[table] = true;
    };
  }
  return List<dynamic>.from(( out ).keys);
}

sync_notify_listeners(impl, tables, event) {
  var listeners = impl["listeners"];
  for(var entry_42151 in listeners.entries){
    var listener_id = entry_42151.key;
    var handle = entry_42151.value;
    var guard = handle["guard"];
    var callback = handle["callback"];
    var matched = false;
    if((guard.runtimeType).toString().contains("Function") || (guard.runtimeType).toString().contains("=>") || (guard).toString().startsWith("Closure")){
      var arr_42152 = tables;
      for(var i42153 = 0; i42153 < arr_42152.length; ++i42153){
        var table = arr_42152[i42153];
        if((() {
          var dart_truthy__42147 = Function.apply((guard as Function),<dynamic>[table]);
          return (null != dart_truthy__42147) && (false != dart_truthy__42147);
        })()){
          matched = true;
        }
      };
    }
    if(("Map" == (guard.runtimeType).toString()) || (guard.runtimeType).toString().startsWith("_Map") || (guard.runtimeType).toString().startsWith("LinkedMap")){
      var arr_42174 = tables;
      for(var i42175 = 0; i42175 < arr_42174.length; ++i42175){
        var table = arr_42174[i42175];
        var table_guard = guard[table];
        if((table_guard.runtimeType).toString().contains("Function") || (table_guard.runtimeType).toString().contains("=>") || (table_guard).toString().startsWith("Closure")){
          var table_payload = <dynamic, dynamic>{
            "db/sync":xtd.get_in(event,<dynamic>["db/sync",table]),
            "db/remove":xtd.get_in(event,<dynamic>["db/remove",table])
          };
          if((() {
            var dart_truthy__42148 = table_guard(table_payload);
            return (null != dart_truthy__42148) && (false != dart_truthy__42148);
          })()){
            matched = true;
          }
        }
        if(((null != table_guard) && (false != table_guard)) && !((table_guard.runtimeType).toString().contains("Function") || (table_guard.runtimeType).toString().contains("=>") || (table_guard).toString().startsWith("Closure"))){
          matched = true;
        }
      };
    }
    if((null != matched) && (false != matched)){
      callback(event);
    }
  };
  return true;
}

sync_process_payload(impl, payload) {
  var db_sync = payload["db/sync"];
  var db_remove = payload["db/remove"];
  if((("Map" == (db_sync.runtimeType).toString()) || (db_sync.runtimeType).toString().startsWith("_Map") || (db_sync.runtimeType).toString().startsWith("LinkedMap")) && (() {
    var dart_truthy__42145 = xtd.not_emptyp(db_sync);
    return (null != dart_truthy__42145) && (false != dart_truthy__42145);
  })()){
    process_add_event(impl,db_sync);
  }
  if((("Map" == (db_remove.runtimeType).toString()) || (db_remove.runtimeType).toString().startsWith("_Map") || (db_remove.runtimeType).toString().startsWith("LinkedMap")) && (() {
    var dart_truthy__42146 = xtd.not_emptyp(db_remove);
    return (null != dart_truthy__42146) && (false != dart_truthy__42146);
  })()){
    for(var entry_42196 in db_remove.entries){
      var table_name = entry_42196.key;
      var ids = entry_42196.value;
      record_delete(impl,table_name,ids);
    };
  }
  var tables = sync_get_tables(payload);
  if(tables.length > 0){
    sync_notify_listeners(impl,tables,payload);
  }
  return true;
}