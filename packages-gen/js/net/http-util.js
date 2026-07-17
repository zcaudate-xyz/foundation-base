function request_body(body){
  if(null == body){
    return null;
  }
  else if("string" == (typeof body)){
    return body;
  }
  else{
    return JSON.stringify(body);
  }
}

function decode_body(body){
  if(!("string" == (typeof body))){
    return body;
  }
  else if("" == body){
    return null;
  }
  else{
    try{
      return JSON.parse(body);
    }
    catch(err){
      return body;
    }
  }
}

function request_prepare(input){
  let {body,headers,method} = input || {};
  return {"method":method || "GET","headers":headers,"body":body || ""};
}

function response_normalize(response){
  if(null == response){
    return {"status":null,"headers":{},"body":null,"error":null};
  }
  else if(((null != response) && ("object" == (typeof response)) && !Array.isArray(response)) && (null != response["body"])){
    let out = Object.assign({},response);
    out["headers"] = Object.assign({},out["headers"]);
    out["body"] = decode_body(out["body"]);
    return out;
  }
  else if((null != response) && ("object" == (typeof response)) && !Array.isArray(response)){
    return response;
  }
  else{
    return {
      "status":null,
      "headers":{},
      "body":decode_body(response),
      "error":null
    };
  }
}

function encode_query_params(params){
  let out = [];
  for(let [k,v] of Object.entries(params || {})){
    if(null != v){
      out.push(k + "=" + String(v));
    }
  };
  return out.join("&");
}

function get_body_data(response){
  let out = response["body"];
  if(((null != out) && ("object" == (typeof out)) && !Array.isArray(out)) && (null != out["data"])){
    return out["data"];
  }
  else{
    return out;
  }
}

module.exports = {
  ["request_body"]:request_body,
  ["decode_body"]:decode_body,
  ["request_prepare"]:request_prepare,
  ["response_normalize"]:response_normalize,
  ["encode_query_params"]:encode_query_params,
  ["get_body_data"]:get_body_data
}