import 'package:xtalk_db/node/example-auth-profile.dart' as example;
import 'package:xtalk_substrate/page-core.dart' as page_core;
import 'package:xtalk_substrate/view.dart' as view;
import 'package:xtalk_substrate/substrate.dart' as substrate;
import 'dart:async';





make_binding(source, space_id, path, opts) {
  return xtd.obj_assign(<dynamic, dynamic>{
    "source":source,
    "space_id":space_id,
    "path":path ?? <dynamic>[]
  },opts ?? <dynamic, dynamic>{});
}

spec(space_id, group_id) {
  space_id = (space_id ?? example.DEFAULT_SPACE_ID);
  group_id = (group_id ?? example.DEFAULT_GROUP_ID);
  return view.view_spec("example/auth-profile",<dynamic, dynamic>{
    "email":make_binding(
        "state",
        space_id,
        <dynamic>["email"],
        <dynamic, dynamic>{"default":""}
      ),
    "password":make_binding(
        "state",
        space_id,
        <dynamic>["password"],
        <dynamic, dynamic>{"default":""}
      ),
    "display_name":make_binding(
        "state",
        space_id,
        <dynamic>["display_name"],
        <dynamic, dynamic>{"default":""}
      ),
    "pending":make_binding("state",space_id,<dynamic>["pending"],<dynamic, dynamic>{}),
    "error":make_binding("state",space_id,<dynamic>["error"],<dynamic, dynamic>{}),
    "session":make_binding(
        "model-output",
        space_id,
        <dynamic>[],
        <dynamic, dynamic>{"group_id":group_id,"model_id":"session"}
      ),
    "profile":make_binding(
        "model-output",
        space_id,
        <dynamic>[],
        <dynamic, dynamic>{"group_id":group_id,"model_id":"profile"}
      )
  },null);
}

action_id(suffix) {
  return "@/view/auth-profile/" + suffix;
}

input(id, label, value, action_id, disabled) {
  return view.node("ui/column",<dynamic, dynamic>{"class":"flex flex-col gap-1.5"},<dynamic>[
    view.node(
      "ui/label",
      <dynamic, dynamic>{"value":label,"for":id},
      <dynamic>[]
    ),
    view.node("ui/input",<dynamic, dynamic>{
      "id":id,
      "value":value ?? "",
      "disabled":true == disabled,
      "on_change":view.action(action_id,view.event_value(<dynamic>["value"]))
    },<dynamic>[])
  ]);
}

button(label, action_id, disabled, pending) {
  return view.node("ui/button",<dynamic, dynamic>{
    "disabled":true == disabled,
    "pending":true == pending,
    "on_press":view.action(action_id,null)
  },<dynamic>[label]);
}

render(snapshot) {
  var session = snapshot["session"];
  var profile_output = snapshot["profile"];
  var profile = (null != profile_output) ? (profile_output["user"] ?? profile_output) : null;
  var current_user = profile ?? ((null != session) ? session["user"] : null);
  var signed_in = null != session;
  var pending = snapshot["pending"];
  var error = snapshot["error"];
  var auth_card = view.node("ui/card",<dynamic, dynamic>{"class":"flex flex-col gap-3.5 p-5"},<dynamic>[
    input(
      "email",
      "Email",
      snapshot["email"],
      action_id("set-email"),
      signed_in
    ),
    input(
      "password",
      "Password",
      snapshot["password"],
      action_id("set-password"),
      signed_in
    ),
    view.node("ui/row",<dynamic, dynamic>{"class":"flex flex-row gap-2.5"},<dynamic>[
      button(
        "Create account",
        action_id("sign-up"),
        signed_in ?? (null != pending),
        pending == "sign-up"
      ),
      button(
        "Sign in",
        action_id("login"),
        signed_in ?? (null != pending),
        pending == "login"
      ),
      button(
        "Sign out",
        action_id("logout"),
        !signed_in || (null != pending),
        pending == "logout"
      )
    ]),
    ((null != error) && (false != error)) ? view.node(
      "ui/alert",
      <dynamic, dynamic>{"variant":"destructive"},
      <dynamic>[error]
    ) : null
  ]);
  var profile_card = view.node("ui/card",<dynamic, dynamic>{"class":"flex flex-col gap-3.5 p-5"},<dynamic>[
    view.node(
      "ui/title",
      <dynamic, dynamic>{"value":"Current session"},
      <dynamic>[]
    ),
    view.node("ui/text",<dynamic, dynamic>{
      "value":((null != signed_in) && (false != signed_in)) ? (current_user["email"] ?? "Authenticated user") : "Not signed in"
    },<dynamic>[]),
    input(
      "display-name",
      "Display name",
      snapshot["display_name"],
      action_id("set-display-name"),
      !signed_in
    ),
    button(
      "Save profile",
      action_id("change-profile"),
      !signed_in || (null != pending),
      pending == "change-profile"
    )
  ]);
  return view.node("ui/column",<dynamic, dynamic>{"class":"auth-profile flex flex-col gap-4 p-6"},<dynamic>[
    view.node(
      "ui/title",
      <dynamic, dynamic>{"value":"Supabase auth profile"},
      <dynamic>[]
    ),
    view.node("ui/description",<dynamic, dynamic>{
      "value":"Authentication and profile state are owned by xt.substrate."
    },<dynamic>[]),
    auth_card,
    profile_card
  ]);
}

