import 'package:xtalk_db/node/proxy-util.dart' as proxy_util;

sign_up(node, service_id, credentials, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/sign-up",
    <dynamic>[service_id,credentials,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

sign_in(node, service_id, credentials, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/sign-in",
    <dynamic>[service_id,credentials,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

sign_out(node, service_id, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/sign-out",
    <dynamic>[service_id,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

refresh(node, service_id, opts) {
  return proxy_util.request_client(node,"@xt.supabase/refresh",<dynamic>[service_id],opts);
}

signed_inp(node, service_id, opts) {
  return proxy_util.request_client(node,"@xt.supabase/signed-in?",<dynamic>[service_id],opts);
}

current_session(node, service_id, opts) {
  return proxy_util.request_client(node,"@xt.supabase/current-session",<dynamic>[service_id],opts);
}

rpc_call(node, service_id, rpc_name, data, opts) {
  return proxy_util.request_client(node,"@xt.supabase/rpc-call",<dynamic>[
    service_id,
    rpc_name,
    data ?? <dynamic, dynamic>{},
    opts ?? <dynamic, dynamic>{}
  ],opts);
}

query_table(node, service_id, table_name, query, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/query-table",
    <dynamic>[service_id,table_name,query,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

attach_model(node, service_id, page_args, supabase_handler, model, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/attach-model",
    <dynamic>[service_id,page_args,supabase_handler,model],
    opts
  );
}

health(node, service_id, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/health",
    <dynamic>[service_id,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

admin_create_user(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-create-user",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

admin_delete_user(node, service_id, user_id, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-delete-user",
    <dynamic>[service_id,user_id,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

admin_generate_link(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-generate-link",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

admin_get_user(node, service_id, user_id, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-get-user",
    <dynamic>[service_id,user_id,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

admin_list_users(node, service_id, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-list-users",
    <dynamic>[service_id,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

admin_update_user(node, service_id, user_id, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/admin-update-user",
    <dynamic>[service_id,user_id,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

authorize(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/authorize",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

callback(node, service_id, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/callback",
    <dynamic>[service_id,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

invite(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/invite",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

otp(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/otp",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

recovery(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/recovery",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

settings(node, service_id, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/settings",
    <dynamic>[service_id,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

token_refresh(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/token-refresh",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

user_get(node, service_id, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/user-get",
    <dynamic>[service_id,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

user_info(node, service_id, opts) {
  return user_get(node,service_id,opts);
}

user_put(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/user-put",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

verify_get(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/verify-get",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}

verify_post(node, service_id, data, opts) {
  return proxy_util.request_client(
    node,
    "@xt.supabase/verify-post",
    <dynamic>[service_id,data,opts ?? <dynamic, dynamic>{}],
    opts
  );
}