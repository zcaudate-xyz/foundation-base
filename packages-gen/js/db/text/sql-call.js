const check = require("@xtalk/db/text/base-check.js")

const ut = require("@xtalk/db/text/sql-util.js")

const conn_sql = require("@xtalk/net/conn-sql.js")

function decode_return(outstr,alt){
  let out = JSON.parse(outstr);
  let {data,status} = out;
  if("error" == status){
    throw "ERR - API: " + outstr;
  }
  return data;
}

function call_format_input(spec,args){
  let targs = spec["input"];
  let out = [];
  for(let i = 0; i < args.length; ++i){
    let arg = args[i];
    let input = targs[i];
    let dbarg = null;
    if(input["type"] == "jsonb"){
      if("string" == (typeof arg)){
        dbarg = arg;
      }
      else{
        dbarg = ut.encode_json(arg);
      }
    }
    else{
      dbarg = ut.encode_value(arg);
    }
    out.push(dbarg);
  };
  return out;
}

function call_format_query(spec,args){
  let {id,schema} = spec;
  let dbname = "\"" + schema + "\"." + id.replace(new RegExp("-","g"),"_") + "";
  let dbargs = call_format_input(spec,args).join(", ");
  return "SELECT " + dbname + "(" + dbargs + ");";
}

function call_raw(client,spec,args){
  let targs = spec["input"];
  let [l_ok,l_err] = check.check_args_length(args,targs);
  if(!l_ok){
    throw "ERR: - " + JSON.stringify(l_err);
  }
  let [t_ok,t_err] = check.check_args_type(args,targs);
  if(!t_ok){
    throw "ERR: - " + JSON.stringify(t_err);
  }
  let q = call_format_query(spec,args);
  let success_fn = function (val){
    if("jsonb" == spec["return"]){
      if((null == val) || (val == "")){
        return null;
      }
      else{
        return ("string" == (typeof val)) ? JSON.parse(val) : val;
      }
    }
    else{
      return val;
    }
  };
  let error_fn = function (err){
    throw "ERR: - " + JSON.stringify(err);
  };
  return conn_sql.query_async(client,q).then(success_fn).catch(error_fn);
}

function call_api(client,spec,args){
  let targs = spec["input"];
  let [l_ok,l_err] = check.check_args_length(args,targs);
  if(!l_ok){
    return JSON.stringify({"status":"error","data":l_err});
  }
  let [t_ok,t_err] = check.check_args_type(args,targs);
  if(!t_ok){
    return JSON.stringify({"status":"error","data":t_err});
  }
  let q = call_format_query(spec,args);
  let success_fn = function (val){
    return "{\"status\": \"ok\", \"data\":" + (("jsonb" == spec["return"]) ? (("string" == (typeof val)) ? val : JSON.stringify(val)) : JSON.stringify(val)) + "}";
  };
  let error_fn = function (err){
    if(err["status"]){
      return JSON.stringify(err);
    }
    else{
      return JSON.stringify({"status":"error","data":err});
    }
  };
  return conn_sql.query_async(client,q).then(success_fn).catch(error_fn);
}

module.exports = {
  ["decode_return"]:decode_return,
  ["call_format_input"]:call_format_input,
  ["call_format_query"]:call_format_query,
  ["call_raw"]:call_raw,
  ["call_api"]:call_api
}