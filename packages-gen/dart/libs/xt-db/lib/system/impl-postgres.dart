import 'package:xtalk_lang/common-protocol.dart' as proto;
import 'package:xtalk_db/system/impl-common.dart' as impl_common;
import 'package:xtalk_db/text/sql-call.dart' as sql_call;
import 'package:xtalk_db/text/sql-util.dart' as sql_util;
import 'package:xtalk_db/text/sql-graph.dart' as sql_graph;
import 'package:xtalk_net/conn-sql.dart' as conn_sql;
import 'dart:async';







ImplPostgres(client, schema, lookup, listeners, opts, metadata) {
  (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{})["xt.db.system.impl_postgres/ImplPostgres"] = true;
  proto.register_protocol_impl(
    impl_common.ISourceRemote["on"],
    "xt.db.system.impl_postgres/ImplPostgres",
    <dynamic, dynamic>{"pull_async":pull_async,"rpc_call_async":rpc_call_async}
  );
  proto.register_protocol_impl(impl_common.ISourceListener["on"],"xt.db.system.impl_postgres/ImplPostgres",<dynamic, dynamic>{
    "add_db_listener":impl_common.add_db_listener_default,
    "remove_db_listener":impl_common.remove_db_listener_default,
    "get_db_listener":impl_common.get_db_listener_default
  });
  proto.register_protocol_impl(
    impl_common.ISourceLifecycle["on"],
    "xt.db.system.impl_postgres/ImplPostgres",
    <dynamic, dynamic>{"stop_db":stop_db}
  );
  return <dynamic, dynamic>{
    "::/protocol-impls":<dynamic, dynamic>{
        impl_common.ISourceRemote["on"]:<dynamic, dynamic>{"pull_async":pull_async,"rpc_call_async":rpc_call_async},
        impl_common.ISourceListener["on"]:<dynamic, dynamic>{
          "add_db_listener":impl_common.add_db_listener_default,
          "remove_db_listener":impl_common.remove_db_listener_default,
          "get_db_listener":impl_common.get_db_listener_default
        },
        impl_common.ISourceLifecycle["on"]:<dynamic, dynamic>{"stop_db":stop_db}
      },
    "schema":schema,
    "lookup":lookup,
    "opts":opts,
    "::":"xt.db.system.impl_postgres/ImplPostgres",
    "metadata":metadata,
    "::/protocols":<dynamic>[
        impl_common.ISourceRemote["on"],
        impl_common.ISourceListener["on"],
        impl_common.ISourceLifecycle["on"]
      ],
    "client":client,
    "listeners":listeners
  };
}

pull_async(impl, tree) {
  var client = impl["client"];
  var opts = impl["opts"];
  var schema = impl["schema"];
  return conn_sql.query_async(client,sql_graph.select(schema,tree,opts));
}

rpc_call_async(impl, rpc_spec, args) {
  var client = impl["client"];
  return sql_call.call_raw(client,rpc_spec,args);
}

stop_db(impl) {
  var client = impl["client"];
  conn_sql.disconnect(client);
  return null;
}

impl_postgres(client, schema, lookup) {
  return ImplPostgres(
    client,
    schema,
    lookup,
    <dynamic, dynamic>{},
    sql_util.postgres_opts(lookup),
    <dynamic, dynamic>{}
  );
}

impl_postgres_init(impl) {
  var client = impl["client"];
  var lookup = impl["lookup"];
  var opts = impl["opts"];
  var schema = impl["schema"];
  return ((Future.sync(() => conn_sql.connect(client))) as Future<dynamic>).then((value) async { return await Function.apply((client) {
    return impl;
  },<dynamic>[value]); });
}