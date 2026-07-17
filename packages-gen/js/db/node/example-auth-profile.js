const xtd = require("@xtalk/lang/common-data.js")

const kernel_supabase = require("@xtalk/db/node/kernel-supabase.js")

const supabase = require("@xtalk/db/node/client-supabase.js")

const page_core = require("@xtalk/substrate/page-core.js")

const db_main = require("@xtalk/db/system/main.js")

const session = require("@xtalk/db/system/impl-supabase-session.js")

const substrate = require("@xtalk/substrate/substrate.js")

var DEFAULT_SPACE_ID = "example/auth";

var DEFAULT_GROUP_ID = "auth";

var DEFAULT_SERVICE_ID = "auth/supabase";

function first_arg(ctx,arg,event_key){
  let value = arg || xtd.get_in(ctx,["event",event_key]) || (ctx["args"])[0] || (xtd.get_in(ctx,["input","data"]))[0] || (xtd.get_in(ctx,["event","data"]))[0];
  if(Array.isArray(value) && (1 == value.length)){
    value = value[0];
  }
  return value;
}

function session_model(service_id){
  return {
    "handler":function (ctx){
        let {node} = ctx;
        return supabase.current_session(node,service_id,{});
      },
    "defaults":{"args":[]},
    "options":{}
  };
}

function profile_model(service_id){
  return {
    "handler":function (ctx){
        let {node} = ctx;
        return supabase.user_get(node,service_id,{});
      },
    "defaults":{"args":[]},
    "options":{}
  };
}

function sign_up_model(service_id){
  return {
    "handler":function (ctx,credentials){
        let {node} = ctx;
        credentials = first_arg(ctx,credentials,"credentials");
        return supabase.sign_up(node,service_id,credentials,{});
      },
    "defaults":{"args":[]},
    "options":{}
  };
}

function login_model(service_id){
  return {
    "handler":function (ctx,credentials){
        let {node} = ctx;
        credentials = first_arg(ctx,credentials,"credentials");
        return supabase.sign_in(node,service_id,credentials,{});
      },
    "defaults":{"args":[]},
    "options":{}
  };
}

function logout_model(service_id){
  return {
    "handler":function (ctx){
        let {node} = ctx;
        return supabase.current_session(node,service_id,{}).then(function (curr_session){
          return supabase.sign_out(node,service_id,{"token":curr_session["access_token"]});
        });
      },
    "defaults":{"args":[]},
    "options":{}
  };
}

function change_profile_model(service_id){
  return {
    "handler":function (ctx,profile_data){
        let {node} = ctx;
        profile_data = first_arg(ctx,profile_data,"profile");
        return supabase.current_session(node,service_id,{}).then(function (curr_session){
          return supabase.user_put(
            node,
            service_id,
            {"data":profile_data},
            {"token":curr_session["access_token"]}
          );
        }).then(function (updated){
          let updated_user = updated["user"] || updated;
          let impl = substrate.get_service(node,service_id);
          let curr_session = session.get_session(impl);
          if(curr_session){
            session.set_session(
              impl,
              Object.assign(Object.assign({},curr_session),{"user":updated_user})
            );
          }
          return updated_user;
        });
      },
    "defaults":{"args":[]},
    "options":{}
  };
}

function auth_profile_models(service_id){
  service_id = (service_id || DEFAULT_SERVICE_ID);
  return {
    "session":session_model(service_id),
    "profile":profile_model(service_id),
    "sign-up":sign_up_model(service_id),
    "login":login_model(service_id),
    "logout":logout_model(service_id),
    "change-profile":change_profile_model(service_id)
  };
}

function attach_auth_profile_models(node,service_id,page_args){
  page_args = (page_args || {});
  let space_id = page_args["space_id"] || DEFAULT_SPACE_ID;
  let group_id = page_args["group_id"] || DEFAULT_GROUP_ID;
  page_core.group_add_attach(node,space_id,group_id,auth_profile_models(service_id));
  return {
    "status":"attached",
    "space":space_id,
    "group":group_id,
    "models":["session","profile","sign-up","login","logout","change-profile"]
  };
}

function create_auth_profile_node(client_defaults,service_id,page_args){
  service_id = (service_id || DEFAULT_SERVICE_ID);
  let node = substrate.node_create({});
  let impl = db_main.create_impl("supabase",client_defaults,null,null);
  substrate.set_service(node,service_id,impl);
  kernel_supabase.init_handlers(node);
  attach_auth_profile_models(node,service_id,page_args);
  return node;
}

module.exports = {
  ["DEFAULT_SPACE_ID"]:DEFAULT_SPACE_ID,
  ["DEFAULT_GROUP_ID"]:DEFAULT_GROUP_ID,
  ["DEFAULT_SERVICE_ID"]:DEFAULT_SERVICE_ID,
  ["first_arg"]:first_arg,
  ["session_model"]:session_model,
  ["profile_model"]:profile_model,
  ["sign_up_model"]:sign_up_model,
  ["login_model"]:login_model,
  ["logout_model"]:logout_model,
  ["change_profile_model"]:change_profile_model,
  ["auth_profile_models"]:auth_profile_models,
  ["attach_auth_profile_models"]:attach_auth_profile_models,
  ["create_auth_profile_node"]:create_auth_profile_node
}