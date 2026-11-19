import React from 'react'

import * as T from 'tamagui'

import 
  {Activity,FlaskConical,LogIn,LogOut,Minus,Monitor,SquareMinus,SquarePlus,User,Wrench}
 from '@tamagui/lucide-icons'

import * as kd from './libs/xt/lang/common-data.mjs'

import * as runtime from './devtools/runtime.mjs'

import * as ui from './devtools/ui.mjs'

import * as ks from './libs/xt/lang/common-string.mjs'

export var FrameDevtoolContext = React.createContext();

export function api_sign_in(client,user){
  return runtime.sign_in(client,user);
}

export function FrameDevtoolLayout({children,color}){
  return React.createElement(T.View,{
    "backgroundColor":color,
    "role":"region",
    "right":15,
    "aria-label":"Runtime devtools",
    "padding":"3px",
    "position":"fixed",
    "borderRadius":"$2",
    "zIndex":1000,
    "bottom":20,
    "opacity":0.95
  },children);
}

export function FrameDevtoolPopup({arrowProps,children,color,contentProps,icon,label,placement}){
  let [open,setOpen] = React.useState(false);
  return React.createElement(ui.PopoverMenu,{
    "open":open,
    "onOpenChange":setOpen,
    "placement":placement || "bottom-start",
    "icon":icon,
    "size":"$4",
    "button":React.createElement(ui.ButtonNormal,{
      "aria-label":label,
      "title":label,
      "icon":icon,
      "backgroundColor":color,
      "size":"$2"
    }),
    "buttonProps":{"size":"$2"},
    "contentProps":{
      "backgroundColor":color,
      "borderWidth":0,
      "gap":"$1",
      "opacity":0.8,
      "padding":"$2",
      ...contentProps
    },
    "arrowProps":{
      "backgroundColor":color,
      "borderWidth":0,
      "size":"$3",
      ...arrowProps
    }
  },children);
}

export function FrameDevtoolButton({children,onPress,...props}){
  return React.createElement(ui.ButtonNormal,Object.assign({},props,{
    "fontSize":"12px",
    "justifyContent":"left",
    "onPress":onPress,
    "size":"$2"
  }),children);
}

export function FrameDevtoolIconButton({children,...props}){
  return React.createElement(ui.ButtonNormal,Object.assign(
    {},
    props,
    {"margin":0,"padding":"3px","size":"$2","width":"24px"}
  ),children);
}

export function FrameDevtoolUserRow({user}){
  let {api,controls,page} = React.useContext(FrameDevtoolContext);
  let backgroundColor = "$color1";
  let {auth} = user;
  let isCurrent = kd.get_in(controls.session,["user","email"]) == kd.get_in(user,["email"]);
  let ncolor = isCurrent ? "$color12" : "$color1";
  let nbackgroundColor = isCurrent ? "$color1" : "$color12";
  return React.createElement(T.XStack,{
    "alignItems":"center",
    "flex":1,
    "paddingHorizontal":2,
    "borderRadius":4,
    "backgroundColor":nbackgroundColor,
    "width":"120px"
  },React.createElement(FrameDevtoolIconButton,{
    "icon":auth ? Minus : null,
    "color":ncolor,
    "backgroundColor":nbackgroundColor,
    "disabled":!auth || !api.capabilities.deleteUser,
    "onPress":function (){
        api.mutations.delete_testuser.mutateAsync({"id":auth.id}).then(function (){
          page.toast.show(ui.t("User Deleted"),{
            "message":JSON.stringify(kd.obj_pick(user,["email","handle","is_super"]))
          });
          if(isCurrent){
            return api.mutations.sign_out.mutateAsync();
          }
        });
      }
  }),React.createElement(FrameDevtoolButton,{
    "flex":1,
    "aria-label":"Sign in as " + user.handle,
    "color":ncolor,
    "backgroundColor":nbackgroundColor,
    "disabled":controls.busy || !user.password || (!auth && !api.capabilities.createUser),
    "onPress":function (){
        if(!auth){
          api.mutations.create_testuser.mutateAsync(user).then(function (res){
            if(res.error){
              throw res.error;
            }
            page.toast.show(ui.t("User Created"),{"message":user.email});
            if(user.is_verified == false){
              return res;
            }
            return api.mutations.sign_in.mutateAsync(user);
          }).catch(function (error){
            page.toast.show(ui.t("User Setup Failed"),{
              "message":error.message || "Could not create or sign in to this test user."
            });
          });
        }
        else if(!isCurrent){
          api.mutations.sign_in.mutateAsync(user).catch(function (error){
            page.toast.show(ui.t("Sign In Failed"),{
              "message":error.message || "Check the test account credentials."
            });
          });
        }
      }
  },user.handle),React.createElement(FrameDevtoolIconButton,{
    "icon":Monitor,
    "label":"SharedWorker",
    "color":ncolor,
    "backgroundColor":nbackgroundColor,
    "onPress":function (){
        console.log(runtime.snapshot(user));
      }
  }));
}

