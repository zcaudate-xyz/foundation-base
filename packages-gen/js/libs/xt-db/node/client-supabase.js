const proxy_util = require("@xtalk/db/node/proxy-util.js")

function sign_up(node,service_id,credentials,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/sign-up",
    [service_id,credentials,opts || {}],
    opts
  );
}

function sign_in(node,service_id,credentials,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/sign-in",
    [service_id,credentials,opts || {}],
    opts
  );
}

function sign_out(node,service_id,opts){
  return proxy_util.request_client(node,"@xt.supabase/sign-out",[service_id,opts || {}],opts);
}

function refresh(node,service_id,opts){
  return proxy_util.request_client(node,"@xt.supabase/refresh",[service_id],opts);
}

function signed_inp(node,service_id,opts){
  return proxy_util.request_client(node,"@xt.supabase/signed-in?",[service_id],opts);
}

function current_session(node,service_id,opts){
  return proxy_util.request_client(node,"@xt.supabase/current-session",[service_id],opts);
}

function rpc_call(node,service_id,rpc_name,data,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/rpc-call",
    [service_id,rpc_name,data || {},opts || {}],
    opts
  );
}

function query_table(node,service_id,table_name,query,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/query-table",
    [service_id,table_name,query,opts || {}],
    opts
  );
}

function attach_model(node,service_id,page_args,supabase_handler,model,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/attach-model",
    [service_id,page_args,supabase_handler,model],
    opts
  );
}

function health(node,service_id,opts){
  return proxy_util.request_client(node,"@xt.supabase/health",[service_id,opts || {}],opts);
}

function admin_create_user(node,service_id,data,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-create-user",
    [service_id,data,opts || {}],
    opts
  );
}

function admin_delete_user(node,service_id,user_id,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-delete-user",
    [service_id,user_id,opts || {}],
    opts
  );
}

function admin_generate_link(node,service_id,data,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-generate-link",
    [service_id,data,opts || {}],
    opts
  );
}

function admin_get_user(node,service_id,user_id,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-get-user",
    [service_id,user_id,opts || {}],
    opts
  );
}

function admin_list_users(node,service_id,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-list-users",
    [service_id,opts || {}],
    opts
  );
}

function admin_update_user(node,service_id,user_id,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-update-user",
    [service_id,user_id,opts || {}],
    opts
  );
}

function authorize(node,service_id,data,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/authorize",
    [service_id,data,opts || {}],
    opts
  );
}

function callback(node,service_id,opts){
  return proxy_util.request_client(node,"@xt.supabase/callback",[service_id,opts || {}],opts);
}

function invite(node,service_id,data,opts){
  return proxy_util.request_client(node,"@xt.supabase/invite",[service_id,data,opts || {}],opts);
}

function otp(node,service_id,data,opts){
  return proxy_util.request_client(node,"@xt.supabase/otp",[service_id,data,opts || {}],opts);
}

function recovery(node,service_id,data,opts){
  return proxy_util.request_client(node,"@xt.supabase/recovery",[service_id,data,opts || {}],opts);
}

function settings(node,service_id,opts){
  return proxy_util.request_client(node,"@xt.supabase/settings",[service_id,opts || {}],opts);
}

function token_refresh(node,service_id,data,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/token-refresh",
    [service_id,data,opts || {}],
    opts
  );
}

function user_get(node,service_id,opts){
  return proxy_util.request_client(node,"@xt.supabase/user-get",[service_id,opts || {}],opts);
}

function user_info(node,service_id,opts){
  return user_get(node,service_id,opts);
}

function user_put(node,service_id,data,opts){
  return proxy_util.request_client(node,"@xt.supabase/user-put",[service_id,data,opts || {}],opts);
}

function verify_get(node,service_id,data,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/verify-get",
    [service_id,data,opts || {}],
    opts
  );
}

function verify_post(node,service_id,data,opts){
  return proxy_util.request_client(
    node,
    "@xt.supabase/verify-post",
    [service_id,data,opts || {}],
    opts
  );
}

module.exports = {
  ["sign_up"]:sign_up,
  ["sign_in"]:sign_in,
  ["sign_out"]:sign_out,
  ["refresh"]:refresh,
  ["signed_inp"]:signed_inp,
  ["current_session"]:current_session,
  ["rpc_call"]:rpc_call,
  ["query_table"]:query_table,
  ["attach_model"]:attach_model,
  ["health"]:health,
  ["admin_create_user"]:admin_create_user,
  ["admin_delete_user"]:admin_delete_user,
  ["admin_generate_link"]:admin_generate_link,
  ["admin_get_user"]:admin_get_user,
  ["admin_list_users"]:admin_list_users,
  ["admin_update_user"]:admin_update_user,
  ["authorize"]:authorize,
  ["callback"]:callback,
  ["invite"]:invite,
  ["otp"]:otp,
  ["recovery"]:recovery,
  ["settings"]:settings,
  ["token_refresh"]:token_refresh,
  ["user_get"]:user_get,
  ["user_info"]:user_info,
  ["user_put"]:user_put,
  ["verify_get"]:verify_get,
  ["verify_post"]:verify_post
}