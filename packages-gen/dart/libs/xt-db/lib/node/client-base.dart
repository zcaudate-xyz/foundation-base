import 'package:xtalk_db/node/proxy-util.dart' as proxy_util;


kernel_init(node, config, schema, lookup, opts) {
  return proxy_util.request_client(node,"@xt.db/kernel-init",<dynamic>[config,schema,lookup],opts);
}

kernel_setup(node, config, schema, lookup, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/kernel-setup",
    <dynamic>[config,schema,lookup],
    opts
  );
}

kernel_teardown(node, config, opts) {
  return proxy_util.request_client(node,"@xt.db/kernel-teardown",<dynamic>[config],opts);
}

subscribe_db(node, primary_id, conn_id, topics, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/subscribe-db",
    <dynamic>[primary_id,conn_id,topics],
    opts
  );
}

unsubscribe_db(node, primary_id, conn_id, topics, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/unsubscribe-db",
    <dynamic>[primary_id,conn_id,topics],
    opts
  );
}

sync_cached(node, primary_id, payload, opts) {
  return proxy_util.request_client(node,"@xt.db/sync-cached",<dynamic>[primary_id,payload],opts);
}

attach_model(node, primary_id, page_args, model_spec, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/attach-model",
    <dynamic>[primary_id,page_args,model_spec],
    opts
  );
}

detach_model(node, primary_id, page_args, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/detach-model",
    <dynamic>[primary_id,page_args],
    opts
  );
}

rpc_call(node, service_id, rpc_spec, rpc_args, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/rpc-call",
    <dynamic>[service_id,rpc_spec,rpc_args],
    opts
  );
}

rpc_attach_model(node, primary_id, page_args, rpc_spec, model, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/rpc-attach-model",
    <dynamic>[primary_id,page_args,rpc_spec,model],
    opts
  );
}

pull_call(node, primary_id, tree, opts) {
  return proxy_util.request_client(node,"@xt.db/pull-call",<dynamic>[primary_id,tree],opts);
}

pull_cached(node, primary_id, tree, opts) {
  return proxy_util.request_client(node,"@xt.db/pull-cached",<dynamic>[primary_id,tree],opts);
}

pull_attach_model(node, primary_id, page_args, tree, model, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/pull-attach-model",
    <dynamic>[primary_id,page_args,tree,model],
    opts
  );
}

dataview_call(node, primary_id, dataview, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/dataview-call",
    <dynamic>[primary_id,dataview],
    opts
  );
}

dataview_cached(node, primary_id, dataview, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/dataview-cached",
    <dynamic>[primary_id,dataview],
    opts
  );
}

dataview_attach_model(node, primary_id, page_args, dataview, model, opts) {
  return proxy_util.request_client(
    node,
    "@xt.db/dataview-attach-model",
    <dynamic>[primary_id,page_args,dataview,model],
    opts
  );
}