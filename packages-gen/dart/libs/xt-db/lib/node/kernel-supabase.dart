import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_substrate/page-core.dart' as page_core;
import 'package:xtalk_net/addon-supabase.dart' as addon;
import 'package:xtalk_net/http-fetch.dart' as http_fetch;
import 'package:xtalk_net/http-util.dart' as http_util;
import 'package:xtalk_db/system/impl-supabase-session.dart' as session;
import 'package:xtalk_substrate/substrate.dart' as substrate;
import 'dart:convert';
import 'dart:async';








supabase_rpc_error_detail(body) {
  var detail = body["details"] ?? body["detail"];
  if("String" == (detail.runtimeType).toString()){
    try{
      return jsonDecode(detail);
    }
    catch(err){
      return detail;
    }
  }
  else{
    return detail;
  }
}

supabase_error_data(response) {
  var status = response["status"];
  var body = response["body"];
  var detail = supabase_rpc_error_detail(body);
  var data = body;
  if((("Map" == (detail.runtimeType).toString()) || (detail.runtimeType).toString().startsWith("_Map") || (detail.runtimeType).toString().startsWith("LinkedMap")) && ("gw.rpc.error" == detail["type"])){
    data = detail;
  }
  if(("Map" == (data.runtimeType).toString()) || (data.runtimeType).toString().startsWith("_Map") || (data.runtimeType).toString().startsWith("LinkedMap")){
    data["status"] = status;
    if(null == data["http_status"]){
      data["http_status"] = status;
    }
  }
  return data;
}

supabase_response_data(response) {
  var status = response["status"];
  var body = response["body"];
  if(((null != status) && (false != status)) && (status >= 400)){
    var data = supabase_error_data(response);
    throw <dynamic, dynamic>{
      "__type__":"xt.exception",
      "message":data["message"] ?? body["message"] ?? "Supabase request failed",
      "data":data
    };
  }
  return http_util.get_body_data(response);
}

supabase_request(node, service_id, cmd) {
  return ((Future.sync(() => Future.sync(() {
    return substrate.get_service(node,service_id);
  }))) as Future<dynamic>).then((value) async { return await Function.apply((impl) {
    var client = impl["client"];
    return ((Future.sync(() => http_fetch.request_http(client,cmd))) as Future<dynamic>).then((value) async { return await Function.apply(supabase_response_data,<dynamic>[value]); });
  },<dynamic>[value]); });
}

supabase_sign_up_handler(space, args, request, node) {
  var service_id = args[0];
  var credentials = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return ((Future.sync(() => Future.sync(() {
    return substrate.get_service(node,service_id);
  }))) as Future<dynamic>).then((value) async { return await Function.apply((impl) {
    var client = impl["client"];
    return ((Future.sync(() => ((Future.sync(() => http_fetch.request_http(client,addon.cmd_signup(credentials,opts)))) as Future<dynamic>).then((value) async { return await Function.apply(http_util.get_body_data,<dynamic>[value]); }))) as Future<dynamic>).then((value) async { return await Function.apply((session) {
      session.set_session(impl,session);
      session.auto_refresh_start(impl);
      return session;
    },<dynamic>[value]); });
  },<dynamic>[value]); });
}

supabase_sign_in_handler(space, args, request, node) {
  var service_id = args[0];
  var credentials = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return ((Future.sync(() => Future.sync(() {
    return substrate.get_service(node,service_id);
  }))) as Future<dynamic>).then((value) async { return await Function.apply((impl) {
    var client = impl["client"];
    return ((Future.sync(() => ((Future.sync(() => http_fetch.request_http(client,addon.cmd_token_password(credentials,opts)))) as Future<dynamic>).then((value) async { return await Function.apply(http_util.get_body_data,<dynamic>[value]); }))) as Future<dynamic>).then((value) async { return await Function.apply((session) {
      session.set_session(impl,session);
      session.auto_refresh_start(impl);
      return session;
    },<dynamic>[value]); });
  },<dynamic>[value]); });
}

