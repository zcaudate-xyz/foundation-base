import React from 'react'

export function snapshot(input){
  let seen = new WeakSet();
  let walk = function (value,depth){
    if((typeof value) == "function"){
      return "[function]";
    }
    if((value == null) || ((typeof value) != "object")){
      return value;
    }
    if(depth > 8){
      return "[depth limit]";
    }
    if(seen.has(value)){
      return "[circular]";
    }
    seen.add(value);
    if(Array.isArray(value)){
      return value.map(function (item){
        return walk(item,depth + 1);
      });
    }
    let result = {};
    for(let key of Object.keys(value)){
      result[key] = (new RegExp(
        "password|token|secret|authorization|cookie|api.?key|^key$",
        "i"
      ).test(key) ? "[redacted]" : walk(value[key],depth + 1));
    };
    return result;
  };
  return walk(input,0);
}

export async function sign_in(client,account){
  if(!client){
    throw new Error("Supply the application's Supabase client.");
  }
  if(!account || !account.password){
    throw new Error("This account has no password configured.");
  }
  let result = await client.auth.signInWithPassword({"email":account.email,"password":account.password});
  if(result.error){
    throw result.error;
  }
  return result;
}

export async function invoke(action,input){
  if(!action){
    throw new Error("This devtool action is not configured.");
  }
  let result = await action(input);
  if(result && result.error){
    throw result.error;
  }
  return result;
}

export function use_runtime(options){
  let [session,setSession] = React.useState(null);
  let [busy,setBusy] = React.useState(false);
  let [error,setError] = React.useState(null);
  let [debugContent,setDebugContent] = React.useState(null);
  let [sharedWorker,setSharedWorker] = React.useState(options.sharedWorker || null);
  let client = options.client;
  React.useEffect(function (){
    let active = true;
    let subscription = null;
    if(client){
      subscription = client.auth.onAuthStateChange(function (_,value){
        if(active){
          setSession(value);
        }
      }).data.subscription;
      client.auth.getSession().then(function (result){
        if(active){
          if(result.error){
            setError(result.error.message);
          }
          else{
            setSession(result.data.session);
          }
        }
      }).catch(function (failure){
        if(active){
          setError(failure.message);
        }
      });
    }
    return function (){
      active = false;
      if(subscription){
        subscription.unsubscribe();
      }
    };
  },[client]);
  React.useEffect(function (){
    setSharedWorker(options.sharedWorker || null);
  },[options.sharedWorker]);
  let toast = options.toast || {
    "show":function (title,details){
        console.info(title,details);
      }
  };
  let run = async function (action,input){
    setBusy(true);
    setError(null);
    try{
      let result = await invoke(action,input);
      return result;
    }
    catch(failure){
      setError(failure.message);
      throw failure;
    }
    finally{
      setBusy(false);
    }
  };
  let api = {
    "capabilities":{
        "createUser":!!options.createUser,
        "deleteUser":!!options.deleteUser
      },
    "queries":options.queries || {},
    "mutations":{
        "sign_in":{
            "mutateAsync":function (account){
                return run(function (value){
                  return sign_in(client,value);
                },account);
              }
          },
        "sign_out":{
            "mutateAsync":function (){
                return run(function (){
                  if(!client){
                    throw new Error("Supply the application's Supabase client.");
                  }
                  return client.auth.signOut();
                },null);
              }
          },
        "create_testuser":{
            "mutateAsync":function (account){
                return run(options.createUser,account);
              }
          },
        "delete_testuser":{
            "mutateAsync":function (account){
                return run(options.deleteUser,account);
              }
          }
      }
  };
  return {
    "themeConfigured":!!options.theme,
    "api":api,
    "setSharedWorker":setSharedWorker,
    "store":options.store || {},
    "controls":{
        "session":session,
        "setSession":setSession,
        "busy":busy,
        "error":error,
        "color":session ? "$green8" : "$color1"
      },
    "page":{"toast":toast},
    "actions":options.actions || [],
    "theme":options.theme || {
        "current":"light",
        "setCurrent":function (){
            
          }
      },
    "debugContent":debugContent,
    "ui":options.ui || {},
    "context":options.context || {},
    "sharedWorker":sharedWorker,
    "toastConfigured":!!options.toast,
    "setDebugContent":setDebugContent,
    "accounts":options.accounts || []
  };
}