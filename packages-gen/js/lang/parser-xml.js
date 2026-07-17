const xtd = require("@xtalk/lang/common-data.js")

const xts = require("@xtalk/lang/common-string.js")

function parse_xml_params(s){
  let params = {};
  let total = s.length;
  let i = 0;
  while(i < total){
    while(i < total){
      let ch = xts.substring(s,i,i + 1);
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
    let key_start = i;
    while((i < total) && ("=" != xts.substring(s,i,i + 1))){
      i = (i + 1);
    }
    if(i >= total){
      break;
    }
    let key = xts.trim(xts.substring(s,key_start,i));
    i = (i + 1);
    while((i < total) && (" " == xts.substring(s,i,i + 1))){
      i = (i + 1);
    }
    if(i >= total){
      break;
    }
    let qchar = xts.substring(s,i,i + 1);
    let value_start = i + 1;
    i = value_start;
    while((i < total) && (qchar != xts.substring(s,i,i + 1))){
      i = (i + 1);
    }
    params[key] = xts.substring(s,value_start,i);
    i = (i + 1);
  }
  return params;
}

function parse_xml_stack(s){
  let output = [];
  let total = s.length;
  let start = 0;
  while(start < total){
    let ni = start;
    while((ni < total) && ("<" != xts.substring(s,ni,ni + 1))){
      ni = (ni + 1);
    }
    if(ni >= total){
      break;
    }
    let j = ni + 1;
    while((j < total) && (">" != xts.substring(s,j,j + 1))){
      j = (j + 1);
    }
    if(j >= total){
      break;
    }
    let text = xts.trim(xts.substring(s,start,ni));
    let inner = xts.trim(xts.substring(s,ni + 1,j));
    let close = xts.starts_withp(inner,"/");
    let empty = xts.ends_withp(inner,"/");
    if(close){
      inner = xts.trim(xts.substring(inner,1,inner.length));
    }
    if(empty){
      inner = xts.trim(xts.substring(inner,0,inner.length - 1));
    }
    let split = 0;
    let inner_total = inner.length;
    while((split < inner_total) && (" " != xts.substring(inner,split,split + 1))){
      split = (split + 1);
    }
    let tag = xts.substring(inner,0,split);
    let params_str = (split < inner_total) ? xts.trim(xts.substring(inner,split + 1,inner_total)) : "";
    let m = {"tag":tag};
    if(0 < params_str.length){
      m["params"] = parse_xml_params(params_str);
    }
    if(0 < text.length){
      m["text"] = text;
    }
    if(close){
      m["close"] = true;
    }
    if(empty){
      m["empty"] = true;
    }
    output.push(m);
    start = (j + 1);
  }
  return output;
}

function to_node_normalise(node){
  let {children,params,tag} = node;
  let out = {"tag":tag};
  if((null != params) && xtd.obj_not_emptyp(params)){
    out["params"] = params;
  }
  if(xtd.arr_not_emptyp(children)){
    out["children"] = children.map(function (v){
      return (((null != v) && ("object" == (typeof v)) && !Array.isArray(v)) && ("xml" == v["::/__type__"])) ? to_node_normalise(v) : v;
    });
  }
  return out;
}

function to_node(stack){
  let top = {"tag":"<TOP>","children":[]};
  let levels = [top];
  let current = top;
  for(let e of stack){
    let etext = e["text"];
    let etag = e["tag"];
    let eparams = e["params"];
    if(null != etext){
      current["children"].push(etext);
    }
    if(true == e["empty"]){
      let enode = {"tag":etag};
      if((null != eparams) && xtd.obj_not_emptyp(eparams)){
        enode["params"] = eparams;
      }
      current["children"].push(enode);
    }
    else if(true != e["close"]){
      let ncurrent = {
        "::/__type__":"xml",
        "parent":current,
        "tag":etag,
        "params":eparams,
        "children":[]
      };
      current["children"].push(ncurrent);
      current = ncurrent;
    }
    else{
      current = current["parent"];
    }
  };
  return to_node_normalise((top["children"])[0]);
}

function parse_xml(s){
  return to_node(parse_xml_stack(s));
}

function to_tree(node){
  let {children,params,tag} = node;
  let arr = [tag];
  if((null != params) && xtd.obj_not_emptyp(params)){
    arr.push(params);
  }
  if(xtd.arr_not_emptyp(children)){
    xtd.arr_assign(arr,children.map(function (e){
      return ((null != e) && ("object" == (typeof e)) && !Array.isArray(e)) ? to_tree(e) : e;
    }));
  }
  return arr;
}

function from_tree(tree){
  let count = tree.length;
  let elem_fn = function (e){
    return Array.isArray(e) ? from_tree(e) : e;
  };
  if(count == 1){
    return {"tag":tree[0]};
  }
  else if((null != tree[1]) && ("object" == (typeof tree[1])) && !Array.isArray(tree[1])){
    return {
      "tag":tree[0],
      "params":tree[1],
      "children":tree.slice(2,tree.length).map(elem_fn)
    };
  }
  else{
    return {
      "tag":tree[0],
      "params":{},
      "children":tree.slice(1,tree.length).map(elem_fn)
    };
  }
}

function to_brief(node){
  let {children,tag} = node;
  let sub_fn = function (e){
    return ((null != e) && ("object" == (typeof e)) && !Array.isArray(e)) ? to_brief(e) : e;
  };
  if(xtd.arr_emptyp(children)){
    return {[tag]:true};
  }
  else if(2 < children.length){
    let has_string = children.some(function (value){
      return "string" == (typeof value);
    });
    let unique = {};
    for(let e of children){
      if((null != e) && ("object" == (typeof e)) && !Array.isArray(e)){
        unique[e["tag"]] = true;
      }
    };
    if(has_string || (Object.keys(unique).length != children.length)){
      return {[tag]:children.map(sub_fn)};
    }
    else{
      let out = {};
      for(let e of children){
        Object.assign(out,to_brief(e));
      };
      return {[tag]:out};
    }
  }
  else{
    return {[tag]:sub_fn(children[0])};
  }
}

function to_string_value(v){
  if("boolean" == (typeof v)){
    return v ? "true" : "false";
  }
  else{
    return String(v);
  }
}

function to_string_params(params){
  if((null == params) || xtd.obj_emptyp(params)){
    return "";
  }
  else{
    let s = "";
    for(let [k,v] of Object.entries(params)){
      s = (s + " " + k + "=" + to_string_value(v));
    };
    return s;
  }
}

function to_string(node){
  let {children,params,tag} = node;
  let body = "";
  if(xtd.arr_not_emptyp(children)){
    for(let e of children){
      body = (body + (((null != e) && ("object" == (typeof e)) && !Array.isArray(e)) ? to_string(e) : to_string_value(e)));
    };
  }
  return "<" + tag + to_string_params(params) + ">" + body + "</" + tag + ">";
}

module.exports = {
  ["parse_xml_params"]:parse_xml_params,
  ["parse_xml_stack"]:parse_xml_stack,
  ["to_node_normalise"]:to_node_normalise,
  ["to_node"]:to_node,
  ["parse_xml"]:parse_xml,
  ["to_tree"]:to_tree,
  ["from_tree"]:from_tree,
  ["to_brief"]:to_brief,
  ["to_string_value"]:to_string_value,
  ["to_string_params"]:to_string_params,
  ["to_string"]:to_string
}