supabase_sign_out_handler(space, args, request, node) {
  var service_id = args[0];
  var opts = args[1] ?? <dynamic, dynamic>{};
  return ((Future.sync(() => Future.sync(() {
    return substrate.get_service(node,service_id);
  }))) as Future<dynamic>).then((value) async { return await Function.apply((impl) {
    session.auto_refresh_stop(impl);
    var client = impl["client"];
    return ((Future.sync(() => http_fetch.request_http(client,addon.cmd_logout(opts)))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      session.set_session(impl,null);
      return <dynamic, dynamic>{"status":"ok"};
    },<dynamic>[value]); });
  },<dynamic>[value]); });
}

supabase_refresh_handler(space, args, request, node) {
  var service_id = args[0];
  return ((Future.sync(() => Future.sync(() {
    return substrate.get_service(node,service_id);
  }))) as Future<dynamic>).then((value) async { return await Function.apply((impl) {
    return session.refresh_session(impl);
  },<dynamic>[value]); });
}

supabase_signed_in_handler(space, args, request, node) {
  var service_id = args[0];
  var impl = substrate.get_service(node,service_id);
  return null != session.get_session(impl);
}

supabase_current_session_handler(space, args, request, node) {
  var service_id = args[0];
  var impl = substrate.get_service(node,service_id);
  return session.get_session(impl);
}

supabase_rpc_call_handler(space, args, request, node) {
  var service_id = args[0];
  var rpc_name = args[1];
  var data = args[2] ?? <dynamic, dynamic>{};
  var opts = args[3] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_rpc_call(rpc_name,data,opts));
}

supabase_query_table_handler(space, args, request, node) {
  var service_id = args[0];
  var table_name = args[1];
  var query = args[2];
  var opts = args[3] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_query_table(table_name,query,opts));
}

supabase_health_handler(space, args, request, node) {
  var service_id = args[0];
  var opts = args[1] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_health(opts));
}

supabase_admin_create_user_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_admin_create_user(data,opts));
}

supabase_admin_delete_user_handler(space, args, request, node) {
  var service_id = args[0];
  var user_id = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_admin_delete_user(user_id,opts));
}

supabase_admin_generate_link_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_admin_generate_link(data,opts));
}

supabase_admin_get_user_handler(space, args, request, node) {
  var service_id = args[0];
  var user_id = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_admin_get_user(user_id,opts));
}

supabase_admin_list_users_handler(space, args, request, node) {
  var service_id = args[0];
  var opts = args[1] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_admin_list_users(opts));
}

supabase_admin_update_user_handler(space, args, request, node) {
  var service_id = args[0];
  var user_id = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_admin_update_user(user_id,opts));
}

supabase_authorize_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_authorize(data,opts));
}

supabase_callback_handler(space, args, request, node) {
  var service_id = args[0];
  var opts = args[1] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_callback(opts));
}

supabase_invite_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_invite(data,opts));
}

supabase_otp_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_otp(data,opts));
}

supabase_recovery_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_recovery(data,opts));
}

supabase_settings_handler(space, args, request, node) {
  var service_id = args[0];
  var opts = args[1] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_settings(opts));
}

supabase_token_refresh_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_token_refresh(data,opts));
}

supabase_user_get_handler(space, args, request, node) {
  var service_id = args[0];
  var opts = args[1] ?? <dynamic, dynamic>{};
  var impl = substrate.get_service(node,service_id);
  var session = session.get_session(impl);
  if(null != session){
    return <dynamic, dynamic>{"user":session["user"]};
  }
  else{
    return supabase_request(node,service_id,addon.cmd_user_get(opts));
  }
}

supabase_user_put_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_user_put(data,opts));
}

supabase_verify_get_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_verify_get(data,opts));
}

supabase_verify_post_handler(space, args, request, node) {
  var service_id = args[0];
  var data = args[1];
  var opts = args[2] ?? <dynamic, dynamic>{};
  return supabase_request(node,service_id,addon.cmd_verify_post(data,opts));
}

supabase_create_model(service_id, supabase_handler, model) {
  var defaults = model["defaults"];
  var options = model["options"];
  var pipeline = model["pipeline"];
  var model_handler = (context) {
    var node = context["node"];
    var cmd = ((supabase_handler.runtimeType).toString().contains("Function") || (supabase_handler.runtimeType).toString().contains("=>") || (supabase_handler).toString().startsWith("Closure")) ? supabase_handler(context) : supabase_handler;
    return supabase_request(node,service_id,cmd);
  };
  return <dynamic, dynamic>{
    "handler":model_handler,
    "pipeline":xtd.obj_assign_nested(
        <dynamic, dynamic>{"remote":<dynamic, dynamic>{"handler":model_handler}},
        pipeline
      ),
    "defaults":defaults,
    "options":options
  };
}

