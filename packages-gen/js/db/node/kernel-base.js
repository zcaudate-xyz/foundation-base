const xtd = require("@xtalk/lang/common-data.js")

const protocol = require("@xtalk/lang/common-protocol.js")

const base_tree = require("@xtalk/db/text/base-tree.js")

const impl_common = require("@xtalk/db/system/impl-common.js")

const page_core = require("@xtalk/substrate/page-core.js")

const impl_main = require("@xtalk/db/system/main.js")

const substrate = require("@xtalk/substrate/substrate.js")

function get_primary_impl(node,service_id){
  let impl = substrate.get_service(node,service_id);
  let primary_id = xtd.get_in(impl,["metadata","primary_id"]);
  if(primary_id){
    return substrate.get_service(node,primary_id);
  }
  else{
    return impl;
  }
}

function get_caching_impl(node,service_id){
  let impl = substrate.get_service(node,service_id);
  let caching_id = xtd.get_in(impl,["metadata","caching_id"]);
  if(caching_id){
    return substrate.get_service(node,caching_id);
  }
}

function kernel_create_config(config){
  let common = Object.assign({"id":"db/common"},config["common"]);
  let primary = Object.assign({"id":"db/primary"},config["primary"]);
  let caching = Object.assign({"id":"db/caching"},config["caching"]);
  return {"common":common,"primary":primary,"caching":caching};
}

function kernel_check_exists(node,config){
  config = kernel_create_config(config);
  return (null != substrate.get_service(node,xtd.get_in(config,["common","id"]))) && (null != substrate.get_service(node,xtd.get_in(config,["primary","id"]))) && (null != substrate.get_service(node,xtd.get_in(config,["caching","id"])));
}

function kernel_setup_single(node,service_id,type,defaults,schema,lookup){
  return impl_main.create_impl_init(impl_main.create_impl(type,defaults,schema,lookup)).then(function (impl){
    substrate.set_service(node,service_id,impl);
    return node;
  });
}

function kernel_teardown_single(node,service_id){
  let impl = substrate.get_service(node,service_id);
  if(protocol.protocol_implements(impl,"xt.db.system.impl_common/ISourceLifecycle")){
    impl_common.stop_db(impl);
  }
  substrate.remove_service(node,service_id);
  return node;
}

function kernel_setup_main(node,config,schema,lookup){
  config = kernel_create_config(config);
  let common_id = xtd.get_in(config,["common","id"]);
  let primary_id = xtd.get_in(config,["primary","id"]);
  let caching_id = xtd.get_in(config,["caching","id"]);
  substrate.set_service(node,common_id,{
    "config":config,
    "schema":schema,
    "lookup":lookup,
    "metadata":{"common_id":common_id}
  });
  return Promise.all([
    kernel_setup_single(
      node,
      primary_id,
      xtd.get_in(config,["primary","type"]),
      xtd.get_in(config,["primary","defaults"]),
      schema,
      lookup
    ),
    kernel_setup_single(
      node,
      caching_id,
      xtd.get_in(config,["caching","type"]),
      xtd.get_in(config,["caching","defaults"]),
      schema,
      lookup
    )
  ]).then(function (init){
    Object.assign((substrate.get_service(node,primary_id))["metadata"],{
      "common_id":common_id,
      "caching_id":caching_id,
      "caching_fn":function (){
            return substrate.get_service(node,caching_id);
          }
    });
    Object.assign((substrate.get_service(node,caching_id))["metadata"],{
      "common_id":common_id,
      "primary_id":primary_id,
      "primary_fn":function (){
            return substrate.get_service(node,primary_id);
          }
    });
    return {"status":"setup","data":config};
  });
}

function kernel_setup_handler(space,args,request,node){
  let config = args[0];
  let schema = args[1];
  let lookup = args[2];
  return kernel_setup_main(node,config,schema,lookup);
}

function kernel_teardown_main(node,config){
  config = kernel_create_config(config);
  kernel_teardown_single(node,xtd.get_in(config,["primary","id"]));
  kernel_teardown_single(node,xtd.get_in(config,["caching","id"]));
  substrate.remove_service(node,xtd.get_in(config,["common","id"]));
  return {"status":"teardown","data":config};
}

