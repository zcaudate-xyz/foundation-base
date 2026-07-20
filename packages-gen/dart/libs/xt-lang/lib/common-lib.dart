type_native(obj) {
  if(obj == null){
    return null;
  }
  var rtype49017 = (obj.runtimeType).toString();
  var sval49018 = (obj).toString();
  if("String" == rtype49017){
    return "string";
  }
  if(("int" == rtype49017) || ("double" == rtype49017) || ("num" == rtype49017)){
    return "number";
  }
  if("bool" == rtype49017){
    return "boolean";
  }
  if(rtype49017.contains("Function") || rtype49017.contains("=>") || sval49018.startsWith("Closure")){
    return "function";
  }
  if(("List" == rtype49017) || rtype49017.contains("List")){
    return "array";
  }
  if(("Map" == rtype49017) || rtype49017.contains("Map")){
    return "object";
  }
  return rtype49017.toLowerCase();
}

type_class(x) {
  var ntype = ((value) {
    if(value == null){
      return null;
    }
    var rtype49055 = (value.runtimeType).toString();
    var sval49056 = (value).toString();
    if("String" == rtype49055){
      return "string";
    }
    if(("int" == rtype49055) || ("double" == rtype49055) || ("num" == rtype49055)){
      return "number";
    }
    if("bool" == rtype49055){
      return "boolean";
    }
    if(rtype49055.contains("Function") || rtype49055.contains("=>") || sval49056.startsWith("Closure")){
      return "function";
    }
    if(("List" == rtype49055) || rtype49055.contains("List")){
      return "array";
    }
    if(("Map" == rtype49055) || rtype49055.contains("Map")){
      return "object";
    }
    return rtype49055.toLowerCase();
  })(x);
  if(("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")){
    return (null == x["::"]) ? "object" : x["::"];
  }
  else{
    return ntype;
  }
}

to_string(x) {
  return (x).toString();
}

to_number(x) {
  return num.parse(x);
}

nilp(x) {
  return null == x;
}

not_nilp(x) {
  return null != x;
}

is_booleanp(x) {
  return "bool" == (x.runtimeType).toString();
}

is_integerp(x) {
  return "int" == (x.runtimeType).toString();
}

is_numberp(x) {
  return ("int" == (x.runtimeType).toString()) || ("double" == (x.runtimeType).toString()) || ("num" == (x.runtimeType).toString());
}

is_stringp(x) {
  return "String" == (x.runtimeType).toString();
}

is_functionp(x) {
  return (x.runtimeType).toString().contains("Function") || (x.runtimeType).toString().contains("=>") || (x).toString().startsWith("Closure");
}

