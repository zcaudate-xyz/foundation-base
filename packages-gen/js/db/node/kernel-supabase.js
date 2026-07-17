const xtd = require("@xtalk/lang/common-data.js")

const page_core = require("@xtalk/substrate/page-core.js")

const addon = require("@xtalk/net/addon-supabase.js")

const http_fetch = require("@xtalk/net/http-fetch.js")

const http_util = require("@xtalk/net/http-util.js")

const session = require("@xtalk/db/system/impl-supabase-session.js")

const substrate = require("@xtalk/substrate/substrate.js")

function supabase_rpc_error_detail(body){
  let detail = body["details"] || body["detail"];
  if("string" == (typeof detail)){
    try{
      return JSON.parse(detail);
    }
    catch(err){
      return detail;
    }
  }
  else{
    return detail;
  }
}

function supabase_error_data(response){
  let status = response["status"];
  let body = response["body"];
  let detail = supabase_rpc_error_detail(body);
  let data = body;
  if(((null != detail) && ("object" == (typeof detail)) && !Array.isArray(detail)) && ("gw.rpc.error" == detail["type"])){
    data = detail;
  }
  if((null != data) && ("object" == (typeof data)) && !Array.isArray(data)){
    data["status"] = status;
    if(null == data["http_status"]){
      data["http_status"] = status;
    }
  }
  return data;
}

function supabase_response_data(response){
  let status = response["status"];
  let data = supabase_error_data(response);
  let body = response["body"];
  if(status && (status >= 400)){
    throw Object.assign(new Error(
      data["message"] || body["message"] || "Supabase request failed"
    ),{"data":data});
  }
  return http_util.get_body_data(response);
}

function supabase_request(node,service_id,cmd){
  return Promise.resolve().then(function (){
    return substrate.get_service(node,service_id);
  }).then(function (impl){
    let client = impl["client"];
    return http_fetch.request_http(client,cmd).then(supabase_response_data);
  });
}

function supabase_sign_up_handler(space,args,request,node){
  let service_id = args[0];
  let credentials = args[1];
  let opts = args[2] || {};
  return Promise.resolve().then(function (){
    return substrate.get_service(node,service_id);
  }).then(function (impl){
    let client = impl["client"];
    return http_fetch.request_http(client,addon.cmd_signup(credentials,opts)).then(http_util.get_body_data).then(function (session){
      session.set_session(impl,session);
      session.auto_refresh_start(impl);
      return session;
    });
  });
}

function supabase_sign_in_handler(space,args,request,node){
  let service_id = args[0];
  let credentials = args[1];
  let opts = args[2] || {};
  return Promise.resolve().then(function (){
    return substrate.get_service(node,service_id);
  }).then(function (impl){
    let client = impl["client"];
    return http_fetch.request_http(client,addon.cmd_token_password(credentials,opts)).then(http_util.get_body_data).then(function (session){
      session.set_session(impl,session);
      session.auto_refresh_start(impl);
      return session;
    });
  });
}

function supabase_sign_out_handler(space,args,request,node){
  let service_id = args[0];
  let opts = args[1] || {};
  return Promise.resolve().then(function (){
    return substrate.get_service(node,service_id);
  }).then(function (impl){
    session.auto_refresh_stop(impl);
    let client = impl["client"];
    return http_fetch.request_http(client,addon.cmd_logout(opts)).then(function (_){
      session.set_session(impl,null);
      return {"status":"ok"};
    });
  });
}

function supabase_refresh_handler(space,args,request,node){
  let service_id = args[0];
  return Promise.resolve().then(function (){
    return substrate.get_service(node,service_id);
  }).then(function (impl){
    return session.refresh_session(impl);
  });
}

function supabase_signed_in_handler(space,args,request,node){
  let service_id = args[0];
  let impl = substrate.get_service(node,service_id);
  return null != session.get_session(impl);
}

function supabase_current_session_handler(space,args,request,node){
  let service_id = args[0];
  let impl = substrate.get_service(node,service_id);
  return session.get_session(impl);
}

function supabase_rpc_call_handler(space,args,request,node){
  let service_id = args[0];
  let rpc_name = args[1];
  let data = args[2] || {};
  let opts = args[3] || {};
  return supabase_request(node,service_id,addon.cmd_rpc_call(rpc_name,data,opts));
}

function supabase_query_table_handler(space,args,request,node){
  let service_id = args[0];
  let table_name = args[1];
  let query = args[2];
  let opts = args[3] || {};
  return supabase_request(node,service_id,addon.cmd_query_table(table_name,query,opts));
}

function supabase_health_handler(space,args,request,node){
  let service_id = args[0];
  let opts = args[1] || {};
  return supabase_request(node,service_id,addon.cmd_health(opts));
}

function supabase_admin_create_user_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_admin_create_user(data,opts));
}

function supabase_admin_delete_user_handler(space,args,request,node){
  let service_id = args[0];
  let user_id = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_admin_delete_user(user_id,opts));
}

function supabase_admin_generate_link_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_admin_generate_link(data,opts));
}

function supabase_admin_get_user_handler(space,args,request,node){
  let service_id = args[0];
  let user_id = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_admin_get_user(user_id,opts));
}