function kernel_teardown_handler(space,args,request,node){
  let config = args[0];
  if("string" == (typeof config)){
    let common_id = xtd.get_in(substrate.get_service(node,config),["metadata","common_id"]);
    config = xtd.get_in(substrate.get_service(node,common_id),["config"]);
  }
  return kernel_teardown_main(node,config);
}

function kernel_init_main(node,config,schema,lookup){
  if(kernel_check_exists(node,config)){
    return {"status":"no_change","data":kernel_create_config(config)};
  }
  else{
    return kernel_setup_main(node,config,schema,lookup);
  }
}

function kernel_init_handler(space,args,request,node){
  let config = args[0];
  let schema = args[1];
  let lookup = args[2];
  return kernel_init_main(node,config,schema,lookup);
}

function subscribe_db_handler(space,args,request,node){
  let primary_id = args[0];
  let conn_id = args[1];
  let topics = args[2];
  let primary = get_primary_impl(node,primary_id);
  return impl_common.subscribe_db(primary,conn_id,topics);
}

function unsubscribe_db_handler(space,args,request,node){
  let primary_id = args[0];
  let conn_id = args[1];
  let topics = args[2];
  let primary = get_primary_impl(node,primary_id);
  return impl_common.unsubscribe_db(primary,conn_id,topics);
}

function sync_cached_handler(space,args,request,node){
  let primary_id = args[0];
  let payload = args[1];
  let caching = get_caching_impl(node,primary_id);
  return impl_common.sync_process_payload(caching,payload);
}

function attach_base_model(node,primary_id,space_id,group_id,model_id,model_spec){
  page_core.group_add_attach(node,space_id,group_id,{[model_id]:model_spec});
  let refresh_map = xtd.get_in(model_spec,["options","refresh"]);
  let caching = get_caching_impl(node,primary_id);
  if(caching && ((null != refresh_map) && ("object" == (typeof refresh_map)) && !Array.isArray(refresh_map))){
    impl_common.add_db_listener(caching,space_id + "/" + group_id + "/" + model_id,{
      "guard":refresh_map,
      "callback":function (event){
            return page_core.model_update(node,space_id,group_id,model_id,event);
          }
    });
  }
  return {
    "status":"attached",
    "space":space_id,
    "group":group_id,
    "model":model_id
  };
}

function attach_model_handler(space,args,request,node){
  let primary_id = args[0];
  let page_args = args[1];
  let {group_id,model_id,space_id} = page_args;
  let model_spec = args[2];
  return attach_base_model(node,primary_id,space_id,group_id,model_id,model_spec);
}

function detach_base_model(node,primary_id,space_id,group_id,model_id){
  page_core.model_remove(node,space_id,group_id,model_id);
  let caching = get_caching_impl(node,primary_id);
  if(null != caching){
    impl_common.remove_db_listener(caching,space_id + "/" + group_id + "/" + model_id);
  }
  return {
    "status":"removed",
    "space":space_id,
    "group":group_id,
    "model":model_id
  };
}

function detach_model_handler(space,args,request,node){
  let primary_id = args[0];
  let page_args = args[1];
  let {group_id,model_id,space_id} = page_args;
  return detach_base_model(node,primary_id,space_id,group_id,model_id);
}

function rpc_call_baseline_fn(node,primary_id,rpc_spec,rpc_args){
  let primary = get_primary_impl(node,primary_id);
  return impl_common.rpc_call_async(primary,rpc_spec,rpc_args).then(function (result){
    let {table} = rpc_spec;
    let caching = get_caching_impl(node,primary_id);
    if(table && caching){
      let {base,type} = table;
      impl_common.sync_process_payload(caching,{[type]:{[base]:result}});
    }
    return result;
  });
}

function rpc_call_handler(space,args,request,node){
  let service_id = args[0];
  let rpc_spec = args[1];
  let rpc_args = args[2];
  return rpc_call_baseline_fn(node,service_id,rpc_spec,rpc_args);
}

function rpc_create_model(primary_id,rpc_spec,model){
  let {defaults,options,pipeline} = model;
  let rpc_handler = function (context){
    let {args,node,space} = context;
    return rpc_call_baseline_fn(node,primary_id,rpc_spec,args);
  };
  return {
    "handler":rpc_handler,
    "pipeline":xtd.obj_assign_nested({
        "remote":{
            "handler":function (context){
                let node = context["node"];
                let args = context["args"];
                return rpc_call_baseline_fn(node,primary_id,rpc_spec,args);
              }
          }
      },pipeline),
    "defaults":defaults,
    "options":options
  };
}

