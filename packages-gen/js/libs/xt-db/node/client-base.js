const proxy_util = require("@xtalk/db/node/proxy-util.js")

function kernel_init(node,config,schema,lookup,opts){
  return proxy_util.request_client(node,"@xt.db/kernel-init",[config,schema,lookup],opts);
}

function kernel_setup(node,config,schema,lookup,opts){
  return proxy_util.request_client(node,"@xt.db/kernel-setup",[config,schema,lookup],opts);
}

function kernel_teardown(node,config,opts){
  return proxy_util.request_client(node,"@xt.db/kernel-teardown",[config],opts);
}

function subscribe_db(node,primary_id,conn_id,topics,opts){
  return proxy_util.request_client(node,"@xt.db/subscribe-db",[primary_id,conn_id,topics],opts);
}

function unsubscribe_db(node,primary_id,conn_id,topics,opts){
  return proxy_util.request_client(node,"@xt.db/unsubscribe-db",[primary_id,conn_id,topics],opts);
}

function sync_cached(node,primary_id,payload,opts){
  return proxy_util.request_client(node,"@xt.db/sync-cached",[primary_id,payload],opts);
}

function attach_model(node,primary_id,page_args,model_spec,opts){
  return proxy_util.request_client(
    node,
    "@xt.db/attach-model",
    [primary_id,page_args,model_spec],
    opts
  );
}

function detach_model(node,primary_id,page_args,opts){
  return proxy_util.request_client(node,"@xt.db/detach-model",[primary_id,page_args],opts);
}

function rpc_call(node,service_id,rpc_spec,rpc_args,opts){
  return proxy_util.request_client(node,"@xt.db/rpc-call",[service_id,rpc_spec,rpc_args],opts);
}

function rpc_attach_model(node,primary_id,page_args,rpc_spec,model,opts){
  return proxy_util.request_client(
    node,
    "@xt.db/rpc-attach-model",
    [primary_id,page_args,rpc_spec,model],
    opts
  );
}

function pull_call(node,primary_id,tree,opts){
  return proxy_util.request_client(node,"@xt.db/pull-call",[primary_id,tree],opts);
}

function pull_cached(node,primary_id,tree,opts){
  return proxy_util.request_client(node,"@xt.db/pull-cached",[primary_id,tree],opts);
}

function pull_attach_model(node,primary_id,page_args,tree,model,opts){
  return proxy_util.request_client(
    node,
    "@xt.db/pull-attach-model",
    [primary_id,page_args,tree,model],
    opts
  );
}

function dataview_call(node,primary_id,dataview,opts){
  return proxy_util.request_client(node,"@xt.db/dataview-call",[primary_id,dataview],opts);
}

function dataview_cached(node,primary_id,dataview,opts){
  return proxy_util.request_client(node,"@xt.db/dataview-cached",[primary_id,dataview],opts);
}

function dataview_attach_model(node,primary_id,page_args,dataview,model,opts){
  return proxy_util.request_client(
    node,
    "@xt.db/dataview-attach-model",
    [primary_id,page_args,dataview,model],
    opts
  );
}

module.exports = {
  ["kernel_init"]:kernel_init,
  ["kernel_setup"]:kernel_setup,
  ["kernel_teardown"]:kernel_teardown,
  ["subscribe_db"]:subscribe_db,
  ["unsubscribe_db"]:unsubscribe_db,
  ["sync_cached"]:sync_cached,
  ["attach_model"]:attach_model,
  ["detach_model"]:detach_model,
  ["rpc_call"]:rpc_call,
  ["rpc_attach_model"]:rpc_attach_model,
  ["pull_call"]:pull_call,
  ["pull_cached"]:pull_cached,
  ["pull_attach_model"]:pull_attach_model,
  ["dataview_call"]:dataview_call,
  ["dataview_cached"]:dataview_cached,
  ["dataview_attach_model"]:dataview_attach_model
}