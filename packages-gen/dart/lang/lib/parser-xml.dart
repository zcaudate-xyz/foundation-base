import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_lang/common-string.dart' as xts;

parse_xml_params(s) {
  var params = <dynamic, dynamic>{};
  var total = s.length;
  var i = 0;
  while(i < total){
    while(i < total){
      var ch = xts.substring(s,i,i + 1);
      if((" " == ch) || ("," == ch)){
        i = (i + 1);
      }
      else{
        break;
      }
    }
    if(i >= total){
      break;
    }
    var key_start = i;
    while((i < total) && ("=" != xts.substring(s,i,i + 1))){
      i = (i + 1);
    }
    if(i >= total){
      break;
    }
    var key = xts.trim(xts.substring(s,key_start,i));
    i = (i + 1);
    while((i < total) && (" " == xts.substring(s,i,i + 1))){
      i = (i + 1);
    }
    if(i >= total){
      break;
    }
    var qchar = xts.substring(s,i,i + 1);
    var value_start = i + 1;
    i = value_start;
    while((i < total) && (qchar != xts.substring(s,i,i + 1))){
      i = (i + 1);
    }
    params[key] = xts.substring(s,value_start,i);
    i = (i + 1);
  }
  return params;
}

parse_xml_stack(s) {
  var output = <dynamic>[];
  var total = s.length;
  var start = 0;
  while(start < total){
    var ni = start;
    while((ni < total) && ("<" != xts.substring(s,ni,ni + 1))){
      ni = (ni + 1);
    }
    if(ni >= total){
      break;
    }
    var j = ni + 1;
    while((j < total) && (">" != xts.substring(s,j,j + 1))){
      j = (j + 1);
    }
    if(j >= total){
      break;
    }
    var text = xts.trim(xts.substring(s,start,ni));
    var inner = xts.trim(xts.substring(s,ni + 1,j));
    var close = xts.starts_withp(inner,"/");
    var empty = xts.ends_withp(inner,"/");
    if((null != close) && (false != close)){
      inner = xts.trim(xts.substring(inner,1,inner.length));
    }
    if((null != empty) && (false != empty)){
      inner = xts.trim(xts.substring(inner,0,inner.length - 1));
    }
    var split = 0;
    var inner_total = inner.length;
    while((split < inner_total) && (" " != xts.substring(inner,split,split + 1))){
      split = (split + 1);
    }
    var tag = xts.substring(inner,0,split);
    var params_str = (split < inner_total) ? xts.trim(xts.substring(inner,split + 1,inner_total)) : "";
    var m = <dynamic, dynamic>{"tag":tag};
    if(0 < params_str.length){
      m["params"] = parse_xml_params(params_str);
    }
    if(0 < text.length){
      m["text"] = text;
    }
    if((null != close) && (false != close)){
      m["close"] = true;
    }
    if((null != empty) && (false != empty)){
      m["empty"] = true;
    }
    output.add(m);
    start = (j + 1);
  }
  return output;
}

to_node_normalise(node) {
  var children = node["children"];
  var params = node["params"];
  var tag = node["tag"];
  var out = <dynamic, dynamic>{"tag":tag};
  if((null != params) && (() {
    var dart_truthy__39910 = xtd.obj_not_emptyp(params);
    return (null != dart_truthy__39910) && (false != dart_truthy__39910);
  })()){
    out["params"] = params;
  }
  if((() {
    var dart_truthy__39911 = xtd.arr_not_emptyp(children);
    return (null != dart_truthy__39911) && (false != dart_truthy__39911);
  })()){
    out["children"] = xtd.arr_map(children,(v) {
      return ((("Map" == (v.runtimeType).toString()) || (v.runtimeType).toString().startsWith("_Map") || (v.runtimeType).toString().startsWith("LinkedMap")) && ("xml" == v["::/__type__"])) ? to_node_normalise(v) : v;
    });
  }
  return out;
}

to_node(stack) {
  var top = <dynamic, dynamic>{"tag":"<TOP>","children":<dynamic>[]};
  var levels = <dynamic>[top];
  var current = top;
  var arr_39917 = stack;
  for(var i39918 = 0; i39918 < arr_39917.length; ++i39918){
    var e = arr_39917[i39918];
    var etext = e["text"];
    var etag = e["tag"];
    var eparams = e["params"];
    if(null != etext){
      current["children"].add(etext);
    }
    if(true == e["empty"]){
      var enode = <dynamic, dynamic>{"tag":etag};
      if((null != eparams) && (() {
        var dart_truthy__39909 = xtd.obj_not_emptyp(eparams);
        return (null != dart_truthy__39909) && (false != dart_truthy__39909);
      })()){
        enode["params"] = eparams;
      }
      current["children"].add(enode);
    }
    else if(true != e["close"]){
      var ncurrent = <dynamic, dynamic>{
        "::/__type__":"xml",
        "parent":current,
        "tag":etag,
        "params":eparams,
        "children":<dynamic>[]
      };
      current["children"].add(ncurrent);
      current = ncurrent;
    }
    else{
      current = current["parent"];
    }
  };
  return to_node_normalise((top["children"])[0]);
}