error_message(err) {
  return (((err is Map) && ("xt.exception" == ((err as Map)["__type__"]))) ? ((err as Map)["message"]) : null) ?? (err).toString();
}

refresh_visible(node, space_id, group_id) {
  return ((Future.sync(() => page_core.model_refresh(node,space_id,group_id,"session",<dynamic, dynamic>{},null))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    if(null != page_core.model_get_output(node,space_id,group_id,"session")){
      return page_core.model_refresh(node,space_id,group_id,"profile",<dynamic, dynamic>{},null);
    }
    else{
      return null;
    }
  },<dynamic>[value]); });
}

run_model(node, space_id, group_id, model_id, event) {
  view.state_set(
    node,
    space_id,
    "example/auth-profile",
    <dynamic>["pending"],
    model_id
  );
  view.state_set(node,space_id,"example/auth-profile",<dynamic>["error"],null);
  return (() async { try { return await ((Future.sync(() => ((Future.sync(() => ((Future.sync(() => page_core.model_refresh(node,space_id,group_id,model_id,event,null))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return refresh_visible(node,space_id,group_id);
  },<dynamic>[value]); }))) as Future<dynamic>).then((value) async { return await Function.apply((output) {
    view.state_set(node,space_id,"example/auth-profile",<dynamic>["pending"],null);
    return output;
  },<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
    view.state_set(node,space_id,"example/auth-profile",<dynamic>["pending"],null);
    view.state_set(
      node,
      space_id,
      "example/auth-profile",
      <dynamic>["error"],
      error_message(err)
    );
    return null;
  },<dynamic>[err])); } })();
}

register_state_handler(node, action_id, space_id, path) {
  substrate.register_handler(node,action_id,(_space, args, _frame, local_node) {
    return view.state_set(local_node,space_id,"example/auth-profile",path,args[0]);
  },<dynamic, dynamic>{"view_id":"example/auth-profile"});
  return action_id;
}

install(node, opts) {
  opts = (opts ?? <dynamic, dynamic>{});
  var space_id = opts["space_id"] ?? example.DEFAULT_SPACE_ID;
  var group_id = opts["group_id"] ?? example.DEFAULT_GROUP_ID;
  register_state_handler(node,action_id("set-email"),space_id,<dynamic>["email"]);
  register_state_handler(node,action_id("set-password"),space_id,<dynamic>["password"]);
  register_state_handler(
    node,
    action_id("set-display-name"),
    space_id,
    <dynamic>["display_name"]
  );
  substrate.register_handler(node,action_id("sign-up"),(_space, _args, _frame, local_node) {
    var credentials = <dynamic, dynamic>{
      "email":view.state_get(
            local_node,
            space_id,
            "example/auth-profile",
            <dynamic>["email"],
            ""
          ),
      "password":view.state_get(
            local_node,
            space_id,
            "example/auth-profile",
            <dynamic>["password"],
            ""
          )
    };
    return run_model(
      local_node,
      space_id,
      group_id,
      "sign-up",
      <dynamic, dynamic>{"credentials":credentials}
    );
  },<dynamic, dynamic>{"view_id":"example/auth-profile"});
  substrate.register_handler(node,action_id("login"),(_space, _args, _frame, local_node) {
    var credentials = <dynamic, dynamic>{
      "email":view.state_get(
            local_node,
            space_id,
            "example/auth-profile",
            <dynamic>["email"],
            ""
          ),
      "password":view.state_get(
            local_node,
            space_id,
            "example/auth-profile",
            <dynamic>["password"],
            ""
          )
    };
    return run_model(
      local_node,
      space_id,
      group_id,
      "login",
      <dynamic, dynamic>{"credentials":credentials}
    );
  },<dynamic, dynamic>{"view_id":"example/auth-profile"});
  substrate.register_handler(node,action_id("logout"),(_space, _args, _frame, local_node) {
    return run_model(local_node,space_id,group_id,"logout",<dynamic, dynamic>{});
  },<dynamic, dynamic>{"view_id":"example/auth-profile"});
  substrate.register_handler(node,action_id("change-profile"),(_space, _args, _frame, local_node) {
    var display_name = view.state_get(
      local_node,
      space_id,
      "example/auth-profile",
      <dynamic>["display_name"],
      ""
    );
    return run_model(
      local_node,
      space_id,
      group_id,
      "change-profile",
      <dynamic, dynamic>{"profile":<dynamic, dynamic>{"display_name":display_name}}
    );
  },<dynamic, dynamic>{"view_id":"example/auth-profile"});
  view.state_set(
    node,
    space_id,
    "example/auth-profile",
    <dynamic>["email"],
    opts["email"] ?? ""
  );
  view.state_set(
    node,
    space_id,
    "example/auth-profile",
    <dynamic>["password"],
    opts["password"] ?? "secret123"
  );
  view.state_set(
    node,
    space_id,
    "example/auth-profile",
    <dynamic>["display_name"],
    opts["display_name"] ?? "Playground User"
  );
  return spec(space_id,group_id);
}