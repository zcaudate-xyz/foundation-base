import 'dart:convert';

frame_kindp(kind) {
  return (kind == "request") || (kind == "response") || (kind == "stream") || (kind == "subscribe") || (kind == "unsubscribe");
}

valid_framep(value) {
  if(!(("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap"))){
    return false;
  }
  var kind = value["kind"];
  var id = value["id"];
  var space = value["space"];
  var meta = value["meta"];
  if(!(() {
    var dart_truthy__51343 = frame_kindp(kind);
    return (null != dart_truthy__51343) && (false != dart_truthy__51343);
  })() || !("String" == (id.runtimeType).toString()) || !("String" == (space.runtimeType).toString()) || ((null != meta) && !(("Map" == (meta.runtimeType).toString()) || (meta.runtimeType).toString().startsWith("_Map") || (meta.runtimeType).toString().startsWith("LinkedMap")))){
    return false;
  }
  if(kind == "request"){
    return ("String" == (value["action"].runtimeType).toString()) && ((null == value["args"]) || ((value["args"].runtimeType).toString().startsWith("List") || (value["args"].runtimeType).toString().startsWith("_GrowableList")));
  }
  else if(kind == "response"){
    return ("String" == (value["reply_to"].runtimeType).toString()) && ("String" == (value["status"].runtimeType).toString());
  }
  else{
    return "String" == (value["signal"].runtimeType).toString();
  }
}

normalize_error(err) {
  if(null == err){
    return null;
  }
  else if("String" == (err.runtimeType).toString()){
    return <dynamic, dynamic>{"message":err};
  }
  else if(("Map" == (err.runtimeType).toString()) || (err.runtimeType).toString().startsWith("_Map") || (err.runtimeType).toString().startsWith("LinkedMap")){
    var out = xt.lang.common_data.obj_clone(err);
    if(!out.containsKey("message") && out.containsKey("error") && ("String" == (out["error"].runtimeType).toString())){
      out["message"] = out["error"];
    }
    if(!out.containsKey("message") && out.containsKey("status") && ("String" == (out["status"].runtimeType).toString())){
      out["message"] = out["status"];
    }
    if(!out.containsKey("message")){
      out["message"] = (err).toString();
    }
    return out;
  }
  else{
    return <dynamic, dynamic>{"message":(err).toString()};
  }
}

normalize_frame(frame) {
  if(!(("Map" == (frame.runtimeType).toString()) || (frame.runtimeType).toString().startsWith("_Map") || (frame.runtimeType).toString().startsWith("LinkedMap"))){
    return frame;
  }
  var out = xt.lang.common_data.obj_clone(frame);
  if(("response" == out["kind"]) && ("error" == out["status"]) && (null != out["error"])){
    out["error"] = normalize_error(out["error"]);
  }
  return out;
}

encode_frame(frame) {
  var out = normalize_frame(frame);
  if(!(() {
    var dart_truthy__51344 = valid_framep(out);
    return (null != dart_truthy__51344) && (false != dart_truthy__51344);
  })()){
    throw "invalid node json frame";
  }
  return jsonEncode(out);
}

decode_frame(input) {
  var out = ("String" == (input.runtimeType).toString()) ? jsonDecode(input) : input;
  if(!(() {
    var dart_truthy__51345 = valid_framep(out);
    return (null != dart_truthy__51345) && (false != dart_truthy__51345);
  })()){
    throw "invalid node json frame";
  }
  return out;
}