parse_xml(s) {
  return to_node(parse_xml_stack(s));
}

to_tree(node) {
  var children = node["children"];
  var params = node["params"];
  var tag = node["tag"];
  var arr = <dynamic>[tag];
  if((null != params) && (() {
    var dart_truthy__39913 = xtd.obj_not_emptyp(params);
    return (null != dart_truthy__39913) && (false != dart_truthy__39913);
  })()){
    arr.add(params);
  }
  if((() {
    var dart_truthy__39914 = xtd.arr_not_emptyp(children);
    return (null != dart_truthy__39914) && (false != dart_truthy__39914);
  })()){
    xtd.arr_assign(arr,xtd.arr_map(children,(e) {
      return (("Map" == (e.runtimeType).toString()) || (e.runtimeType).toString().startsWith("_Map") || (e.runtimeType).toString().startsWith("LinkedMap")) ? to_tree(e) : e;
    }));
  }
  return arr;
}

from_tree(tree) {
  var count = tree.length;
  var elem_fn = (e) {
    return ((e.runtimeType).toString().startsWith("List") || (e.runtimeType).toString().startsWith("_GrowableList")) ? from_tree(e) : e;
  };
  if(count == 1){
    return <dynamic, dynamic>{"tag":tree[0]};
  }
  else if(("Map" == (tree[1].runtimeType).toString()) || (tree[1].runtimeType).toString().startsWith("_Map") || (tree[1].runtimeType).toString().startsWith("LinkedMap")){
    return <dynamic, dynamic>{
      "tag":tree[0],
      "params":tree[1],
      "children":xtd.arr_map(tree.sublist(2 - 0,tree.length),elem_fn)
    };
  }
  else{
    return <dynamic, dynamic>{
      "tag":tree[0],
      "params":<dynamic, dynamic>{},
      "children":xtd.arr_map(tree.sublist(1 - 0,tree.length),elem_fn)
    };
  }
}

to_brief(node) {
  var children = node["children"];
  var tag = node["tag"];
  var sub_fn = (e) {
    return (("Map" == (e.runtimeType).toString()) || (e.runtimeType).toString().startsWith("_Map") || (e.runtimeType).toString().startsWith("LinkedMap")) ? to_brief(e) : e;
  };
  if((() {
    var dart_truthy__39916 = xtd.arr_emptyp(children);
    return (null != dart_truthy__39916) && (false != dart_truthy__39916);
  })()){
    return <dynamic, dynamic>{tag:true};
  }
  else if(2 < children.length){
    var has_string = xtd.arr_some(children,(value) {
      return "String" == (value.runtimeType).toString();
    });
    var unique = <dynamic, dynamic>{};
    var arr_39939 = children;
    for(var i39940 = 0; i39940 < arr_39939.length; ++i39940){
      var e = arr_39939[i39940];
      if(("Map" == (e.runtimeType).toString()) || (e.runtimeType).toString().startsWith("_Map") || (e.runtimeType).toString().startsWith("LinkedMap")){
        unique[e["tag"]] = true;
      }
    };
    if(((null != has_string) && (false != has_string)) || (List<dynamic>.from(( unique ).keys).length != children.length)){
      return <dynamic, dynamic>{tag:xtd.arr_map(children,sub_fn)};
    }
    else{
      var out = <dynamic, dynamic>{};
      var arr_39965 = children;
      for(var i39966 = 0; i39966 < arr_39965.length; ++i39966){
        var e = arr_39965[i39966];
        xtd.obj_assign(out,to_brief(e));
      };
      return <dynamic, dynamic>{tag:out};
    }
  }
  else{
    return <dynamic, dynamic>{
      tag:Function.apply((sub_fn as Function),<dynamic>[children[0]])
    };
  }
}

to_string_value(v) {
  if("bool" == (v.runtimeType).toString()){
    return ((null != v) && (false != v)) ? "true" : "false";
  }
  else{
    return (v).toString();
  }
}

to_string_params(params) {
  if((null == params) || (() {
    var dart_truthy__39915 = xtd.obj_emptyp(params);
    return (null != dart_truthy__39915) && (false != dart_truthy__39915);
  })()){
    return "";
  }
  else{
    var s = "";
    for(var entry_39987 in params.entries){
      var k = entry_39987.key;
      var v = entry_39987.value;
      s = (s + " " + k + "=" + to_string_value(v));
    };
    return s;
  }
}

to_string(node) {
  var children = node["children"];
  var params = node["params"];
  var tag = node["tag"];
  var body = "";
  if((() {
    var dart_truthy__39912 = xtd.arr_not_emptyp(children);
    return (null != dart_truthy__39912) && (false != dart_truthy__39912);
  })()){
    var arr_39988 = children;
    for(var i39989 = 0; i39989 < arr_39988.length; ++i39989){
      var e = arr_39988[i39989];
      body = (body + ((("Map" == (e.runtimeType).toString()) || (e.runtimeType).toString().startsWith("_Map") || (e.runtimeType).toString().startsWith("LinkedMap")) ? to_string(e) : to_string_value(e)));
    };
  }
  return "<" + tag + to_string_params(params) + ">" + body + "</" + tag + ">";
}