is_arrayp(x) {
  return (x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList");
}

is_objectp(x) {
  return ("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap");
}

noop() {
  return null;
}

identity(x) {
  return x;
}

T(x) {
  return true;
}

F(x) {
  return false;
}

add(a, b) {
  return a + b;
}

sub(a, b) {
  return a - b;
}

mul(a, b) {
  return a * b;
}

div(a, b) {
  return a / b;
}

gt(a, b) {
  return (("String" == (a.runtimeType).toString()) || ("String" == (b.runtimeType).toString())) ? ((a).toString().compareTo((b).toString()) > 0) : (a > b);
}

lt(a, b) {
  return (("String" == (a.runtimeType).toString()) || ("String" == (b.runtimeType).toString())) ? ((a).toString().compareTo((b).toString()) < 0) : (a < b);
}

gte(a, b) {
  return (("String" == (a.runtimeType).toString()) || ("String" == (b.runtimeType).toString())) ? ((a).toString().compareTo((b).toString()) >= 0) : (a >= b);
}

lte(a, b) {
  return (("String" == (a.runtimeType).toString()) || ("String" == (b.runtimeType).toString())) ? ((a).toString().compareTo((b).toString()) <= 0) : (a <= b);
}

eq(a, b) {
  return a == b;
}

neq(a, b) {
  return a != b;
}

neg(x) {
  return -x;
}

inc(x) {
  return x + 1;
}

dec(x) {
  return x - 1;
}

zerop(x) {
  return x == 0;
}

posp(x) {
  return x > 0;
}

negp(x) {
  return x < 0;
}

evenp(x) {
  return 0 == (x % 2);
}

oddp(x) {
  return !(0 == (x % 2));
}

wrap_callback(callbacks, key) {
  if(null == callbacks){
    callbacks = <dynamic, dynamic>{};
  }
  var result_fn = (result) {
    var f = callbacks[key];
    if(null != f){
      return Function.apply(f,<dynamic>[result]);
    }
    else{
      return result;
    }
  };
  return result_fn;
}

return_encode(out, id, key) {
  if(out == null){
    return json.encode(
      <dynamic, dynamic>{"id":id,"key":key,"type":"data","return":"nil","value":null}
    );
  }
  var outtype49093 = (out.runtimeType).toString();
  var outstr49094 = (out).toString();
  var compact49095 = (() {
     final omitNilKeys = <String>{"db/remove", "select_method", "select_control", "return_method", "return_query", "return_count", "return_id", "return_bulk", "return_omit", "data_only", "model_id", "view_id"};
     dynamic compact(dynamic value) {
       if (value is Function) {
         return null;
       }
       if (value is Map) {
         final out = <dynamic, dynamic>{};
         value.forEach((key, entry) {
           if (!(entry is Function)) {
             final next = compact(entry);
             if (next != null || !(key is String && omitNilKeys.contains(key))) {
               out[key] = next;
             }
           }
         });
         return out;
       }
       if (value is List) {
         return List<dynamic>.from(value.where((entry) => !(entry is Function))
                                          .map((entry) => compact(entry)));
       }
       return value;
     }
     return compact( out );
   })();
  if("String" == outtype49093){
    return json.encode(
      <dynamic, dynamic>{"id":id,"key":key,"type":"data","return":"string","value":out}
    );
  }
  if(("int" == outtype49093) || ("double" == outtype49093) || ("num" == outtype49093)){
    return json.encode(
      <dynamic, dynamic>{"id":id,"key":key,"type":"data","return":"number","value":out}
    );
  }
  if("bool" == outtype49093){
    return json.encode(
      <dynamic, dynamic>{"id":id,"key":key,"type":"data","return":"boolean","value":out}
    );
  }
  if(("List" == outtype49093) || outtype49093.contains("List")){
    return json.encode(<dynamic, dynamic>{
      "id":id,
      "key":key,
      "type":"data",
      "return":"array",
      "value":compact49095
    });
  }
  if(("Map" == outtype49093) || outtype49093.contains("Map")){
    return json.encode(<dynamic, dynamic>{
      "id":id,
      "key":key,
      "type":"data",
      "return":"object",
      "value":compact49095
    });
  }
  if(outtype49093.contains("Function") || outtype49093.contains("=>") || outstr49094.startsWith("Closure")){
    return json.encode(<dynamic, dynamic>{
      "id":id,
      "key":key,
      "type":"raw",
      "return":"function",
      "value":outstr49094
    });
  }
  return json.encode(<dynamic, dynamic>{
    "id":id,
    "key":key,
    "type":"raw",
    "return":outtype49093.toLowerCase(),
    "value":outstr49094
  });
}

return_wrap(f) {
  try{
    var out = f();
    try{
      return Function.apply(return_encode,<dynamic>[out]);
    }
    catch(_){
      return Function.apply(return_encode,<dynamic>[out,null,null]);
    }
  }
  catch(e){
    return json.encode(<dynamic, dynamic>{
      "type":"error",
      "value":<dynamic, dynamic>{"message":e.toString()}
    });
  }
}

return_eval(s) {
  throw "eval not supported in Dart";
}