const example = require("@xtalk/db/node/example-auth-profile.js")

const page_core = require("@xtalk/substrate/page-core.js")

const view = require("@xtalk/substrate/view.js")

const substrate = require("@xtalk/substrate/substrate.js")

function make_binding(source,space_id,path,opts){
  return Object.assign(
    {"source":source,"space_id":space_id,"path":path || []},
    opts || {}
  );
}

function spec(space_id,group_id){
  space_id = (space_id || example.DEFAULT_SPACE_ID);
  group_id = (group_id || example.DEFAULT_GROUP_ID);
  return view.view_spec("example/auth-profile",{
    "email":make_binding("state",space_id,["email"],{"default":""}),
    "password":make_binding("state",space_id,["password"],{"default":""}),
    "display_name":make_binding("state",space_id,["display_name"],{"default":""}),
    "pending":make_binding("state",space_id,["pending"],{}),
    "error":make_binding("state",space_id,["error"],{}),
    "session":make_binding(
        "model-output",
        space_id,
        [],
        {"group_id":group_id,"model_id":"session"}
      ),
    "profile":make_binding(
        "model-output",
        space_id,
        [],
        {"group_id":group_id,"model_id":"profile"}
      )
  },null);
}

function action_id(suffix){
  return "@/view/auth-profile/" + suffix;
}

function input(id,label,value,action_id,disabled){
  return view.node("ui/column",{"class":"flex flex-col gap-1.5"},[
    view.node("ui/label",{"value":label,"for":id},[]),
    view.node("ui/input",{
      "id":id,
      "value":value || "",
      "disabled":true == disabled,
      "on_change":view.action(action_id,view.event_value(["value"]))
    },[])
  ]);
}

function button(label,action_id,disabled,pending){
  return view.node("ui/button",{
    "disabled":true == disabled,
    "pending":true == pending,
    "on_press":view.action(action_id,null)
  },[label]);
}

