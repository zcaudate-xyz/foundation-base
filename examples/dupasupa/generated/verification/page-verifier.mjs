import api from '../next/lib/server.mjs'

import model from '../lib/model.mjs'

import * as fs from 'node:fs'

import * as runtime from './libs/app/manage/page-runtime.mjs'

export var DEFINITIONS = [
  {
  "routes":["/","/web-main"],
  "group_id":"web-main/campaigns",
  "actions":{
    "create":{
      "operation":"create-owned-campaign",
      "operation-spec":{
        "id":"create-owned-campaign",
        "schema":"dupasupa_rpc",
        "input":[
          {"name":"i_user","symbol":"i_user","type":"uuid"},
          {"name":"i_title","symbol":"i_title","type":"text"}
        ],
        "return":"jsonb",
        "flags":{},
        "write":true
      },
      "transport":{
        "method":"POST",
        "path":"/api/campaigns",
        "constants":{"action":"create"},
        "bindings":{"title":"i_title"}
      }
    },
    "save":{
      "operation":"save-owned-campaign",
      "operation-spec":{
        "id":"save-owned-campaign",
        "schema":"dupasupa_rpc",
        "input":[
          {"name":"i_user","symbol":"i_user","type":"uuid"},
          {"name":"i_id","symbol":"i_id","type":"uuid"},
          {"name":"i_title","symbol":"i_title","type":"text"}
        ],
        "return":"jsonb",
        "flags":{},
        "write":true
      },
      "transport":{
        "method":"POST",
        "path":"/api/campaigns",
        "constants":{"action":"save"},
        "bindings":{"id":"i_id","title":"i_title"}
      }
    }
  },
  "models":{
    "rows":{
      "operation":"list-owned-campaigns",
      "operation-spec":{
        "id":"list-owned-campaigns",
        "schema":"dupasupa_rpc",
        "input":[{"name":"i_user","symbol":"i_user","type":"uuid"}],
        "return":"jsonb",
        "flags":{},
        "write":false
      },
      "kind":"dataview",
      "table":"Campaign",
      "transport":{"method":"GET","path":"/api/campaigns"}
    }
  },
  "id":"campaigns",
  "runtime":"Generated Substrate campaign model → verified-session Next handlers → local PostgreSQL. Each scenario owns a disposable database and Auth users.",
  "kind":"screen",
  "order":["rows"],
  "area":"member",
  "target":"next"
},
  {
  "id":"profile",
  "routes":["/profile"],
  "area":"member",
  "kind":"screen",
  "models":{
    "profile":{
      "operation":"ret-profile",
      "operation-spec":{
        "id":"ret-profile",
        "schema":"dupasupa_rpc",
        "input":[{"name":"i_user","symbol":"i_user","type":"uuid"}],
        "return":"jsonb",
        "flags":{},
        "write":false
      },
      "transport":{"method":"GET","path":"/api/profile"}
    }
  },
  "actions":{
    "save":{
      "operation":"save-profile",
      "operation-spec":{
        "id":"save-profile",
        "schema":"dupasupa_rpc",
        "input":[
          {"name":"i_user","symbol":"i_user","type":"uuid"},
          {"name":"i_handle","symbol":"i_handle","type":"text"},
          {"name":"i_name","symbol":"i_name","type":"text"},
          {"name":"i_bio","symbol":"i_bio","type":"text"}
        ],
        "return":"jsonb",
        "flags":{},
        "write":true
      },
      "transport":{
        "method":"POST",
        "path":"/api/profile",
        "bindings":{"handle":"handle","display_name":"display_name","bio":"bio"}
      }
    }
  },
  "target":"next",
  "runtime":"Profile reads use the verified-session Next handler. Profile mutations are tested in the DB/UI scaffold; not yet available in this workbench."
}
];

export async function execute(request){
  let actors = JSON.parse(process.env.DUPASUPA_VERIFY_ACTORS || "{}");
  let handles = {};
  let trace = [];
  let results = {};
  let send = async function (actor,method,path,body){
    let user = actors[actor];
    let headers = {"content-type":"application/json"};
    if(user){
      headers.authorization = ("Bearer " + user.token);
    }
    let req = new Request("http://localhost" + path,{
      "method":method,
      "headers":headers,
      "body":body ? JSON.stringify(body) : undefined
    });
    let routes = {
      "GET /api/profile":api.GET_PROFILE,
      "GET /api/campaigns":api.GET,
      "POST /api/campaigns":api.POST
    };
    let handler = routes[method + " " + path];
    if(!handler){
      throw new Error("Unsupported verified route.");
    }
    let response = await handler(req);
    let value = await response.json();
    trace.push({
      "actor":actor,
      "method":method,
      "path":path,
      "status":response.status
    });
    if(!response.ok){
      throw Object.assign(new Error(value.error),{"status":response.status});
    }
    return value;
  };
  let run = async function (step){
    try{
      let actor = step.actor || "alice";
      if(!actors[actor] && (actor != "anon")){
        return {"blocked":"Choose alice, bob or anon as the fixture actor."};
      }
      let handleFor = function (){
        if(!handles[actor]){
          handles[actor] = model.open({
            "list":function (){
                        let binding = runtime.request(runtime.operation(DEFINITIONS,"list-owned-campaigns"),{});
                        return send(actor,binding.method,binding.path,binding.body);
                      },
            "call":function (operation,values){
                        let entry = runtime.operation(
                          DEFINITIONS,
                          (operation == "save-campaign") ? "save-owned-campaign" : operation
                        );
                        let binding = runtime.request(entry,values);
                        return send(actor,binding.method,binding.path,binding.body);
                      }
          });
        }
        return handles[actor];
      };
      let adapters = {
        "ret-profile":async function (input,entry){
                let binding = runtime.request(entry,input.payload || {});
                return {
                  "result":await send(actor,binding.method,binding.path,binding.body)
                };
              },
        "list-owned-campaigns":async function (){
                let rows = await model.loadRows(handleFor());
                return {"result":{"output":rows,"count":rows.length},"state":rows};
              },
        "create-owned-campaign":async function (input){
                let handle = handleFor();
                let created = await model.create(handle,input.payload.title);
                return {"result":created,"state":model.snapshot(handle)};
              },
        "save-owned-campaign":async function (input){
                let rows = await model.save(handleFor(),input.payload.id,input.payload.title);
                return {
                  "result":rows.find(function (row){
                            return row.id == input.payload.id;
                          }),
                  "state":rows
                };
              }
      };
      return await runtime.execute(DEFINITIONS,adapters,step);
    }
    catch(error){
      return {
        "error":{"message":error.message,"status":error.status || 400}
      };
    }
  };
  try{
    let actual = await runtime.scenario(request,run);
    actual.trace = trace;
    return actual;
  }
  catch(error){
    throw error;
  }
  finally{
    for(let handle of Object.values(handles)){
      model.close(handle);
    };
  }
}

if(process.env.DUPASUPA_PAGE_VERIFY == "1"){
  let request = JSON.parse(fs.readFileSync(0,"utf8"));
  execute(request).then(function (result){
    process.stdout.write(JSON.stringify(result));
  }).catch(function (error){
    process.stderr.write(error.message);
    process.exitCode = 1;
  });
}

export default execute