function supabase_admin_list_users_handler(space,args,request,node){
  let service_id = args[0];
  let opts = args[1] || {};
  return supabase_request(node,service_id,addon.cmd_admin_list_users(opts));
}

function supabase_admin_update_user_handler(space,args,request,node){
  let service_id = args[0];
  let user_id = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_admin_update_user(user_id,opts));
}

function supabase_authorize_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_authorize(data,opts));
}

function supabase_callback_handler(space,args,request,node){
  let service_id = args[0];
  let opts = args[1] || {};
  return supabase_request(node,service_id,addon.cmd_callback(opts));
}

function supabase_invite_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_invite(data,opts));
}

function supabase_otp_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_otp(data,opts));
}

function supabase_recovery_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_recovery(data,opts));
}

function supabase_settings_handler(space,args,request,node){
  let service_id = args[0];
  let opts = args[1] || {};
  return supabase_request(node,service_id,addon.cmd_settings(opts));
}

function supabase_token_refresh_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_token_refresh(data,opts));
}

function supabase_user_get_handler(space,args,request,node){
  let service_id = args[0];
  let opts = args[1] || {};
  let impl = substrate.get_service(node,service_id);
  let session = session.get_session(impl);
  if(null != session){
    return {"user":session["user"]};
  }
  else{
    return supabase_request(node,service_id,addon.cmd_user_get(opts));
  }
}

function supabase_user_put_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_user_put(data,opts));
}

function supabase_verify_get_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_verify_get(data,opts));
}

function supabase_verify_post_handler(space,args,request,node){
  let service_id = args[0];
  let data = args[1];
  let opts = args[2] || {};
  return supabase_request(node,service_id,addon.cmd_verify_post(data,opts));
}

function supabase_create_model(service_id,supabase_handler,model){
  let {defaults,options,pipeline} = model;
  let model_handler = function (context){
    let node = context["node"];
    let cmd = ("function" == (typeof supabase_handler)) ? supabase_handler(context) : supabase_handler;
    return supabase_request(node,service_id,cmd);
  };
  return {
    "handler":model_handler,
    "pipeline":xtd.obj_assign_nested({"remote":{"handler":model_handler}},pipeline),
    "defaults":defaults,
    "options":options
  };
}

function supabase_attach_model(space,args,request,node){
  let service_id = args[0];
  let page_args = args[1];
  let supabase_handler = args[2];
  let model = args[3];
  let {group_id,model_id,space_id} = page_args;
  let model_spec = supabase_create_model(service_id,supabase_handler,model);
  page_core.group_add_attach(node,space_id,group_id,{[model_id]:model_spec});
  return {
    "status":"attached",
    "space":space_id,
    "group":group_id,
    "model":model_id
  };
}

function init_handlers(node){
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

module.exports = {
  ["supabase_rpc_error_detail"]:supabase_rpc_error_detail,
  ["supabase_error_data"]:supabase_error_data,
  ["supabase_response_data"]:supabase_response_data,
  ["supabase_request"]:supabase_request,
  ["supabase_sign_up_handler"]:supabase_sign_up_handler,
  ["supabase_sign_in_handler"]:supabase_sign_in_handler,
  ["supabase_sign_out_handler"]:supabase_sign_out_handler,
  ["supabase_refresh_handler"]:supabase_refresh_handler,
  ["supabase_signed_in_handler"]:supabase_signed_in_handler,
  ["supabase_current_session_handler"]:supabase_current_session_handler,
  ["supabase_rpc_call_handler"]:supabase_rpc_call_handler,
  ["supabase_query_table_handler"]:supabase_query_table_handler,
  ["supabase_health_handler"]:supabase_health_handler,
  ["supabase_admin_create_user_handler"]:supabase_admin_create_user_handler,
  ["supabase_admin_delete_user_handler"]:supabase_admin_delete_user_handler,
  ["supabase_admin_generate_link_handler"]:supabase_admin_generate_link_handler,
  ["supabase_admin_get_user_handler"]:supabase_admin_get_user_handler,
  ["supabase_admin_list_users_handler"]:supabase_admin_list_users_handler,
  ["supabase_admin_update_user_handler"]:supabase_admin_update_user_handler,
  ["supabase_authorize_handler"]:supabase_authorize_handler,
  ["supabase_callback_handler"]:supabase_callback_handler,
  ["supabase_invite_handler"]:supabase_invite_handler,
  ["supabase_otp_handler"]:supabase_otp_handler,
  ["supabase_recovery_handler"]:supabase_recovery_handler,
  ["supabase_settings_handler"]:supabase_settings_handler,
  ["supabase_token_refresh_handler"]:supabase_token_refresh_handler,
  ["supabase_user_get_handler"]:supabase_user_get_handler,
  ["supabase_user_put_handler"]:supabase_user_put_handler,
  ["supabase_verify_get_handler"]:supabase_verify_get_handler,
  ["supabase_verify_post_handler"]:supabase_verify_post_handler,
  ["supabase_create_model"]:supabase_create_model,
  ["supabase_attach_model"]:supabase_attach_model,
  ["init_handlers"]:init_handlers
}