function render(snapshot){
  let session = snapshot["session"];
  let profile_output = snapshot["profile"];
  let profile = (null != profile_output) ? (profile_output["user"] || profile_output) : null;
  let current_user = profile || ((null != session) ? session["user"] : null);
  let signed_in = null != session;
  let pending = snapshot["pending"];
  let error = snapshot["error"];
  let auth_card = view.node("ui/card",{"class":"flex flex-col gap-3.5 p-5"},[
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
    view.node("ui/row",{"class":"flex flex-row gap-2.5"},[
      button(
        "Create account",
        action_id("sign-up"),
        signed_in || (null != pending),
        pending == "sign-up"
      ),
      button(
        "Sign in",
        action_id("login"),
        signed_in || (null != pending),
        pending == "login"
      ),
      button(
        "Sign out",
        action_id("logout"),
        !signed_in || (null != pending),
        pending == "logout"
      )
    ]),
    error ? view.node("ui/alert",{"variant":"destructive"},[error]) : null
  ]);
  let profile_card = view.node("ui/card",{"class":"flex flex-col gap-3.5 p-5"},[
    view.node("ui/title",{"value":"Current session"},[]),
    view.node("ui/text",{
      "value":signed_in ? (current_user["email"] || "Authenticated user") : "Not signed in"
    },[]),
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
  return view.node("ui/column",{"class":"auth-profile flex flex-col gap-4 p-6"},[
    view.node("ui/title",{"value":"Supabase auth profile"},[]),
    view.node("ui/description",{
      "value":"Authentication and profile state are owned by xt.substrate."
    },[]),
    auth_card,
    profile_card
  ]);
}

function error_message(err){
  return ((err instanceof Error) ? err["message"] : null) || String(err);
}

function refresh_visible(node,space_id,group_id){
  return page_core.model_refresh(node,space_id,group_id,"session",{},null).then(function (_){
    if(null != page_core.model_get_output(node,space_id,group_id,"session")){
      return page_core.model_refresh(node,space_id,group_id,"profile",{},null);
    }
    else{
      return null;
    }
  });
}

function run_model(node,space_id,group_id,model_id,event){
  view.state_set(node,space_id,"example/auth-profile",["pending"],model_id);
  view.state_set(node,space_id,"example/auth-profile",["error"],null);
  return page_core.model_refresh(node,space_id,group_id,model_id,event,null).then(function (_){
    return refresh_visible(node,space_id,group_id);
  }).then(function (output){
    view.state_set(node,space_id,"example/auth-profile",["pending"],null);
    return output;
  }).catch(function (err){
    view.state_set(node,space_id,"example/auth-profile",["pending"],null);
    view.state_set(
      node,
      space_id,
      "example/auth-profile",
      ["error"],
      error_message(err)
    );
    return null;
  });
}

function register_state_handler(node,action_id,space_id,path){
  substrate.register_handler(node,action_id,function (_space,args,_frame,local_node){
    return view.state_set(local_node,space_id,"example/auth-profile",path,args[0]);
  },{"view_id":"example/auth-profile"});
  return action_id;
}

function install(node,opts){
  opts = (opts || {});
  let space_id = opts["space_id"] || example.DEFAULT_SPACE_ID;
  let group_id = opts["group_id"] || example.DEFAULT_GROUP_ID;
  register_state_handler(node,action_id("set-email"),space_id,["email"]);
  register_state_handler(node,action_id("set-password"),space_id,["password"]);
  register_state_handler(node,action_id("set-display-name"),space_id,["display_name"]);
  substrate.register_handler(node,action_id("sign-up"),function (_space,_args,_frame,local_node){
    let credentials = {
      "email":view.state_get(local_node,space_id,"example/auth-profile",["email"],""),
      "password":view.state_get(local_node,space_id,"example/auth-profile",["password"],"")
    };
    return run_model(
      local_node,
      space_id,
      group_id,
      "sign-up",
      {"credentials":credentials}
    );
  },{"view_id":"example/auth-profile"});
  substrate.register_handler(node,action_id("login"),function (_space,_args,_frame,local_node){
    let credentials = {
      "email":view.state_get(local_node,space_id,"example/auth-profile",["email"],""),
      "password":view.state_get(local_node,space_id,"example/auth-profile",["password"],"")
    };
    return run_model(
      local_node,
      space_id,
      group_id,
      "login",
      {"credentials":credentials}
    );
  },{"view_id":"example/auth-profile"});
  substrate.register_handler(node,action_id("logout"),function (_space,_args,_frame,local_node){
    return run_model(local_node,space_id,group_id,"logout",{});
  },{"view_id":"example/auth-profile"});
  substrate.register_handler(node,action_id("change-profile"),function (_space,_args,_frame,local_node){
    let display_name = view.state_get(local_node,space_id,"example/auth-profile",["display_name"],"");
    return run_model(
      local_node,
      space_id,
      group_id,
      "change-profile",
      {"profile":{"display_name":display_name}}
    );
  },{"view_id":"example/auth-profile"});
  view.state_set(
    node,
    space_id,
    "example/auth-profile",
    ["email"],
    opts["email"] || ""
  );
  view.state_set(
    node,
    space_id,
    "example/auth-profile",
    ["password"],
    opts["password"] || "secret123"
  );
  view.state_set(
    node,
    space_id,
    "example/auth-profile",
    ["display_name"],
    opts["display_name"] || "Playground User"
  );
  return spec(space_id,group_id);
}

module.exports = {
  ["make_binding"]:make_binding,
  ["spec"]:spec,
  ["action_id"]:action_id,
  ["input"]:input,
  ["button"]:button,
  ["render"]:render,
  ["error_message"]:error_message,
  ["refresh_visible"]:refresh_visible,
  ["run_model"]:run_model,
  ["register_state_handler"]:register_state_handler,
  ["install"]:install
}