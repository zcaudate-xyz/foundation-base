const event_common = require("@xtalk/event/base-listener.js")

function new_log_latest(m){
  return event_common.blank_container(
    "event.log-latest",
    Object.assign({"last":null,"cache":{},"interval":30000,"callback":null},m)
  );
}

function clear_cache(log,t){
  if(null == t){
    t = Date.now();
  }
  let {cache,interval,last} = log;
  let out = [];
  if((null != last) && (interval >= (t - last))){
    return out;
  }
  log["last"] = t;
  for(let k of Object.keys(cache)){
    let entry = cache[k];
    if(interval < (t - entry["t"])){
      delete(cache[k]);
      out.push(k);
    }
  };
  return out;
}

function queue_latest(log,key,latest){
  let {cache} = log;
  let entry = cache[key];
  let t = Date.now();
  if(null == entry){
    cache[key] = {"t":t,"latest":latest};
    clear_cache(log,t);
    return true;
  }
  else if(entry["latest"] < latest){
    cache[key] = {"t":t,"latest":latest};
    clear_cache(log,t);
    return true;
  }
  else{
    return false;
  }
}

module.exports = {
  ["new_log_latest"]:new_log_latest,
  ["clear_cache"]:clear_cache,
  ["queue_latest"]:queue_latest
}