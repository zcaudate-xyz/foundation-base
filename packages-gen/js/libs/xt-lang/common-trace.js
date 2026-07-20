const xtd = require("@xtalk/lang/common-data.js")

function trace_log(){
  if(!(null == globalThis["TRACE"])){
    return globalThis["TRACE"];
  }
  else{
    globalThis["TRACE"] = [];
    return globalThis["TRACE"];
  }
}

function trace_log_clear(){
  globalThis["TRACE"] = [];
  return globalThis["TRACE"];
}

function trace_log_add(data,tag,opts){
  let log = trace_log();
  let m = xtd.obj_assign({"tag":tag,"data":data,"time":Date.now()},opts);
  log.push(m);
  return log.length;
}

function trace_filter(tag){
  return trace_log().filter(function (e){
    return tag == e["tag"];
  });
}

function trace_last_entry(tag){
  let log = trace_log();
  if(null == tag){
    return log[log.length + -1];
  }
  else{
    let tagged = trace_filter(tag);
    return tagged[tagged.length + -1];
  }
}

function trace_data(tag){
  return trace_log().map(function (e){
    return e["data"];
  });
}

function trace_last(tag){
  return (trace_last_entry(tag))["data"];
}

function trace_run(f){
  trace_log_clear();
  f();
  return trace_log();
}

module.exports = {
  ["trace_log"]:trace_log,
  ["trace_log_clear"]:trace_log_clear,
  ["trace_log_add"]:trace_log_add,
  ["trace_filter"]:trace_filter,
  ["trace_last_entry"]:trace_last_entry,
  ["trace_data"]:trace_data,
  ["trace_last"]:trace_last,
  ["trace_run"]:trace_run
}