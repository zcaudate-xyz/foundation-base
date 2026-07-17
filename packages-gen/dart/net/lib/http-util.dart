request_body(body) {
  if(null == body){
    return null;
  }
  else if("String" == (body.runtimeType).toString()){
    return body;
  }
  else{
    return jsonEncode(body);
  }
}

decode_body(body) {
  if(!("String" == (body.runtimeType).toString())){
    return body;
  }
  else if("" == body){
    return null;
  }
  else{
    try{
      return jsonDecode(body);
    }
    catch(err){
      return body;
    }
  }
}

request_prepare(input) {
  var body = (input ?? <dynamic, dynamic>{})["body"];
  var headers = (input ?? <dynamic, dynamic>{})["headers"];
  var method = (input ?? <dynamic, dynamic>{})["method"];
  return <dynamic, dynamic>{"method":method ?? "GET","headers":headers,"body":body ?? ""};
}

response_normalize(response) {
  if(null == response){
    return <dynamic, dynamic>{
      "status":null,
      "headers":<dynamic, dynamic>{},
      "body":null,
      "error":null
    };
  }
  else if((("Map" == (response.runtimeType).toString()) || (response.runtimeType).toString().startsWith("_Map") || (response.runtimeType).toString().startsWith("LinkedMap")) && response.containsKey("body")){
    var out = xtd.obj_clone(response);
    out["headers"] = xtd.obj_assign(<dynamic, dynamic>{},out["headers"]);
    out["body"] = decode_body(out["body"]);
    return out;
  }
  else if(("Map" == (response.runtimeType).toString()) || (response.runtimeType).toString().startsWith("_Map") || (response.runtimeType).toString().startsWith("LinkedMap")){
    return response;
  }
  else{
    return <dynamic, dynamic>{
      "status":null,
      "headers":<dynamic, dynamic>{},
      "body":decode_body(response),
      "error":null
    };
  }
}

encode_query_params(params) {
  var out = <dynamic>[];
  for(var entry_41983 in (params ?? <dynamic, dynamic>{}).entries){
    var k = entry_41983.key;
    var v = entry_41983.value;
    if(null != v){
      out.add(k + "=" + (v).toString());
    }
  };
  return out.join("&");
}

get_body_data(response) {
  var out = response["body"];
  if((("Map" == (out.runtimeType).toString()) || (out.runtimeType).toString().startsWith("_Map") || (out.runtimeType).toString().startsWith("LinkedMap")) && (null != out["data"])){
    return out["data"];
  }
  else{
    return out;
  }
}