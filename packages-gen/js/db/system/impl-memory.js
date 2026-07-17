const proto = require("@xtalk/lang/common-protocol.js")

const graph = require("@xtalk/db/system/memory-graph.js")

const util = require("@xtalk/db/system/memory-util.js")

const impl_common = require("@xtalk/db/system/impl-common.js")

const f = require("@xtalk/db/text/base-flatten.js")

function ImplMemory(rows,schema,lookup,listeners,metadata){
  if(!globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["xt.db.system.impl_memory/ImplMemory"]){
    globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["xt.db.system.impl_memory/ImplMemory"] = true;
    proto.register_protocol_impl(impl_common.ISourceLocal["on"],"xt.db.system.impl_memory/ImplMemory",{
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
      {"pull_async":pull_async,"rpc_call_async":rpc_call_async}
    );
    proto.register_protocol_impl(impl_common.ISourceListener["on"],"xt.db.system.impl_memory/ImplMemory",{
      "add_db_listener":impl_common.add_db_listener_default,
      "remove_db_listener":impl_common.remove_db_listener_default,
      "get_db_listener":impl_common.get_db_listener_default
    });
  }
  return {
    "::":"xt.db.system.impl_memory/ImplMemory",
    "::/protocols":[
        impl_common.ISourceLocal["on"],
        impl_common.ISourceRemote["on"],
        impl_common.ISourceListener["on"]
      ],
    "rows":rows,
    "schema":schema,
    "lookup":lookup,
    "listeners":listeners,
    "metadata":metadata
  };
}

function pull(impl,tree){
  let {opts,rows,schema} = impl;
  return graph.pull(rows,schema,tree,opts);
}

function pull_async(impl,tree){
  return Promise.resolve().then(function (){
    return pull(impl,tree);
  });
}

function record_add(impl,table_name,records){
  let {opts,rows,schema} = impl;
  return util.add_bulk(rows,schema,{[table_name]:records});
}

function record_delete(impl,table_name,ids){
  let {opts,rows,schema} = impl;
  return util.remove_bulk(rows,schema,table_name,ids);
}

function process_add_event(impl,data){
  let {rows,schema} = impl;
  return util.add_bulk(rows,schema,data);
}

function process_remove_event(impl,data){
  let {lookup,rows,schema} = impl;
  let ordered = f.flatten_bulk_ids(schema,lookup,data);
  for(let entry of ordered){
    let [table_name,ids] = entry;
    util.remove_bulk(rows,schema,table_name,ids);
  };
  return ordered.map(function (arr){
    return arr[0];
  });
}

function clear_db(impl){
  let {rows} = impl;
  for(let table_key of Object.keys(rows)){
    delete(rows[table_key]);
  };
  return null;
}

function rpc_call_async(_impl,_rpc_spec,_args){
  throw "db.impl.memory does not support rpc_call_async";
}

function impl_memory(schema,lookup){
  return ImplMemory({},schema,lookup,{},{});
}

module.exports = {
  ["pull"]:pull,
  ["pull_async"]:pull_async,
  ["record_add"]:record_add,
  ["record_delete"]:record_delete,
  ["process_add_event"]:process_add_event,
  ["process_remove_event"]:process_remove_event,
  ["clear_db"]:clear_db,
  ["rpc_call_async"]:rpc_call_async,
  ["ImplMemory"]:ImplMemory,
  ["impl_memory"]:impl_memory
}