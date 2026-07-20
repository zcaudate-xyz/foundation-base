const React = require("react")

const example = require("@xtalk/db/node/example-auth-profile.js")

const xtd = require("@xtalk/lang/common-data.js")

const view_runtime = require("@xtalk/db/react/view/runtime.js")

const page_core = require("@xtalk/substrate/page-core.js")

const shared_view = require("@xtalk/db/node/example-auth-profile-view.js")

function error_message(err){
  return ((err instanceof Error) ? err["message"] : null) || String(err);
}

function refresh_auth_state(node,space_id,group_id,setSession,setProfile){
  return page_core.model_refresh(node,space_id,group_id,"session",{},null).then(function (_){
    let curr_session = page_core.model_get_output(node,space_id,group_id,"session");
    setSession(curr_session);
    if(null != curr_session){
      return page_core.model_refresh(node,space_id,group_id,"profile",{},null).then(function (_){
        let profile_output = page_core.model_get_output(node,space_id,group_id,"profile");
        setProfile(xtd.get_in(profile_output,["user"]));
        return profile_output;
      });
    }
    else{
      setProfile(null);
      return null;
    }
  });
}

function run_action(node,space_id,group_id,model_id,event,setBusy,setError,setSession,setProfile){
  setBusy(model_id);
  setError(null);
  return page_core.model_refresh(node,space_id,group_id,model_id,event || {},null).then(function (_){
    return refresh_auth_state(node,space_id,group_id,setSession,setProfile);
  }).then(function (out){
    setBusy(null);
    return out;
  }).catch(function (err){
    setBusy(null);
    setError(error_message(err));
    return null;
  });
}

function field_style(){
  return {
    "display":"flex",
    "flexDirection":"column",
    "gap":"6px",
    "fontSize":"13px",
    "fontWeight":600,
    "color":"#374151"
  };
}

function input_style(){
  return {
    "border":"1px solid #d1d5db",
    "borderRadius":"8px",
    "padding":"10px 12px",
    "fontSize":"14px",
    "fontWeight":400,
    "outline":"none"
  };
}

function button_style(primary){
  return {
    "color":primary ? "#ffffff" : "#111827",
    "borderRadius":"8px",
    "borderColor":primary ? "#111827" : "#d1d5db",
    "background":primary ? "#111827" : "#ffffff",
    "cursor":"pointer",
    "padding":"9px 13px",
    "fontWeight":600,
    "fontSize":"13px",
    "border":"1px solid"
  };
}

