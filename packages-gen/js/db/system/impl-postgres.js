const proto = require("@xtalk/lang/common-protocol.js")

const impl_common = require("@xtalk/db/system/impl-common.js")

const sql_call = require("@xtalk/db/text/sql-call.js")

const sql_util = require("@xtalk/db/text/sql-util.js")

const sql_graph = require("@xtalk/db/text/sql-graph.js")

const conn_sql = require("@xtalk/net/conn-sql.js")

function ImplPostgres(client,schema,lookup,listeners,opts,metadata){
  if(!globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["xt.db.system.impl_postgres/ImplPostgres"]){
    globalThis["xt_lang_common_protocol$$IMPLEMENTATIONS"]["xt.db.system.impl_postgres/ImplPostgres"] = true;
    proto.register_protocol_impl(
      impl_common.ISourceRemote["on"],
      "xt.db.system.impl_postgres/ImplPostgres",
      {"pull_async":pull_async,"rpc_call_async":rpc_call_async}
    );
    proto.register_protocol_impl(impl_common.ISourceListener["on"],"xt.db.system.impl_postgres/ImplPostgres",{
      "add_db_listener":impl_common.add_db_listener_default,
      "remove_db_listener":impl_common.remove_db_listener_default,
      "get_db_listener":impl_common.get_db_listener_default
    });
    proto.register_protocol_impl(
      impl_common.ISourceLifecycle["on"],
      "xt.db.system.impl_postgres/ImplPostgres",
      {"stop_db":stop_db}
    );
  }
  return {
    "::":"xt.db.system.impl_postgres/ImplPostgres",
    "::/protocols":[
        impl_common.ISourceRemote["on"],
        impl_common.ISourceListener["on"],
        impl_common.ISourceLifecycle["on"]
      ],
    "client":client,
    "schema":schema,
    "lookup":lookup,
    "listeners":listeners,
    "opts":opts,
    "metadata":metadata
  };
}

function pull_async(impl,tree){
  let {client,opts,schema} = impl;
  return conn_sql.query_async(client,sql_graph.select(schema,tree,opts));
}

function rpc_call_async(impl,rpc_spec,args){
  let {client} = impl;
  return sql_call.call_raw(client,rpc_spec,args);
}

function stop_db(impl){
  let {client} = impl;
  conn_sql.disconnect(client);
  return null;
}

function impl_postgres(client,schema,lookup){
  return ImplPostgres(client,schema,lookup,{},sql_util.postgres_opts(lookup),{});
}

function impl_postgres_init(impl){
  let {client,lookup,opts,schema} = impl;
  return conn_sql.connect(client).then(function (client){
    return impl;
  });
}

module.exports = {
  ["pull_async"]:pull_async,
  ["rpc_call_async"]:rpc_call_async,
  ["stop_db"]:stop_db,
  ["ImplPostgres"]:ImplPostgres,
  ["impl_postgres"]:impl_postgres,
  ["impl_postgres_init"]:impl_postgres_init
}