supabase_attach_model(space, args, request, node) {
  var service_id = args[0];
  var page_args = args[1];
  var supabase_handler = args[2];
  var model = args[3];
  var group_id = page_args["group_id"];
  var model_id = page_args["model_id"];
  var space_id = page_args["space_id"];
  var model_spec = supabase_create_model(service_id,supabase_handler,model);
  page_core.group_add_attach(node,space_id,group_id,<dynamic, dynamic>{model_id:model_spec});
  return <dynamic, dynamic>{
    "status":"attached",
    "space":space_id,
    "group":group_id,
    "model":model_id
  };
}

init_handlers(node) {
  substrate.register_handler(node,"@xt.supabase/sign-up",supabase_sign_up_handler,null);
  substrate.register_handler(node,"@xt.supabase/sign-in",supabase_sign_in_handler,null);
  substrate.register_handler(node,"@xt.supabase/sign-out",supabase_sign_out_handler,null);
  substrate.register_handler(node,"@xt.supabase/refresh",supabase_refresh_handler,null);
  substrate.register_handler(node,"@xt.supabase/signed-in?",supabase_signed_in_handler,null);
  substrate.register_handler(
    node,
    "@xt.supabase/current-session",
    supabase_current_session_handler,
    null
  );
  substrate.register_handler(node,"@xt.supabase/rpc-call",supabase_rpc_call_handler,null);
  substrate.register_handler(
    node,
    "@xt.supabase/query-table",
    supabase_query_table_handler,
    null
  );
  substrate.register_handler(node,"@xt.supabase/health",supabase_health_handler,null);
  substrate.register_handler(
    node,
    "@xt.supabase/admin-create-user",
    supabase_admin_create_user_handler,
    null
  );
  substrate.register_handler(
    node,
    "@xt.supabase/admin-delete-user",
    supabase_admin_delete_user_handler,
    null
  );
  substrate.register_handler(
    node,
    "@xt.supabase/admin-generate-link",
    supabase_admin_generate_link_handler,
    null
  );
  substrate.register_handler(
    node,
    "@xt.supabase/admin-get-user",
    supabase_admin_get_user_handler,
    null
  );
  substrate.register_handler(
    node,
    "@xt.supabase/admin-list-users",
    supabase_admin_list_users_handler,
    null
  );
  substrate.register_handler(
    node,
    "@xt.supabase/admin-update-user",
    supabase_admin_update_user_handler,
    null
  );
  substrate.register_handler(node,"@xt.supabase/authorize",supabase_authorize_handler,null);
  substrate.register_handler(node,"@xt.supabase/callback",supabase_callback_handler,null);
  substrate.register_handler(node,"@xt.supabase/invite",supabase_invite_handler,null);
  substrate.register_handler(node,"@xt.supabase/otp",supabase_otp_handler,null);
  substrate.register_handler(node,"@xt.supabase/recovery",supabase_recovery_handler,null);
  substrate.register_handler(node,"@xt.supabase/settings",supabase_settings_handler,null);
  substrate.register_handler(
    node,
    "@xt.supabase/token-refresh",
    supabase_token_refresh_handler,
    null
  );
  substrate.register_handler(node,"@xt.supabase/user-get",supabase_user_get_handler,null);
  substrate.register_handler(node,"@xt.supabase/user-info",supabase_user_get_handler,null);
  substrate.register_handler(node,"@xt.supabase/user-put",supabase_user_put_handler,null);
  substrate.register_handler(
    node,
    "@xt.supabase/verify-get",
    supabase_verify_get_handler,
    null
  );
  substrate.register_handler(
    node,
    "@xt.supabase/verify-post",
    supabase_verify_post_handler,
    null
  );
  substrate.register_handler(node,"@xt.supabase/attach-model",supabase_attach_model,null);
  return node;
}