function rpc_attach_model(space,args,request,node){
  let primary_id = args[0];
  let page_args = args[1];
  let rpc_spec = args[2];
  let model = args[3];
  let {group_id,model_id,space_id} = page_args;
  let model_spec = rpc_create_model(primary_id,rpc_spec,model);
  return attach_base_model(node,primary_id,space_id,group_id,model_id,model_spec);
}

function pull_call_baseline_fn(node,primary_id,tree){
  let primary = get_primary_impl(node,primary_id);
  return impl_common.pull_async(primary,tree).then(function (result){
    let table = tree[0];
    let caching = get_caching_impl(node,primary_id);
    if(caching){
      let payload = {"db/sync":{[table]:result}};
      impl_common.sync_process_payload(caching,payload);
    }
    return result;
  });
}

function pull_call_handler(space,args,request,node){
  let primary_id = args[0];
  let tree = args[1];
  return pull_call_baseline_fn(node,primary_id,tree);
}

function pull_cached_handler(space,args,request,node){
  let primary_id = args[0];
  let tree = args[1];
  let caching = get_caching_impl(node,primary_id);
  return impl_common.pull(caching,tree);
}

function pull_create_model(primary_id,tree,model){
  let {defaults,options,pipeline} = model;
  let table = tree[0];
  return {
    "handler":function (context){
        let node = context["node"];
        let caching = get_caching_impl(node,primary_id);
        return impl_common.pull(caching,tree);
      },
    "pipeline":xtd.obj_assign_nested({
        "remote":{
            "handler":function (context){
                let node = context["node"];
                return pull_call_baseline_fn(node,primary_id,tree);
              }
          }
      },pipeline),
    "defaults":defaults,
    "options":options
  };
}

function pull_attach_model(space,args,request,node){
  let primary_id = args[0];
  let page_args = args[1];
  let tree = args[2];
  let model = args[3];
  let {group_id,model_id,space_id} = page_args;
  let model_spec = pull_create_model(primary_id,tree,model);
  return attach_base_model(node,primary_id,space_id,group_id,model_id,model_spec);
}

function dataview_prep_tree(impl,dataview){
  let {schema} = impl;
  let [ok,tree] = base_tree.plan_view(
    schema,
    Object.assign({"select_args":[],"return_args":[]},dataview)
  );
  if(!ok){
    throw Object.assign(new Error("Invalid Dataview"),{"data":dataview});
  }
  return tree;
}

function dataview_call_baseline_fn(node,primary_id,dataview){
  let impl = get_primary_impl(node,primary_id);
  let tree = dataview_prep_tree(impl,dataview);
  return impl_common.pull_async(impl,tree).then(function (result){
    let {table} = dataview;
    let caching = get_caching_impl(node,primary_id);
    if(caching){
      let payload = {"db/sync":{[table]:result}};
      impl_common.sync_process_payload(caching,payload);
    }
    return result;
  });
}

function dataview_call_handler(space,args,request,node){
  let primary_id = args[0];
  let dataview = args[1];
  return dataview_call_baseline_fn(node,primary_id,dataview);
}

function dataview_cached_handler(space,args,request,node){
  let primary_id = args[0];
  let dataview = args[1];
  let caching = get_caching_impl(node,primary_id);
  let tree = dataview_prep_tree(caching,dataview);
  return impl_common.pull(caching,tree);
}

function dataview_create_model(primary_id,dataview,model){
  let {defaults,options,pipeline} = model;
  let {table} = dataview;
  return {
    "handler":function (context){
        let node = context["node"];
        let args = context["args"];
        let caching = get_caching_impl(node,primary_id);
        let {schema} = caching;
        let [ok,tree] = base_tree.plan_view(schema,Object.assign(
          Object.assign({"select_args":[],"return_args":[]},args[0]),
          dataview
        ));
        if(!ok){
          throw Object.assign(new Error("Invalid Dataview"),{"data":dataview});
        }
        return impl_common.pull(caching,tree);
      },
    "pipeline":xtd.obj_assign_nested({
        "remote":{
            "handler":function (context){
                let node = context["node"];
                let args = context["args"];
                return dataview_call_baseline_fn(
                  node,
                  primary_id,
                  Object.assign(Object.assign({},dataview),args[0])
                );
              }
          }
      },pipeline),
    "defaults":defaults,
    "options":options
  };
}

