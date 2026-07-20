function frame_kindp(kind){
  return (kind == "request") || (kind == "response") || (kind == "stream") || (kind == "subscribe") || (kind == "unsubscribe");
}

function valid_framep(value){
  if(!((null != value) && ("object" == (typeof value)) && !Array.isArray(value))){
    return false;
  }
  let kind = value["kind"];
  let id = value["id"];
  let space = value["space"];
  let meta = value["meta"];
  if(!frame_kindp(kind) || !("string" == (typeof id)) || !("string" == (typeof space)) || ((null != meta) && !((null != meta) && ("object" == (typeof meta)) && !Array.isArray(meta)))){
    return false;
  }
  if(kind == "request"){
    return ("string" == (typeof value["action"])) && ((null == value["args"]) || Array.isArray(value["args"]));
  }
  else if(kind == "response"){
    return ("string" == (typeof value["reply_to"])) && ("string" == (typeof value["status"]));
  }
  else{
    return "string" == (typeof value["signal"]);
  }
}

function normalize_error(err){
  if(null == err){
    return null;
  }
  else if("string" == (typeof err)){
    return {"message":err};
  }
  else if((null != err) && ("object" == (typeof err)) && !Array.isArray(err)){
    let out = Object.assign({},err);
    if(!(null != out["message"]) && (null != out["error"]) && ("string" == (typeof out["error"]))){
      out["message"] = out["error"];
    }
    if(!(null != out["message"]) && (null != out["status"]) && ("string" == (typeof out["status"]))){
      out["message"] = out["status"];
    }
    if(!(null != out["message"])){
      out["message"] = String(err);
    }
    return out;
  }
  else{
    return {"message":String(err)};
  }
}

function normalize_frame(frame){
  if(!((null != frame) && ("object" == (typeof frame)) && !Array.isArray(frame))){
    return frame;
  }
  let out = Object.assign({},frame);
  if(("response" == out["kind"]) && ("error" == out["status"]) && (null != out["error"])){
    out["error"] = normalize_error(out["error"]);
  }
  return out;
}

function encode_frame(frame){
  let out = normalize_frame(frame);
  if(!valid_framep(out)){
    throw "invalid node json frame";
  }
  return JSON.stringify(out);
}

function decode_frame(input){
  let out = ("string" == (typeof input)) ? JSON.parse(input) : input;
  if(!valid_framep(out)){
    throw "invalid node json frame";
  }
  return out;
}

module.exports = {
  ["frame_kindp"]:frame_kindp,
  ["valid_framep"]:valid_framep,
  ["normalize_error"]:normalize_error,
  ["normalize_frame"]:normalize_frame,
  ["encode_frame"]:encode_frame,
  ["decode_frame"]:decode_frame
}