function AuthProfileApp({group_id,initial_email,node,space_id}){
  space_id = (space_id || example.DEFAULT_SPACE_ID);
  group_id = (group_id || example.DEFAULT_GROUP_ID);
  let [email,setEmail] = React.useState(
    initial_email || ("auth-profile-" + String(Date.now()) + "@example.com")
  );
  let [password,setPassword] = React.useState("secret123");
  let [displayName,setDisplayName] = React.useState("Playground User");
  let [session,setSession] = React.useState();
  let [profile,setProfile] = React.useState();
  let [busy,setBusy] = React.useState();
  let [error,setError] = React.useState();
  let credentials = {"email":email,"password":password};
  let current_user = profile || session["user"];
  let current_email = current_user["email"];
  let current_name = xtd.get_in(current_user,["user_metadata","display_name"]);
  let signed_in = null != session;
  React.useEffect(function (){
    refresh_auth_state(node,space_id,group_id,setSession,setProfile);
    return null;
  },[]);
  return (
    <main
      style={{
          "minHeight":"100%",
          "boxSizing":"border-box",
          "padding":"32px",
          "background":"#f3f4f6",
          "fontFamily":"system-ui, sans-serif",
          "color":"#111827"
        }}>
      <div
        style={{
            "maxWidth":"720px",
            "margin":"0 auto",
            "display":"flex",
            "flexDirection":"column",
            "gap":"18px"
          }}>
        <header>
          <div
            style={{
                "fontSize":"12px",
                "fontWeight":700,
                "letterSpacing":"0.08em",
                "textTransform":"uppercase",
                "color":"#6b7280"
              }}>foundation-base / local-min
          </div>
          <h1 style={{"margin":"6px 0 4px","fontSize":"30px"}}>Supabase auth profile</h1>
          <p style={{"margin":0,"color":"#4b5563","lineHeight":1.5}}>
            Create or sign in to a local account, then update the user's auth metadata through substrate page models.
          </p>
        </header>
        <section
          style={{
              "background":"#ffffff",
              "border":"1px solid #e5e7eb",
              "borderRadius":"12px",
              "padding":"20px",
              "display":"flex",
              "flexDirection":"column",
              "gap":"14px",
              "boxShadow":"0 8px 24px rgba(17,24,39,0.05)"
            }}>
          <label style={field_style()}>
            Email
            <input
              style={input_style()}
              value={email}
              disabled={signed_in}
              onChange={function (event){
                  setEmail(event.target.value);
                }}/>
          </label>
          <label style={field_style()}>
            Password
            <input
              style={input_style()}
              type="password"
              value={password}
              disabled={signed_in}
              onChange={function (event){
                  setPassword(event.target.value);
                }}/>
          </label>
          <div style={{"display":"flex","gap":"10px","flexWrap":"wrap"}}>
            <button
              style={button_style(true)}
              disabled={(null != busy) || signed_in}
              onClick={function (){
                  run_action(
                    node,
                    space_id,
                    group_id,
                    "sign-up",
                    {"credentials":credentials},
                    setBusy,
                    setError,
                    setSession,
                    setProfile
                  );
                }}>{(busy == "sign-up") ? "Creating..." : "Create account"}
            </button>
            <button
              style={button_style(false)}
              disabled={(null != busy) || signed_in}
              onClick={function (){
                  run_action(
                    node,
                    space_id,
                    group_id,
                    "login",
                    {"credentials":credentials},
                    setBusy,
                    setError,
                    setSession,
                    setProfile
                  );
                }}>{(busy == "login") ? "Signing in..." : "Sign in"}
            </button>
            <button
              style={button_style(false)}
              disabled={(null != busy) || !signed_in}
              onClick={function (){
                  run_action(
                    node,
                    space_id,
                    group_id,
                    "logout",
                    {},
                    setBusy,
                    setError,
                    setSession,
                    setProfile
                  );
                }}>{(busy == "logout") ? "Signing out..." : "Sign out"}
            </button>
          </div>
          {error ? (
            <div
              style={{
                  "padding":"10px 12px",
                  "borderRadius":"8px",
                  "background":"#fef2f2",
                  "color":"#b91c1c",
                  "fontSize":"13px"
                }}>{error}
            </div>) : null}
        </section>
        <section
          style={{
              "background":"#ffffff",
              "border":"1px solid #e5e7eb",
              "borderRadius":"12px",
              "padding":"20px",
              "display":"flex",
              "flexDirection":"column",
              "gap":"14px"
            }}>
          <div
            style={{
                "display":"flex",
                "justifyContent":"space-between",
                "gap":"16px",
                "alignItems":"flex-start"
              }}>
            <div>
              <h2 style={{"margin":"0 0 4px","fontSize":"18px"}}>Current session</h2>
              <div style={{"color":"#6b7280","fontSize":"13px"}}>
                {signed_in ? (current_email || "Authenticated user") : "Not signed in"}
              </div>
            </div>
            <span
              style={{
                  "padding":"5px 9px",
                  "borderRadius":"999px",
                  "fontSize":"12px",
                  "fontWeight":700,
                  "background":signed_in ? "#dcfce7" : "#f3f4f6",
                  "color":signed_in ? "#166534" : "#6b7280"
                }}>{signed_in ? "signed in" : "signed out"}
            </span>
          </div>
          <label style={field_style()}>
            Display name
            <input
              style={input_style()}
              value={displayName}
              disabled={!signed_in}
              onChange={function (event){
                  setDisplayName(event.target.value);
                }}/>
          </label>
          <button
            style={button_style(true)}
            disabled={(null != busy) || !signed_in}
            onClick={function (){
                run_action(
                  node,
                  space_id,
                  group_id,
                  "change-profile",
                  {"profile":{"display_name":displayName}},
                  setBusy,
                  setError,
                  setSession,
                  setProfile
                );
              }}>{(busy == "change-profile") ? "Saving..." : "Save profile"}
          </button>
          {current_name ? (
            <div style={{"fontSize":"13px","color":"#374151"}}>Stored display name: <strong>{current_name}</strong></div>) : null}
        </section>
      </div>
    </main>);
}

function render_playground(node,opts){
  opts = (opts || {});
  let space_id = opts["space_id"] || example.DEFAULT_SPACE_ID;
  let group_id = opts["group_id"] || example.DEFAULT_GROUP_ID;
  window.PLAYGROUND.setTitle("Supabase auth profile");
  window.PLAYGROUND.setStage((
    <AuthProfileApp
      node={node}
      spaceId={space_id}
      groupId={group_id}
      initialEmail={opts["email"]}/>));
  return node;
}

function render_substrate_view_playground(node,opts){
  opts = (opts || {});
  let spec = shared_view.install(node,opts);
  let space_id = opts["space_id"] || example.DEFAULT_SPACE_ID;
  window.PLAYGROUND.setTitle("Substrate auth profile");
  window.PLAYGROUND.setStage((
    <view_runtime.View
      node={node}
      spec={spec}
      renderFn={shared_view.render}
      options={{"space_id":space_id}}/>));
  return node;
}

function mount_playground(client_defaults,opts){
  opts = (opts || {});
  let service_id = opts["service_id"] || example.DEFAULT_SERVICE_ID;
  let page_args = {
    "space_id":opts["space_id"] || example.DEFAULT_SPACE_ID,
    "group_id":opts["group_id"] || example.DEFAULT_GROUP_ID
  };
  let node = example.create_auth_profile_node(client_defaults,service_id,page_args);
  render_playground(node,opts);
  return node;
}

module.exports = {
  ["error_message"]:error_message,
  ["refresh_auth_state"]:refresh_auth_state,
  ["run_action"]:run_action,
  ["field_style"]:field_style,
  ["input_style"]:input_style,
  ["button_style"]:button_style,
  ["AuthProfileApp"]:AuthProfileApp,
  ["render_playground"]:render_playground,
  ["render_substrate_view_playground"]:render_substrate_view_playground,
  ["mount_playground"]:mount_playground
}