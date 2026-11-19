"use client";
import {TamaguiProvider,createTamagui} from 'tamagui'

import {defaultConfig} from '@tamagui/config/v4'

import React from 'react'

import {createClient} from '@supabase/supabase-js'

import {AuthContext} from './auth-context.js'

import {FrameDevtoolPanel,FrameDevtoolProvider} from '../lib/devtools/devtools.mjs'

export var toolConfig = createTamagui(defaultConfig);

export function AuthProvider(props){
  let [session,setSession] = React.useState(null);
  let [client,setClient] = React.useState(null);
  let [accounts,setAccounts] = React.useState([]);
  let [inspection,setInspection] = React.useState({});
  let [error,setError] = React.useState("");
  let [ready,setReady] = React.useState(false);
  let current = React.useRef(null);
  let initialized = React.useRef(false);
  React.useEffect(function (){
    let active = true;
    let subscription = null;
    let api = null;
    fetch("/api/auth/config",{"cache":"no-store"}).then(function (response){
      return response.json();
    }).then(async function (config){
      if(!active){
        return;
      }
      if(config.error){
        throw new Error(config.error);
      }
      api = createClient(config.url,config.key,{"auth":{"storageKey":"dupasupa-auth"}});
      setClient(api);
      setAccounts(config.accounts || []);
      subscription = api.auth.onAuthStateChange(function (_,value){
        if(active && initialized.current){
          if((current.current ? current.current.user.id : null) != (value ? value.user.id : null)){
            setInspection({});
          }
          current.current = value;
          setSession(value);
        }
      }).data.subscription;
      let result = await api.auth.getSession();
      if(result.error){
        throw result.error;
      }
      if(result.data.session){
        let verified = await api.auth.getUser(result.data.session.access_token);
        if(verified.error){
          await api.auth.signOut({"scope":"local"});
          result.data.session = null;
        }
      }
      initialized.current = true;
      if(active){
        current.current = result.data.session;
        setSession(result.data.session);
        setReady(true);
      }
    }).catch(function (failure){
      if(active){
        setError(failure.message);
        setReady(true);
      }
    });
    return function (){
      active = false;
      if(subscription){
        subscription.unsubscribe();
      }
      if(api){
        api.auth.stopAutoRefresh();
      }
    };
  },[]);
  let request = async function (path,method,body){
    let token = current.current ? current.current.access_token : null;
    if(!token){
      throw new Error("Sign in to continue.");
    }
    let response = await fetch(path,{
      "method":method,
      "cache":"no-store",
      "headers":{
            "content-type":"application/json",
            "authorization":"Bearer " + token
          },
      "body":body ? JSON.stringify(body) : undefined
    });
    let value = await response.json();
    if(!response.ok){
      throw new Error(value.error || "Request failed.");
    }
    return value;
  };
  return React.createElement(AuthContext.Provider,{
    "value":{
        "session":session,
        "ready":ready,
        "request":request,
        "inspect":setInspection
      }
  },React.createElement(FrameDevtoolProvider,{
    "client":client,
    "accounts":accounts,
    "store":session ? inspection.store : {},
    "queries":session ? inspection.queries : {},
    "context":{"application":"dupasupa","ready":ready,"error":error},
    "ui":{"actor":session ? session.user.id : null}
  },React.createElement(
    "nav",
    {"className":"app-nav"},
    React.createElement("a",{"href":"/web-main"},"Campaigns"),
    React.createElement("a",{"href":"/profile"},"Profile")
  ),React.createElement(
    "div",
    {"key":session ? session.user.id : "signed-out"},
    props.children
  ),(ready && (process.env.NODE_ENV == "development")) ? React.createElement(
    TamaguiProvider,
    {"config":toolConfig,"defaultTheme":"light"},
    React.createElement(FrameDevtoolPanel)
  ) : null));
}

export default AuthProvider