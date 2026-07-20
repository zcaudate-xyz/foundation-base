import 'package:xtalk_net/http-fetch.dart' as fetch;
import 'package:xtalk_net/http-util.dart' as ut;
import 'dart:convert';



wrap_supabase_auth(handler) {
  return (client, input) {
    var defaults = client["defaults"];
    var apikey = input["apikey"] ?? defaults["apikey"];
    var token = input["token"] ?? defaults["token"];
    var headers = xt.lang.common_data.obj_assign(xt.lang.common_data.obj_assign(xt.lang.common_data.obj_assign(<dynamic, dynamic>{
      "Content-Type":"application/json",
      "Accept":"application/json"
    },input["headers"]),((null != token) && (false != token)) ? <dynamic, dynamic>{"Authorization":"Bearer " + token} : null),((null != apikey) && (false != apikey)) ? <dynamic, dynamic>{"apikey":apikey} : null);
    return Function.apply((handler as Function),<dynamic>[
      client,
      xt.lang.common_data.obj_assign(
          xt.lang.common_data.obj_assign(<dynamic, dynamic>{},input),
          <dynamic, dynamic>{"headers":headers}
        )
    ]);
  };
}

middleware_supabase() {
  return <dynamic>[
    fetch.wrap_prepare_input,
    wrap_supabase_auth,
    fetch.wrap_normalise
  ];
}

cmd_rpc_call(rpc_name, data, opts) {
  var path = "/rest/v1/rpc/" + rpc_name;
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":path,
    "method":"POST",
    "body":jsonEncode(data ?? <dynamic, dynamic>{})
  },opts);
}

cmd_query_table(table_name, query, opts) {
  var path = "/rest/v1/" + table_name + "?" + query;
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{"path":path,"method":"GET"},opts);
}

cmd_health(opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/health","method":"GET"},
    opts
  );
}

cmd_signup(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/signup",
    "method":"POST",
    "body":jsonEncode(data)
  },opts);
}

cmd_admin_create_user(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/admin/users",
    "method":"POST",
    "body":jsonEncode(data)
  },opts);
}

cmd_admin_delete_user(user_id, opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/admin/users/" + user_id,"method":"DELETE"},
    opts
  );
}

cmd_admin_generate_link(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/admin/generate_link",
    "method":"POST",
    "body":jsonEncode(data)
  },opts);
}

cmd_admin_get_user(user_id, opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/admin/users/" + user_id,"method":"GET"},
    opts
  );
}

cmd_admin_list_users(opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/admin/users","method":"GET"},
    opts
  );
}

cmd_admin_update_user(user_id, opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/admin/users/" + user_id,"method":"PUT"},
    opts
  );
}

cmd_authorize(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/authorize?" + ut.encode_query_params(data),
    "method":"GET"
  },opts);
}

cmd_callback(opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/callback","method":"GET"},
    opts
  );
}

cmd_invite(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/invite",
    "method":"POST",
    "body":jsonEncode(data)
  },opts);
}

cmd_logout(opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/logout","method":"POST"},
    opts
  );
}

cmd_otp(data, opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/otp","method":"POST","body":jsonEncode(data)},
    opts
  );
}

cmd_recovery(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/recover",
    "method":"POST",
    "body":jsonEncode(data)
  },opts);
}

cmd_settings(opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/settings","method":"GET"},
    opts
  );
}

cmd_token_password(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/token?grant_type=password",
    "method":"POST",
    "body":jsonEncode(data)
  },opts);
}

cmd_token_refresh(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/token?grant_type=refresh_token",
    "method":"POST",
    "body":jsonEncode(data)
  },opts);
}

cmd_user_get(opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/user","method":"GET"},
    opts
  );
}

cmd_user_put(data, opts) {
  return xt.lang.common_data.obj_assign(
    <dynamic, dynamic>{"path":"/auth/v1/user","method":"PUT","body":jsonEncode(data)},
    opts
  );
}

cmd_verify_get(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/verify?" + ut.encode_query_params(data),
    "method":"GET"
  },opts);
}

cmd_verify_post(data, opts) {
  return xt.lang.common_data.obj_assign(<dynamic, dynamic>{
    "path":"/auth/v1/verify",
    "method":"POST",
    "body":jsonEncode(data)
  },opts);
}