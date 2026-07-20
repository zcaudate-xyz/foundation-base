import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_lang/common-protocol.dart' as protocol;
import 'package:xtalk_db/text/base-tree.dart' as base_tree;
import 'package:xtalk_db/system/impl-common.dart' as impl_common;
import 'package:xtalk_substrate/page-core.dart' as page_core;
import 'package:xtalk_db/system/main.dart' as impl_main;
import 'package:xtalk_substrate/substrate.dart' as substrate;
import 'dart:async';








get_primary_impl(node, service_id) {
  var impl = substrate.get_service(node,service_id);
  var primary_id = xtd.get_in(impl,<dynamic>["metadata","primary_id"]);
  if((null != primary_id) && (false != primary_id)){
    return substrate.get_service(node,primary_id);
  }
  else{
    return impl;
  }
}

get_caching_impl(node, service_id) {
  var impl = substrate.get_service(node,service_id);
  var caching_id = xtd.get_in(impl,<dynamic>["metadata","caching_id"]);
  if((null != caching_id) && (false != caching_id)){
    return substrate.get_service(node,caching_id);
  }
}

kernel_create_config(config) {
  var common = xtd.obj_assign(<dynamic, dynamic>{"id":"db/common"},config["common"]);
  var primary = xtd.obj_assign(<dynamic, dynamic>{"id":"db/primary"},config["primary"]);
  var caching = xtd.obj_assign(<dynamic, dynamic>{"id":"db/caching"},config["caching"]);
  return <dynamic, dynamic>{"common":common,"primary":primary,"caching":caching};
}

kernel_check_exists(node, config) {
  config = kernel_create_config(config);
  return (null != substrate.get_service(node,xtd.get_in(config,<dynamic>["common","id"]))) && (null != substrate.get_service(node,xtd.get_in(config,<dynamic>["primary","id"]))) && (null != substrate.get_service(node,xtd.get_in(config,<dynamic>["caching","id"])));
}

kernel_setup_single(node, service_id, type, defaults, schema, lookup) {
  return ((Future.sync(() => impl_main.create_impl_init(impl_main.create_impl(type,defaults,schema,lookup)))) as Future<dynamic>).then((value) async { return await Function.apply((impl) {
    substrate.set_service(node,service_id,impl);
    return node;
  },<dynamic>[value]); });
}

kernel_teardown_single(node, service_id) {
  var impl = substrate.get_service(node,service_id);
  if((() {
    var dart_truthy__52671 = protocol.protocol_implements(impl,"xt.db.system.impl_common/ISourceLifecycle");
    return (null != dart_truthy__52671) && (false != dart_truthy__52671);
  })()){
    impl_common.stop_db(impl);
  }
  substrate.remove_service(node,service_id);
  return node;
}

kernel_setup_main(node, config, schema, lookup) {
  config = kernel_create_config(config);
  var common_id = xtd.get_in(config,<dynamic>["common","id"]);
  var primary_id = xtd.get_in(config,<dynamic>["primary","id"]);
  var caching_id = xtd.get_in(config,<dynamic>["caching","id"]);
  substrate.set_service(node,common_id,<dynamic, dynamic>{
    "config":config,
    "schema":schema,
    "lookup":lookup,
    "metadata":<dynamic, dynamic>{"common_id":common_id}
  });
  return ((Future.sync(() => Future.wait(List<Future<dynamic>>.from(( <dynamic>[
    kernel_setup_single(
    node,
    primary_id,
    xtd.get_in(config,<dynamic>["primary","type"]),
    xtd.get_in(config,<dynamic>["primary","defaults"]),
    schema,
    lookup
  ),
    kernel_setup_single(
    node,
    caching_id,
    xtd.get_in(config,<dynamic>["caching","type"]),
    xtd.get_in(config,<dynamic>["caching","defaults"]),
    schema,
    lookup
  )
  ] ).map((entry) => Future.sync(() => entry)))))) as Future<dynamic>).then((value) async { return await Function.apply((init) {
    xtd.obj_assign((substrate.get_service(node,primary_id))["metadata"],<dynamic, dynamic>{
      "common_id":common_id,
      "caching_id":caching_id,
      "caching_fn":() {
            return substrate.get_service(node,caching_id);
          }
    });
    xtd.obj_assign((substrate.get_service(node,caching_id))["metadata"],<dynamic, dynamic>{
      "common_id":common_id,
      "primary_id":primary_id,
      "primary_fn":() {
            return substrate.get_service(node,primary_id);
          }
    });
    return <dynamic, dynamic>{"status":"setup","data":config};
  },<dynamic>[value]); });
}