export function FrameDevtoolUser(){
  let {accounts,api,controls} = React.useContext(FrameDevtoolContext);
  let users = accounts;
  let userError = controls.error;
  return React.createElement(FrameDevtoolPopup,{
    "icon":User,
    "label":"Test accounts",
    "color":controls.color,
    "placement":"left-end",
    "contentProps":{"backgroundColor":"$color12"},
    "arrowProps":{"backgroundColor":"$color12"}
  },React.createElement(T.YStack,{"gap":"$1"},userError ? React.createElement(
    T.Text,
    {"fontSize":"8px","color":"$red8","maxWidth":"190px"},
    "Could not load users: " + userError
  ) : null,accounts.map(function (account){
    let auth_user = account.id ? {"id":account.id} : account.auth;
    let user = kd.obj_assign({},account);
    user.auth = auth_user;
    user = kd.obj_assign(user,account);
    let key = account.handle;
    return React.createElement(FrameDevtoolUserRow,{key,user});
  })));
}

export function FrameDevtoolSharedWorker(){
  let {controls,sharedWorker} = React.useContext(FrameDevtoolContext);
  let state = kd.get_in(sharedWorker,["state"]);
  let connection = kd.get_in(state,["connection"]);
  let status = connection ? "connected" : "disconnected";
  let transport_id = kd.get_in(connection,["transport_id"]) || "n/a";
  let transport_kind = kd.get_in(connection,["transport","meta","kind"]) || "n/a";
  let worker = kd.get_in(connection,["ready","worker"]) || "n/a";
  let signal = kd.get_in(connection,["ready","signal"]) || "n/a";
  let init_status = kd.get_in(state,["init","status"]) || "n/a";
  return React.createElement(FrameDevtoolPopup,{
    "icon":Monitor,
    "label":"SharedWorker",
    "color":controls.color,
    "placement":"bottom-end",
    "contentProps":{
        "alignItems":"flex-start",
        "justifyContent":"left",
        "minWidth":"190px"
      }
  },React.createElement(T.YStack,{"gap":"$1"},React.createElement(
    T.Text,
    {"fontFamily":"monospace","fontSize":"8px"},
    "SharedWorker"
  ),React.createElement(
    T.Text,
    {"fontFamily":"monospace","fontSize":"8px"},
    "status: " + status
  ),React.createElement(
    T.Text,
    {"fontFamily":"monospace","fontSize":"8px"},
    "transport: " + transport_kind
  ),React.createElement(
    T.Text,
    {"fontFamily":"monospace","fontSize":"8px"},
    "transport-id: " + transport_id
  ),React.createElement(
    T.Text,
    {"fontFamily":"monospace","fontSize":"8px"},
    "worker: " + worker
  ),React.createElement(
    T.Text,
    {"fontFamily":"monospace","fontSize":"8px"},
    "signal: " + signal
  ),React.createElement(
    T.Text,
    {"fontFamily":"monospace","fontSize":"8px"},
    "init: " + init_status
  ),React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "onPress":function (){
        console.log("SHAREDWORKER",runtime.snapshot(sharedWorker));
      }
  },"Inspect")));
}

export function FrameDevtoolActivity(){
  let context = React.useContext(FrameDevtoolContext);
  let {controls} = context;
  return React.createElement(FrameDevtoolPopup,{
    "icon":Activity,
    "label":"Inspect runtime",
    "color":controls.color,
    "placement":"bottom-end",
    "contentProps":{"alignItems":"flex-start","justifyContent":"left"}
  },React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "onPress":function (){
        console.log("G Store",runtime.snapshot(context.store));
      }
  },"G Store"),React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "onPress":function (){
        console.log("G UI",runtime.snapshot(context.ui));
      }
  },"G UI"),React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "onPress":function (){
        console.log("SESSION",runtime.snapshot(controls.session));
      }
  },"Auth Session"),React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "onPress":function (){
        console.log("TOOL CONTEXT",runtime.snapshot(context));
      }
  },"Tool Context"),React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "onPress":function (){
        console.log(runtime.snapshot(context));
      }
  },"Dev All"),React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "onPress":function (){
        globalThis.DevContext = runtime.snapshot(context.context);
        console.log(runtime.snapshot(context.context));
      }
  },"Dev Context"),React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "data-devtool-action":"queries",
    "onPress":function (){
        globalThis.DevQueries = runtime.snapshot(context.api.queries);
        console.log(globalThis.DevQueries);
      }
  },"Dev Queries"),React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "onPress":function (){
        globalThis.DevOutputs = kd.obj_map(runtime.snapshot(context.api.queries),kd.key_fn("output"));
        console.log(globalThis.DevOutputs);
      }
  },"Dev Outputs"));
}

