"use client";
import React from 'react'

import auth from './auth-context.js'

import bindings from '../../lib/bindings.mjs'

export function ProfileEditor(props){
  let [handle,setHandle] = React.useState(props.profile.handle);
  let [name,setName] = React.useState(props.profile.display_name);
  let [bio,setBio] = React.useState(props.profile.bio);
  let [saved,setSaved] = React.useState(props.profile);
  let [status,setStatus] = React.useState("");
  let [busy,setBusy] = React.useState(false);
  React.useEffect(function (){
    if(props.inspect){
      props.inspect({
        "store":{"profile":saved},
        "queries":{"profile":{"output":saved,"busy":busy,"status":status}}
      });
      return function (){
        props.inspect({});
      };
    }
  },[saved,busy,status]);
  let submit = async function (event){
    event.preventDefault();
    if(busy){
      return;
    }
    setBusy(true);
    setStatus("Saving…");
    try{
      let value = await props.save({"handle":handle,"display_name":name,"bio":bio});
      setSaved(value);
      setHandle(value.handle);
      setName(value.display_name);
      setBio(value.bio);
      setStatus("Saved.");
    }
    catch(error){
      setStatus(error.message);
    }
    finally{
      setBusy(false);
    }
  };
  return React.createElement("section",{"className":"campaign"},React.createElement(
    "h2",
    {"data-profile-name":true},
    saved.display_name || saved.handle
  ),React.createElement("p",{"className":"profile-email"},props.email),React.createElement("form",{"onSubmit":submit,"className":"profile-form"},React.createElement("label",{},"Display name",React.createElement("input",{
    "name":"display_name",
    "value":name,
    "required":true,
    "disabled":busy,
    "onChange":function (event){
        setName(event.target.value);
      }
  })),React.createElement("label",{},"Handle",React.createElement("input",{
    "name":"handle",
    "value":handle,
    "required":true,
    "disabled":busy,
    "onChange":function (event){
        setHandle(event.target.value);
      }
  })),React.createElement("label",{},"Bio",React.createElement("textarea",{
    "name":"bio",
    "value":bio,
    "rows":4,
    "disabled":busy,
    "onChange":function (event){
        setBio(event.target.value);
      }
  })),React.createElement(
    "button",
    {"type":"submit","disabled":busy},
    busy ? "Saving…" : "Save profile"
  )),React.createElement("p",{"role":"status","aria-live":"polite"},status));
}

export function Profile(props){
  let account = auth.useAuth();
  let [profile,setProfile] = React.useState(null);
  let [error,setError] = React.useState("");
  let session = account ? account.session : null;
  let request = props.request || (account ? account.request : null);
  React.useEffect(function (){
    let active = true;
    if(session || props.request){
      (function (){
        let bound = bindings.request("profile","model","profile",{});
        return request(bound.path,bound.method,bound.body);
      })().then(function (value){
        if(active){
          setProfile(value);
        }
      }).catch(function (failure){
        if(active){
          setError(failure.message);
        }
      });
    }
    return function (){
      active = false;
    };
  },[]);
  return React.createElement("main",{},React.createElement(
    "header",
    {},
    React.createElement("p",{"className":"eyebrow"},"DUPASUPA"),
    React.createElement("h1",{},"Your profile")
  ),(!session && !props.request) ? React.createElement("p",{},"Sign in using Devtools to view your profile.") : (error ? React.createElement("p",{"role":"alert"},error) : (profile ? React.createElement(ProfileEditor,{
    "profile":profile,
    "inspect":account ? account.inspect : null,
    "email":session ? session.user.email : "",
    "save":function (values){
        let bound = bindings.request("profile","action","save",values);
        return request(bound.path,bound.method,bound.body);
      }
  }) : React.createElement("p",{"role":"status"},"Loading profile…"))));
}

export default Profile