kernel_setup_handler(space, args, request, node) {
  var config = args[0];
  var schema = args[1];
  var lookup = args[2];
  return kernel_setup_main(node,config,schema,lookup);
}

kernel_teardown_main(node, config) {
  config = kernel_create_config(config);
  kernel_teardown_single(node,xtd.get_in(config,<dynamic>["primary","id"]));
  kernel_teardown_single(node,xtd.get_in(config,<dynamic>["caching","id"]));
  substrate.remove_service(node,xtd.get_in(config,<dynamic>["common","id"]));
  return <dynamic, dynamic>{"status":"teardown","data":config};
}

kernel_teardown_handler(space, args, request, node) {
  var config = args[0];
  if("String" == (config.runtimeType).toString()){
    var common_id = xtd.get_in(
      substrate.get_service(node,config),
      <dynamic>["metadata","common_id"]
    );
    config = xtd.get_in(substrate.get_service(node,common_id),<dynamic>["config"]);
  }
  return kernel_teardown_main(node,config);
}

kernel_init_main(node, config, schema, lookup) {
  if((() {
    var dart_truthy__52670 = kernel_check_exists(node,config);
    return (null != dart_truthy__52670) && (false != dart_truthy__52670);
  })()){
    return <dynamic, dynamic>{"status":"no_change","data":kernel_create_config(config)};
  }
  else{
    return kernel_setup_main(node,config,schema,lookup);
  }
}

kernel_init_handler(space, args, request, node) {
  var config = args[0];
  var schema = args[1];
  var lookup = args[2];
  return kernel_init_main(node,config,schema,lookup);
}

subscribe_db_handler(space, args, request, node) {
  var primary_id = args[0];
  var conn_id = args[1];
  var topics = args[2];
  var primary = get_primary_impl(node,primary_id);
  return impl_common.subscribe_db(primary,conn_id,topics);
}

unsubscribe_db_handler(space, args, request, node) {
  var primary_id = args[0];
  var conn_id = args[1];
  var topics = args[2];
  var primary = get_primary_impl(node,primary_id);
  return impl_common.unsubscribe_db(primary,conn_id,topics);
}

sync_cached_handler(space, args, request, node) {
  var primary_id = args[0];
  var payload = args[1];
  var caching = get_caching_impl(node,primary_id);
  return impl_common.sync_process_payload(caching,payload);
}

attach_base_model(node, primary_id, space_id, group_id, model_id, model_spec) {
  page_core.group_add_attach(node,space_id,group_id,<dynamic, dynamic>{model_id:model_spec});
  var refresh_map = xtd.get_in(model_spec,<dynamic>["options","refresh"]);
  var caching = get_caching_impl(node,primary_id);
  if(((null != caching) && (false != caching)) && (("Map" == (refresh_map.runtimeType).toString()) || (refresh_map.runtimeType).toString().startsWith("_Map") || (refresh_map.runtimeType).toString().startsWith("LinkedMap"))){
    impl_common.add_db_listener(caching,space_id + "/" + group_id + "/" + model_id,<dynamic, dynamic>{
      "guard":refresh_map,
      "callback":(event) {
            return page_core.model_update(node,space_id,group_id,model_id,event);
          }
    });
  }
  return <dynamic, dynamic>{
    "status":"attached",
    "space":space_id,
    "group":group_id,
    "model":model_id
  };
}

attach_model_handler(space, args, request, node) {
  var primary_id = args[0];
  var page_args = args[1];
  var group_id = page_args["group_id"];
  var model_id = page_args["model_id"];
  var space_id = page_args["space_id"];
  var model_spec = args[2];
  return attach_base_model(node,primary_id,space_id,group_id,model_id,model_spec);
}

detach_base_model(node, primary_id, space_id, group_id, model_id) {
  page_core.model_remove(node,space_id,group_id,model_id);
  var caching = get_caching_impl(node,primary_id);
  if(null != caching){
    impl_common.remove_db_listener(caching,space_id + "/" + group_id + "/" + model_id);
  }
  return <dynamic, dynamic>{
    "status":"removed",
    "space":space_id,
    "group":group_id,
    "model":model_id
  };
}