export function FrameDevtoolWrench(){
  let context = React.useContext(FrameDevtoolContext);
  let {controls,page} = context;
  let {current,setCurrent} = ui.useThemeGlobal();
  return React.createElement(FrameDevtoolPopup,{
    "icon":Wrench,
    "label":"Devtool actions",
    "color":controls.color,
    "placement":"bottom",
    "contentProps":{"alignItems":"flex-start","justifyContent":"left"}
  },React.createElement(FrameDevtoolButton,{
    "disabled":!context.toastConfigured,
    "backgroundColor":controls.color,
    "onPress":function (){
        page.toast.show(
          ui.t("Ping Toast"),
          {"message":ui.t("This is an example of the ping toast")}
        );
      }
  },"Ping Toast"),React.createElement(FrameDevtoolButton,{
    "disabled":!context.themeConfigured,
    "backgroundColor":controls.color,
    "onPress":function (){
        setCurrent((current == "light") ? "dark" : "light");
      }
  },"Theme " + ks.capitalize(current)),(React.useContext(FrameDevtoolContext).actions || []).map(function (action){
    return React.createElement(
      FrameDevtoolButton,
      {"key":action.id,"onPress":action.run},
      action.label
    );
  }));
}

export function FrameDevtoolInfo(){
  let {api,controls,page} = React.useContext(FrameDevtoolContext);
  let {accounts} = React.useContext(FrameDevtoolContext);
  let dims = ui.useWindowDimensions();
  let activeAccount = accounts.find(function (account){
    return account.email == kd.get_in(controls.session,["user","email"]);
  });
  let handle = (activeAccount ? activeAccount.handle : null) || kd.get_in(controls.session,["user","user_metadata","handle"]) || kd.get_in(controls.session,["user","email"]) || "anon";
  return React.createElement(T.XStack,{"padding":"$1","gap":"$1"},React.createElement(T.YStack,{"flex":1},React.createElement(FrameDevtoolButton,{
    "backgroundColor":controls.color,
    "aria-label":controls.session ? "Sign out" : "Sign in",
    "disabled":controls.busy || (!controls.session && (accounts.length == 0)),
    "width":"90px",
    "onPress":function (){
        if(controls.session){
          api.mutations.sign_out.mutateAsync();
        }
        else{
          api.mutations.sign_in.mutateAsync(accounts[0]).catch(function (error){
            page.toast.show(ui.t("Sign In Failed"),{
              "message":error.message || "Check the test account credentials."
            });
          });
        }
      }
  },handle)),React.createElement(T.YStack,{},React.createElement(
    T.Text,
    {"fontFamily":"monospace","fontSize":"8px"},
    "w: " + dims.width
  ),React.createElement(
    T.Text,
    {"fontFamily":"monospace","fontSize":"8px"},
    "h: " + dims.height
  )));
}

export function useFrameDevtoolContext(options){
  return runtime.use_runtime(options);
}

export function useDevtoolInjection(content){
  let {setDebugContent} = React.useContext(FrameDevtoolContext);
  React.useEffect(function (){
    setDebugContent(content);
    return function (){
      setDebugContent(null);
    };
  },[]);
}

export function useDevtoolSharedWorker(resource){
  let context = React.useContext(FrameDevtoolContext);
  let setSharedWorker = context ? context["setSharedWorker"] : null;
  React.useEffect(function (){
    if(setSharedWorker){
      setSharedWorker(resource);
      return function (){
        setSharedWorker(null);
      };
    }
    return undefined;
  },[setSharedWorker,resource]);
}

export function FrameDevtoolScreen(){
  let {controls,debugContent} = React.useContext(FrameDevtoolContext);
  if(!debugContent){
    return null;
  }
  return React.createElement(FrameDevtoolPopup,{
    "icon":FlaskConical,
    "label":"Page debug",
    "color":controls.color,
    "placement":"top-end",
    "contentProps":{"backgroundColor":"$color12"},
    "arrowProps":{"backgroundColor":"$color12"}
  },debugContent);
}

export function FrameDevtoolProvider(props){
  let context = useFrameDevtoolContext(props);
  return React.createElement(FrameDevtoolContext.Provider,{"value":context},React.createElement(
    ui.ThemeGlobalContext.Provider,
    {"value":context.theme},
    props.children
  ));
}

export function FrameDevtoolPanel(){
  let context = React.useContext(FrameDevtoolContext);
  let {controls} = context;
  if(process.env.NODE_ENV == "production"){
    return null;
  }
  return React.createElement(FrameDevtoolLayout,{"color":controls.color},React.createElement(T.YStack,{"gap":"$1"},React.createElement(
    T.XStack,
    {"gap":"$1"},
    (process.env.NODE_ENV != "production") ? React.createElement(FrameDevtoolUser) : null,
    React.createElement(FrameDevtoolWrench),
    React.createElement(FrameDevtoolScreen),
    React.createElement(FrameDevtoolSharedWorker),
    React.createElement(FrameDevtoolActivity)
  ),React.createElement(FrameDevtoolInfo)));
}