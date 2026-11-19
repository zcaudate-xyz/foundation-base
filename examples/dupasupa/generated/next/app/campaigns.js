"use client";
import React from 'react'

import auth from './auth-context.js'

import bindings from '../../lib/bindings.mjs'

import model from '../../lib/model.mjs'

export function CampaignForm(props){
  let [title,setTitle] = React.useState(props.row.title);
  let [status,setStatus] = React.useState("");
  let [busy,setBusy] = React.useState(false);
  React.useEffect(function (){
    setTitle(props.row.title);
  },[props.row.title]);
  let submit = async function (event){
    event.preventDefault();
    if(busy){
      return;
    }
    setBusy(true);
    setStatus("Saving…");
    try{
      await props.save(props.row.id,title);
      setStatus("Saved.");
    }
    catch(error){
      setStatus(error.message);
    }
    finally{
      setBusy(false);
    }
  };
  return React.createElement("section",{"className":"campaign"},React.createElement("h2",{"data-saved-title":true},props.row.title),React.createElement("form",{"onSubmit":submit},React.createElement("label",{},"Campaign title",React.createElement("input",{
    "name":"title",
    "required":true,
    "disabled":busy,
    "value":title,
    "onChange":function (event){
        setTitle(event.target.value);
      }
  })),React.createElement(
    "button",
    {"type":"submit","disabled":busy},
    busy ? "Saving…" : "Save"
  )),React.createElement("p",{"role":"status","aria-live":"polite"},status));
}

export function CampaignCreate(props){
  let [title,setTitle] = React.useState("");
  let [busy,setBusy] = React.useState(false);
  let [status,setStatus] = React.useState("");
  let submit = async function (event){
    event.preventDefault();
    if(busy){
      return;
    }
    setBusy(true);
    setStatus("Creating…");
    try{
      await props.create(title);
      setTitle("");
      setStatus("Campaign created.");
    }
    catch(error){
      setStatus(error.message);
    }
    finally{
      setBusy(false);
    }
  };
  return React.createElement("section",{"className":"campaign"},React.createElement("h2",{},"New campaign"),React.createElement("form",{"onSubmit":submit,"aria-label":"Create campaign"},React.createElement("label",{},"New campaign title",React.createElement("input",{
    "name":"new-title",
    "value":title,
    "required":true,
    "maxLength":120,
    "disabled":busy,
    "onChange":function (event){
        setTitle(event.target.value);
      }
  })),React.createElement(
    "button",
    {"type":"submit","disabled":busy},
    busy ? "Creating…" : "Create campaign"
  )),React.createElement("p",{"role":"status","aria-live":"polite"},status));
}

export function Campaigns(props){
  let account = auth.useAuth();
  let allowed = props.transport || (account && account.session);
  let [rows,setRows] = React.useState([]);
  let [error,setError] = React.useState("");
  let [loading,setLoading] = React.useState(true);
  let handle = React.useRef(null);
  React.useEffect(function (){
    if(!allowed){
      return;
    }
    let active = true;
    let transport = props.transport || {
      "list":function (){
            let bound = bindings.request("campaigns","model","rows",{});
            return account.request(bound.path,bound.method,bound.body);
          },
      "call":function (operation,values){
            let request = bindings.request(
              "campaigns",
              "action",
              (operation == "create-owned-campaign") ? "create" : "save",
              values
            );
            return account.request(request.path,request.method,request.body);
          }
    };
    let owner = model.open(transport);
    handle.current = owner;
    model.loadRows(owner).then(function (value){
      if(active){
        setRows(value);
        setLoading(false);
      }
    }).catch(function (failure){
      if(active){
        setError(failure.message);
        setLoading(false);
      }
    });
    return function (){
      active = false;
      model.close(owner);
      handle.current = null;
    };
  },[]);
  React.useEffect(function (){
    if(account && account.inspect){
      account.inspect({
        "store":{"campaigns":handle.current},
        "queries":{"campaigns":{"output":rows,"loading":loading,"error":error}}
      });
      return function (){
        account.inspect({});
      };
    }
  },[rows,loading,error]);
  let create = async function (title){
    let created = await model.create(handle.current,title);
    setRows(model.snapshot(handle.current));
    setError("");
    return created;
  };
  let refresh = async function (){
    setLoading(true);
    setError("");
    try{
      setRows(await model.loadRows(handle.current));
    }
    catch(failure){
      setError(failure.message);
    }
    finally{
      setLoading(false);
    }
  };
  let save = async function (id,title){
    let value = await model.save(handle.current,id,title);
    setRows(value);
    return value;
  };
  if(!allowed){
    return React.createElement(
      "main",
      {},
      React.createElement("h1",{},"Campaigns"),
      React.createElement("p",{},"Sign in using Devtools to see your campaigns.")
    );
  }
  return React.createElement("main",{},React.createElement(
    "header",
    {},
    React.createElement("p",{"className":"eyebrow"},"DUPASUPA"),
    React.createElement("h1",{},"Campaigns"),
    React.createElement("p",{},"Saved titles are stored in PostgreSQL.")
  ),loading ? React.createElement("p",{"role":"status"},"Loading campaigns…") : null,error ? React.createElement("p",{"role":"alert"},error) : null,(!loading && !error && (rows.length == 0)) ? React.createElement("p",{},"No campaigns yet. Create your first campaign below.") : null,rows.map(function (row){
    return React.createElement(CampaignForm,{"key":row.id,"row":row,"save":save});
  }),!loading ? React.createElement(CampaignCreate,{"create":create}) : null,React.createElement(
    "button",
    {"type":"button","onClick":refresh,"disabled":loading},
    loading ? "Refreshing…" : "Refresh campaigns"
  ));
}

export default Campaigns