function dataview_attach_model(space,args,request,node){
  let primary_id = args[0];
  let page_args = args[1];
  let dataview = args[2];
  let model = args[3];
  let {group_id,model_id,space_id} = page_args;
  let model_spec = dataview_create_model(primary_id,dataview,model);
  return attach_base_model(node,primary_id,space_id,group_id,model_id,model_spec);
}

function init_handlers(node){
  substrate.register_handler(node,"@xt.db/kernel-init",kernel_init_handler);
  substrate.register_handler(node,"@xt.db/kernel-setup",kernel_setup_handler);
  substrate.register_handler(node,"@xt.db/kernel-teardown",kernel_teardown_handler);
  substrate.register_handler(node,"@xt.db/subscribe-db",subscribe_db_handler,null);
  substrate.register_handler(node,"@xt.db/unsubscribe-db",unsubscribe_db_handler,null);
  substrate.register_handler(node,"@xt.db/sync-cached",sync_cached_handler,null);
  substrate.register_handler(node,"@xt.db/attach-model",attach_model_handler,null);
  substrate.register_handler(node,"@xt.db/detach-model",detach_model_handler,null);
  substrate.register_handler(node,"@xt.db/rpc-call",rpc_call_handler,null);
  substrate.register_handler(node,"@xt.db/rpc-attach-model",rpc_attach_model,null);
  substrate.register_handler(node,"@xt.db/pull-call",pull_call_handler,null);
  substrate.register_handler(node,"@xt.db/pull-cached",pull_cached_handler,null);
  substrate.register_handler(node,"@xt.db/pull-attach-model",pull_attach_model,null);
  substrate.register_handler(node,"@xt.db/dataview-call",dataview_call_handler,null);
  substrate.register_handler(node,"@xt.db/dataview-cached",dataview_cached_handler,null);
  substrate.register_handler(node,"@xt.db/dataview-attach-model",dataview_attach_model,null);
  return node;
}

module.exports = {
  ["get_primary_impl"]:get_primary_impl,
  ["get_caching_impl"]:get_caching_impl,
  ["kernel_create_config"]:kernel_create_config,
  ["kernel_check_exists"]:kernel_check_exists,
  ["kernel_setup_single"]:kernel_setup_single,
  ["kernel_teardown_single"]:kernel_teardown_single,
  ["kernel_setup_main"]:kernel_setup_main,
  ["kernel_setup_handler"]:kernel_setup_handler,
  ["kernel_teardown_main"]:kernel_teardown_main,
  ["kernel_teardown_handler"]:kernel_teardown_handler,
  ["kernel_init_main"]:kernel_init_main,
  ["kernel_init_handler"]:kernel_init_handler,
  ["subscribe_db_handler"]:subscribe_db_handler,
  ["unsubscribe_db_handler"]:unsubscribe_db_handler,
  ["sync_cached_handler"]:sync_cached_handler,
  ["attach_base_model"]:attach_base_model,
  ["attach_model_handler"]:attach_model_handler,
  ["detach_base_model"]:detach_base_model,
  ["detach_model_handler"]:detach_model_handler,
  ["rpc_call_baseline_fn"]:rpc_call_baseline_fn,
  ["rpc_call_handler"]:rpc_call_handler,
  ["rpc_create_model"]:rpc_create_model,
  ["rpc_attach_model"]:rpc_attach_model,
  ["pull_call_baseline_fn"]:pull_call_baseline_fn,
  ["pull_call_handler"]:pull_call_handler,
  ["pull_cached_handler"]:pull_cached_handler,
  ["pull_create_model"]:pull_create_model,
  ["pull_attach_model"]:pull_attach_model,
  ["dataview_prep_tree"]:dataview_prep_tree,
  ["dataview_call_baseline_fn"]:dataview_call_baseline_fn,
  ["dataview_call_handler"]:dataview_call_handler,
  ["dataview_cached_handler"]:dataview_cached_handler,
  ["dataview_create_model"]:dataview_create_model,
  ["dataview_attach_model"]:dataview_attach_model,
  ["init_handlers"]:init_handlers
}