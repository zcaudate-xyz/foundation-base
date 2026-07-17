function type_native(obj){
  if(obj == null){
    return null;
  }
  let t = typeof obj;
  if(t == "object"){
    if(Array.isArray(obj)){
      return "array";
    }
    else{
      let tn = obj["constructor"]["name"];
      if(tn == "Object"){
        return "object";
      }
      else{
        return tn;
      }
    }
  }
  else{
    return t;
  }
}

function type_class(x){
  let ntype = null;
  if(x == null){
    return null;
  }
  let t = typeof x;
  if(t == "object"){
    if(Array.isArray(x)){
      ntype = "array";
    }
    else{
      let tn = x["constructor"]["name"];
      if(tn == "Object"){
        ntype = "object";
      }
      else{
        ntype = tn;
      }
    }
  }
  else{
    ntype = t;
  }
  if((null != x) && ("object" == (typeof x)) && !Array.isArray(x)){
    return (null == x["::"]) ? "object" : x["::"];
  }
  else{
    return ntype;
  }
}

function to_string(x){
  return String(x);
}

function to_number(x){
  return Number(x);
}

function nilp(x){
  return null == x;
}

function not_nilp(x){
  return null != x;
}

function is_booleanp(x){
  return "boolean" == (typeof x);
}

function is_integerp(x){
  return Number.isInteger(x);
}

function is_numberp(x){
  return "number" == (typeof x);
}

function is_stringp(x){
  return "string" == (typeof x);
}

function is_functionp(x){
  return "function" == (typeof x);
}

function is_arrayp(x){
  return Array.isArray(x);
}

function is_objectp(x){
  return (null != x) && ("object" == (typeof x)) && !Array.isArray(x);
}

function noop(){
  return null;
}

function identity(x){
  return x;
}

function T(x){
  return true;
}

function F(x){
  return false;
}

function add(a,b){
  return a + b;
}

function sub(a,b){
  return a - b;
}

function mul(a,b){
  return a * b;
}

function div(a,b){
  return a / b;
}

function gt(a,b){
  return a > b;
}

function lt(a,b){
  return a < b;
}

function gte(a,b){
  return a >= b;
}

function lte(a,b){
  return a <= b;
}

function eq(a,b){
  return a == b;
}

function neq(a,b){
  return a != b;
}

function neg(x){
  return -x;
}

function inc(x){
  return x + 1;
}

function dec(x){
  return x - 1;
}

function zerop(x){
  return x == 0;
}

function posp(x){
  return x > 0;
}

function negp(x){
  return x < 0;
}

function evenp(x){
  return 0 == (x % 2);
}

function oddp(x){
  return !(0 == (x % 2));
}

function wrap_callback(callbacks,key){
  if(null == callbacks){
    callbacks = {};
  }
  let result_fn = function (result){
    let f = callbacks[key];
    if(null != f){
      return f.apply(null,[result]);
    }
    else{
      return result;
    }
  };
  return result_fn;
}

function return_encode(out,id,key){
  let type_fn = function (obj){
    if(obj == null){
      return null;
    }
    let t = typeof obj;
    if(t == "object"){
      if(Array.isArray(obj)){
        return "array";
      }
      else{
        let tn = obj["constructor"]["name"];
        if(tn == "Object"){
          return "object";
        }
        else{
          return tn;
        }
      }
    }
    else{
      return t;
    }
  };
  let ts = type_fn(out);
  if("function" == ts){
    return JSON.stringify({
      "id":id,
      "key":key,
      "type":"raw",
      "return":"function",
      "value":out.toString()
    });
  }
  else if("object" != ts){
    return JSON.stringify({"id":id,"key":key,"type":"data","return":ts,"value":out});
  }
  else if(null == out){
    return JSON.stringify(
      {"id":id,"key":key,"type":"data","return":"nil","value":out}
    );
  }
  else{
    try{
      return JSON.stringify({"id":id,"key":key,"type":"data","return":ts,"value":out});
    }
    catch(e){
      return JSON.stringify({
        "id":id,
        "key":key,
        "type":"raw",
        "return":ts,
        "value":out.toString()
      });
    }
  }
}

function return_wrap(f){
  try{
    let out = f();
    return return_encode(out);
  }
  catch(e){
    let err = ("string" == (typeof e)) ? e : {"message":e["message"],"stack":e["stack"]};
    return JSON.stringify({"type":"error","value":err})
  }
}

function return_eval(s){
  return return_wrap(function (){
    return eval(s);
  });
}

module.exports = {
  ["type_native"]:type_native,
  ["type_class"]:type_class,
  ["to_string"]:to_string,
  ["to_number"]:to_number,
  ["nilp"]:nilp,
  ["not_nilp"]:not_nilp,
  ["is_booleanp"]:is_booleanp,
  ["is_integerp"]:is_integerp,
  ["is_numberp"]:is_numberp,
  ["is_stringp"]:is_stringp,
  ["is_functionp"]:is_functionp,
  ["is_arrayp"]:is_arrayp,
  ["is_objectp"]:is_objectp,
  ["noop"]:noop,
  ["identity"]:identity,
  ["T"]:T,
  ["F"]:F,
  ["add"]:add,
  ["sub"]:sub,
  ["mul"]:mul,
  ["div"]:div,
  ["gt"]:gt,
  ["lt"]:lt,
  ["gte"]:gte,
  ["lte"]:lte,
  ["eq"]:eq,
  ["neq"]:neq,
  ["neg"]:neg,
  ["inc"]:inc,
  ["dec"]:dec,
  ["zerop"]:zerop,
  ["posp"]:posp,
  ["negp"]:negp,
  ["evenp"]:evenp,
  ["oddp"]:oddp,
  ["wrap_callback"]:wrap_callback,
  ["return_encode"]:return_encode,
  ["return_wrap"]:return_wrap,
  ["return_eval"]:return_eval
}