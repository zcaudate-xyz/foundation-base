import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_db/node/kernel-supabase.dart' as kernel_supabase;
import 'package:xtalk_db/node/client-supabase.dart' as supabase;
import 'package:xtalk_substrate/page-core.dart' as page_core;
import 'package:xtalk_db/system/main.dart' as db_main;
import 'package:xtalk_db/system/impl-supabase-session.dart' as session;
import 'package:xtalk_substrate/substrate.dart' as substrate;
import 'dart:async';








var DEFAULT_SPACE_ID = "example/auth";

var DEFAULT_GROUP_ID = "auth";

var DEFAULT_SERVICE_ID = "auth/supabase";

first_arg(ctx, arg, event_key) {
  var value = arg ?? xtd.get_in(ctx,<dynamic>["event",event_key]) ?? (ctx["args"])[0] ?? (xtd.get_in(ctx,<dynamic>["input","data"]))[0] ?? (xtd.get_in(ctx,<dynamic>["event","data"]))[0];
  if(((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")) && (1 == value.length)){
    value = value[0];
  }
  return value;
}

session_model(service_id) {
  return <dynamic, dynamic>{
    "handler":(ctx) {
        var node = ctx["node"];
        return supabase.current_session(node,service_id,<dynamic, dynamic>{});
      },
    "defaults":<dynamic, dynamic>{"args":<dynamic>[]},
    "options":<dynamic, dynamic>{}
  };
}

profile_model(service_id) {
  return <dynamic, dynamic>{
    "handler":(ctx) {
        var node = ctx["node"];
        return supabase.user_get(node,service_id,<dynamic, dynamic>{});
      },
    "defaults":<dynamic, dynamic>{"args":<dynamic>[]},
    "options":<dynamic, dynamic>{}
  };
}

sign_up_model(service_id) {
  return <dynamic, dynamic>{
    "handler":(ctx, credentials) {
        var node = ctx["node"];
        credentials = first_arg(ctx,credentials,"credentials");
        return supabase.sign_up(node,service_id,credentials,<dynamic, dynamic>{});
      },
    "defaults":<dynamic, dynamic>{"args":<dynamic>[]},
    "options":<dynamic, dynamic>{}
  };
}

login_model(service_id) {
  return <dynamic, dynamic>{
    "handler":(ctx, credentials) {
        var node = ctx["node"];
        credentials = first_arg(ctx,credentials,"credentials");
        return supabase.sign_in(node,service_id,credentials,<dynamic, dynamic>{});
      },
    "defaults":<dynamic, dynamic>{"args":<dynamic>[]},
    "options":<dynamic, dynamic>{}
  };
}

logout_model(service_id) {
  return <dynamic, dynamic>{
    "handler":(ctx) {
        var node = ctx["node"];
        return ((Future.sync(() => supabase.current_session(node,service_id,<dynamic, dynamic>{}))) as Future<dynamic>).then((value) async { return await Function.apply((curr_session) {
          return supabase.sign_out(
            node,
            service_id,
            <dynamic, dynamic>{"token":curr_session["access_token"]}
          );
        },<dynamic>[value]); });
      },
    "defaults":<dynamic, dynamic>{"args":<dynamic>[]},
    "options":<dynamic, dynamic>{}
  };
}

change_profile_model(service_id) {
  return <dynamic, dynamic>{
    "handler":(ctx, profile_data) {
        var node = ctx["node"];
        profile_data = first_arg(ctx,profile_data,"profile");
        return ((Future.sync(() => ((Future.sync(() => supabase.current_session(node,service_id,<dynamic, dynamic>{}))) as Future<dynamic>).then((value) async { return await Function.apply((curr_session) {
          return supabase.user_put(
            node,
            service_id,
            <dynamic, dynamic>{"data":profile_data},
            <dynamic, dynamic>{"token":curr_session["access_token"]}
          );
        },<dynamic>[value]); }))) as Future<dynamic>).then((value) async { return await Function.apply((updated) {
          var updated_user = updated["user"] ?? updated;
          var impl = substrate.get_service(node,service_id);
          var curr_session = session.get_session(impl);
          if((null != curr_session) && (false != curr_session)){
            session.set_session(impl,xtd.obj_assign(
              xtd.obj_clone(curr_session),
              <dynamic, dynamic>{"user":updated_user}
            ));
          }
          return updated_user;
        },<dynamic>[value]); });
      },
    "defaults":<dynamic, dynamic>{"args":<dynamic>[]},
    "options":<dynamic, dynamic>{}
  };
}

auth_profile_models(service_id) {
  service_id = (service_id ?? DEFAULT_SERVICE_ID);
  return <dynamic, dynamic>{
    "session":session_model(service_id),
    "profile":profile_model(service_id),
    "sign-up":sign_up_model(service_id),
    "login":login_model(service_id),
    "logout":logout_model(service_id),
    "change-profile":change_profile_model(service_id)
  };
}

attach_auth_profile_models(node, service_id, page_args) {
  page_args = (page_args ?? <dynamic, dynamic>{});
  var space_id = page_args["space_id"] ?? DEFAULT_SPACE_ID;
  var group_id = page_args["group_id"] ?? DEFAULT_GROUP_ID;
  page_core.group_add_attach(node,space_id,group_id,auth_profile_models(service_id));
  return <dynamic, dynamic>{
    "status":"attached",
    "space":space_id,
    "group":group_id,
    "models":<dynamic>["session","profile","sign-up","login","logout","change-profile"]
  };
}

create_auth_profile_node(client_defaults, service_id, page_args) {
  service_id = (service_id ?? DEFAULT_SERVICE_ID);
  var node = substrate.node_create(<dynamic, dynamic>{});
  var impl = db_main.create_impl("supabase",client_defaults,null,null);
  substrate.set_service(node,service_id,impl);
  kernel_supabase.init_handlers(node);
  attach_auth_profile_models(node,service_id,page_args);
  return node;
}