detach_model_handler(space, args, request, node) {
  var primary_id = args[0];
  var page_args = args[1];
  var group_id = page_args["group_id"];
  var model_id = page_args["model_id"];
  var space_id = page_args["space_id"];
  return detach_base_model(node,primary_id,space_id,group_id,model_id);
}

rpc_call_baseline_fn(node, primary_id, rpc_spec, rpc_args) {
  var primary = get_primary_impl(node,primary_id);
  return ((Future.sync(() => impl_common.rpc_call_async(primary,rpc_spec,rpc_args))) as Future<dynamic>).then((value) async { return await Function.apply((result) {
    var table = rpc_spec["table"];
    var caching = get_caching_impl(node,primary_id);
    if(((null != table) && (false != table)) && ((null != caching) && (false != caching))){
      var base = table["base"];
      var type = table["type"];
      impl_common.sync_process_payload(
        caching,
        <dynamic, dynamic>{type:<dynamic, dynamic>{base:result}}
      );
    }
    return result;
  },<dynamic>[value]); });
}

rpc_call_handler(space, args, request, node) {
  var service_id = args[0];
  var rpc_spec = args[1];
  var rpc_args = args[2];
  return Function.apply(
    (rpc_call_baseline_fn as Function),
    <dynamic>[node,service_id,rpc_spec,rpc_args]
  );
}

rpc_create_model(primary_id, rpc_spec, model) {
  var defaults = model["defaults"];
  var options = model["options"];
  var pipeline = model["pipeline"];
  var rpc_handler = (context) {
    var args = context["args"];
    var node = context["node"];
    var space = context["space"];
    return Function.apply(
      (rpc_call_baseline_fn as Function),
      <dynamic>[node,primary_id,rpc_spec,args]
    );
  };
  return <dynamic, dynamic>{
    "handler":rpc_handler,
    "pipeline":xtd.obj_assign_nested(<dynamic, dynamic>{
        "remote":<dynamic, dynamic>{
            "handler":(context) {
                var node = context["node"];
                var args = context["args"];
                return Function.apply(
                  (rpc_call_baseline_fn as Function),
                  <dynamic>[node,primary_id,rpc_spec,args]
                );
              }
          }
      },pipeline),
    "defaults":defaults,
    "options":options
  };
}

rpc_attach_model(space, args, request, node) {
  var primary_id = args[0];
  var page_args = args[1];
  var rpc_spec = args[2];
  var model = args[3];
  var group_id = page_args["group_id"];
  var model_id = page_args["model_id"];
  var space_id = page_args["space_id"];
  var model_spec = rpc_create_model(primary_id,rpc_spec,model);
  return attach_base_model(node,primary_id,space_id,group_id,model_id,model_spec);
}

pull_call_baseline_fn(node, primary_id, tree) {
  var primary = get_primary_impl(node,primary_id);
  return ((Future.sync(() => impl_common.pull_async(primary,tree))) as Future<dynamic>).then((value) async { return await Function.apply((result) {
    var table = tree[0];
    var caching = get_caching_impl(node,primary_id);
    if((null != caching) && (false != caching)){
      var payload = <dynamic, dynamic>{"db/sync":<dynamic, dynamic>{table:result}};
      impl_common.sync_process_payload(caching,payload);
    }
    return result;
  },<dynamic>[value]); });
}

pull_call_handler(space, args, request, node) {
  var primary_id = args[0];
  var tree = args[1];
  return Function.apply(
    (pull_call_baseline_fn as Function),
    <dynamic>[node,primary_id,tree]
  );
}

pull_cached_handler(space, args, request, node) {
  var primary_id = args[0];
  var tree = args[1];
  var caching = get_caching_impl(node,primary_id);
  return impl_common.pull(caching,tree);
}

pull_create_model(primary_id, tree, model) {
  var defaults = model["defaults"];
  var options = model["options"];
  var pipeline = model["pipeline"];
  var table = tree[0];
  return <dynamic, dynamic>{
    "handler":(context) {
        var node = context["node"];
        var caching = get_caching_impl(node,primary_id);
        return impl_common.pull(caching,tree);
      },
    "pipeline":xtd.obj_assign_nested(<dynamic, dynamic>{
        "remote":<dynamic, dynamic>{
            "handler":(context) {
                var node = context["node"];
                return Function.apply(
                  (pull_call_baseline_fn as Function),
                  <dynamic>[node,primary_id,tree]
                );
              }
          }
      },pipeline),
    "defaults":defaults,
    "options":options
  };
}

