const fetch = require("@xtalk/net/http-fetch.js")

const ut = require("@xtalk/net/http-util.js")

function wrap_supabase_auth(handler){
  return function (client,input){
    let {defaults} = client;
    let apikey = input["apikey"] || defaults["apikey"];
    let token = input["token"] || defaults["token"];
    let headers = Object.assign(Object.assign(Object.assign({
      "Content-Type":"application/json",
      "Accept":"application/json"
    },input["headers"]),token ? {"Authorization":"Bearer " + token} : null),apikey ? {"apikey":apikey} : null);
    return handler(
      client,
      Object.assign(Object.assign({},input),{"headers":headers})
    );
  };
}

function middleware_supabase(){
  return [
    fetch.wrap_prepare_input,
    wrap_supabase_auth,
    fetch.wrap_normalise
  ];
}

function cmd_rpc_call(rpc_name,data,opts){
  let path = "/rest/v1/rpc/" + rpc_name;
  return Object.assign(
    {"path":path,"method":"POST","body":JSON.stringify(data || {})},
    opts
  );
}

function cmd_query_table(table_name,query,opts){
  let path = "/rest/v1/" + table_name + "?" + query;
  return Object.assign({"path":path,"method":"GET"},opts);
}

function cmd_health(opts){
  return Object.assign({"path":"/auth/v1/health","method":"GET"},opts);
}

function cmd_signup(data,opts){
  return Object.assign({
    "path":"/auth/v1/signup",
    "method":"POST",
    "body":JSON.stringify(data)
  },opts);
}

function cmd_admin_create_user(data,opts){
  return Object.assign({
    "path":"/auth/v1/admin/users",
    "method":"POST",
    "body":JSON.stringify(data)
  },opts);
}

function cmd_admin_delete_user(user_id,opts){
  return Object.assign(
    {"path":"/auth/v1/admin/users/" + user_id,"method":"DELETE"},
    opts
  );
}

function cmd_admin_generate_link(data,opts){
  return Object.assign({
    "path":"/auth/v1/admin/generate_link",
    "method":"POST",
    "body":JSON.stringify(data)
  },opts);
}

function cmd_admin_get_user(user_id,opts){
  return Object.assign(
    {"path":"/auth/v1/admin/users/" + user_id,"method":"GET"},
    opts
  );
}

function cmd_admin_list_users(opts){
  return Object.assign({"path":"/auth/v1/admin/users","method":"GET"},opts);
}

function cmd_admin_update_user(user_id,opts){
  return Object.assign(
    {"path":"/auth/v1/admin/users/" + user_id,"method":"PUT"},
    opts
  );
}

function cmd_authorize(data,opts){
  return Object.assign({
    "path":"/auth/v1/authorize?" + ut.encode_query_params(data),
    "method":"GET"
  },opts);
}

function cmd_callback(opts){
  return Object.assign({"path":"/auth/v1/callback","method":"GET"},opts);
}

function cmd_invite(data,opts){
  return Object.assign({
    "path":"/auth/v1/invite",
    "method":"POST",
    "body":JSON.stringify(data)
  },opts);
}

function cmd_logout(opts){
  return Object.assign({"path":"/auth/v1/logout","method":"POST"},opts);
}

function cmd_otp(data,opts){
  return Object.assign({
    "path":"/auth/v1/otp",
    "method":"POST",
    "body":JSON.stringify(data)
  },opts);
}

function cmd_recovery(data,opts){
  return Object.assign({
    "path":"/auth/v1/recover",
    "method":"POST",
    "body":JSON.stringify(data)
  },opts);
}

function cmd_settings(opts){
  return Object.assign({"path":"/auth/v1/settings","method":"GET"},opts);
}

function cmd_token_password(data,opts){
  return Object.assign({
    "path":"/auth/v1/token?grant_type=password",
    "method":"POST",
    "body":JSON.stringify(data)
  },opts);
}

function cmd_token_refresh(data,opts){
  return Object.assign({
    "path":"/auth/v1/token?grant_type=refresh_token",
    "method":"POST",
    "body":JSON.stringify(data)
  },opts);
}

function cmd_user_get(opts){
  return Object.assign({"path":"/auth/v1/user","method":"GET"},opts);
}

function cmd_user_put(data,opts){
  return Object.assign({
    "path":"/auth/v1/user",
    "method":"PUT",
    "body":JSON.stringify(data)
  },opts);
}

function cmd_verify_get(data,opts){
  return Object.assign({
    "path":"/auth/v1/verify?" + ut.encode_query_params(data),
    "method":"GET"
  },opts);
}

function cmd_verify_post(data,opts){
  return Object.assign({
    "path":"/auth/v1/verify",
    "method":"POST",
    "body":JSON.stringify(data)
  },opts);
}

module.exports = {
  ["wrap_supabase_auth"]:wrap_supabase_auth,
  ["middleware_supabase"]:middleware_supabase,
  ["cmd_rpc_call"]:cmd_rpc_call,
  ["cmd_query_table"]:cmd_query_table,
  ["cmd_health"]:cmd_health,
  ["cmd_signup"]:cmd_signup,
  ["cmd_admin_create_user"]:cmd_admin_create_user,
  ["cmd_admin_delete_user"]:cmd_admin_delete_user,
  ["cmd_admin_generate_link"]:cmd_admin_generate_link,
  ["cmd_admin_get_user"]:cmd_admin_get_user,
  ["cmd_admin_list_users"]:cmd_admin_list_users,
  ["cmd_admin_update_user"]:cmd_admin_update_user,
  ["cmd_authorize"]:cmd_authorize,
  ["cmd_callback"]:cmd_callback,
  ["cmd_invite"]:cmd_invite,
  ["cmd_logout"]:cmd_logout,
  ["cmd_otp"]:cmd_otp,
  ["cmd_recovery"]:cmd_recovery,
  ["cmd_settings"]:cmd_settings,
  ["cmd_token_password"]:cmd_token_password,
  ["cmd_token_refresh"]:cmd_token_refresh,
  ["cmd_user_get"]:cmd_user_get,
  ["cmd_user_put"]:cmd_user_put,
  ["cmd_verify_get"]:cmd_verify_get,
  ["cmd_verify_post"]:cmd_verify_post
}