pull_attach_model(space, args, request, node) {
  var primary_id = args[0];
  var page_args = args[1];
  var tree = args[2];
  var model = args[3];
  var group_id = page_args["group_id"];
  var model_id = page_args["model_id"];
  var space_id = page_args["space_id"];
  var model_spec = pull_create_model(primary_id,tree,model);
  return attach_base_model(node,primary_id,space_id,group_id,model_id,model_spec);
}

dataview_prep_tree(impl, dataview) {
  var schema = impl["schema"];
  var value_52674 = base_tree.plan_view(schema,xtd.obj_assign(
    <dynamic, dynamic>{"select_args":<dynamic>[],"return_args":<dynamic>[]},
    dataview
  ));
  var ok = value_52674[0];
  var tree = value_52674[1];
  if(!((null != ok) && (false != ok))){
    throw <dynamic, dynamic>{
      "__type__":"xt.exception",
      "message":"Invalid Dataview",
      "data":dataview
    };
  }
  return tree;
}

dataview_call_baseline_fn(node, primary_id, dataview) {
  var impl = get_primary_impl(node,primary_id);
  var tree = dataview_prep_tree(impl,dataview);
  return ((Future.sync(() => impl_common.pull_async(impl,tree))) as Future<dynamic>).then((value) async { return await Function.apply((result) {
    var table = dataview["table"];
    var caching = get_caching_impl(node,primary_id);
    if((null != caching) && (false != caching)){
      var payload = <dynamic, dynamic>{"db/sync":<dynamic, dynamic>{table:result}};
      impl_common.sync_process_payload(caching,payload);
    }
    return result;
  },<dynamic>[value]); });
}

dataview_call_handler(space, args, request, node) {
  var primary_id = args[0];
  var dataview = args[1];
  return Function.apply(
    (dataview_call_baseline_fn as Function),
    <dynamic>[node,primary_id,dataview]
  );
}

dataview_cached_handler(space, args, request, node) {
  var primary_id = args[0];
  var dataview = args[1];
  var caching = get_caching_impl(node,primary_id);
  var tree = dataview_prep_tree(caching,dataview);
  return impl_common.pull(caching,tree);
}

dataview_create_model(primary_id, dataview, model) {
  var defaults = model["defaults"];
  var options = model["options"];
  var pipeline = model["pipeline"];
  var table = dataview["table"];
  return <dynamic, dynamic>{
    "handler":(context) {
        var node = context["node"];
        var args = context["args"];
        var caching = get_caching_impl(node,primary_id);
        var schema = caching["schema"];
        var value_52675 = base_tree.plan_view(schema,xtd.obj_assign(xtd.obj_assign(
          <dynamic, dynamic>{"select_args":<dynamic>[],"return_args":<dynamic>[]},
          args[0]
        ),dataview));
        var ok = value_52675[0];
        var tree = value_52675[1];
        if(!((null != ok) && (false != ok))){
          throw <dynamic, dynamic>{
            "__type__":"xt.exception",
            "message":"Invalid Dataview",
            "data":dataview
          };
        }
        return impl_common.pull(caching,tree);
      },
    "pipeline":xtd.obj_assign_nested(<dynamic, dynamic>{
        "remote":<dynamic, dynamic>{
            "handler":(context) {
                var node = context["node"];
                var args = context["args"];
                return Function.apply((dataview_call_baseline_fn as Function),<dynamic>[
                  node,
                  primary_id,
                  xtd.obj_assign(xtd.obj_assign(<dynamic, dynamic>{},dataview),args[0])
                ]);
              }
          }
      },pipeline),
    "defaults":defaults,
    "options":options
  };
}

dataview_attach_model(space, args, request, node) {
  var primary_id = args[0];
  var page_args = args[1];
  var dataview = args[2];
  var model = args[3];
  var group_id = page_args["group_id"];
  var model_id = page_args["model_id"];
  var space_id = page_args["space_id"];
  var model_spec = dataview_create_model(primary_id,dataview,model);
  return attach_base_model(node,primary_id,space_id,group_id,model_id,model_spec);
}

init_handlers(node) {
  substrate.register_handler(node,"@xt.db/kernel-init",kernel_init_handler,null);
  substrate.register_handler(node,"@xt.db/kernel-setup",kernel_setup_handler,null);
  substrate.register_handler(node,"@xt.db/kernel-teardown",kernel_teardown_handler,null);
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