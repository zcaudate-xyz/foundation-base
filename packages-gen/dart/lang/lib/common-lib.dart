type_native(obj) {
  if(obj == null){
    return null;
  }
  var rtype41101 = (obj.runtimeType).toString();
  var sval41102 = (obj).toString();
  if("String" == rtype41101){
    return "string";
  }
  if(("int" == rtype41101) || ("double" == rtype41101) || ("num" == rtype41101)){
    return "number";
  }
  if("bool" == rtype41101){
    return "boolean";
  }
  if(rtype41101.contains("Function") || rtype41101.contains("=>") || sval41102.startsWith("Closure")){
    return "function";
  }
  if(("List" == rtype41101) || rtype41101.contains("List")){
    return "array";
  }
  if(("Map" == rtype41101) || rtype41101.contains("Map")){
    return "object";
  }
  return rtype41101.toLowerCase();
}

type_class(x) {
  var ntype = ((value) {
    if(value == null){
      return null;
    }
    var rtype41139 = (value.runtimeType).toString();
    var sval41140 = (value).toString();
    if("String" == rtype41139){
      return "string";
    }
    if(("int" == rtype41139) || ("double" == rtype41139) || ("num" == rtype41139)){
      return "number";
    }
    if("bool" == rtype41139){
      return "boolean";
    }
    if(rtype41139.contains("Function") || rtype41139.contains("=>") || sval41140.startsWith("Closure")){
      return "function";
    }
    if(("List" == rtype41139) || rtype41139.contains("List")){
      return "array";
    }
    if(("Map" == rtype41139) || rtype41139.contains("Map")){
      return "object";
    }
    return rtype41139.toLowerCase();
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
  var outtype41177 = (out.runtimeType).toString();
  var outstr41178 = (out).toString();
  var compact41179 = (() {
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
  if("String" == outtype41177){
    return json.encode(
      <dynamic, dynamic>{"id":id,"key":key,"type":"data","return":"string","value":out}
    );
  }
  if(("int" == outtype41177) || ("double" == outtype41177) || ("num" == outtype41177)){
    return json.encode(
      <dynamic, dynamic>{"id":id,"key":key,"type":"data","return":"number","value":out}
    );
  }
  if("bool" == outtype41177){
    return json.encode(
      <dynamic, dynamic>{"id":id,"key":key,"type":"data","return":"boolean","value":out}
    );
  }
  if(("List" == outtype41177) || outtype41177.contains("List")){
    return json.encode(<dynamic, dynamic>{
      "id":id,
      "key":key,
      "type":"data",
      "return":"array",
      "value":compact41179
    });
  }
  if(("Map" == outtype41177) || outtype41177.contains("Map")){
    return json.encode(<dynamic, dynamic>{
      "id":id,
      "key":key,
      "type":"data",
      "return":"object",
      "value":compact41179
    });
  }
  if(outtype41177.contains("Function") || outtype41177.contains("=>") || outstr41178.startsWith("Closure")){
    return json.encode(<dynamic, dynamic>{
      "id":id,
      "key":key,
      "type":"raw",
      "return":"function",
      "value":outstr41178
    });
  }
  return json.encode(<dynamic, dynamic>{
    "id":id,
    "key":key,
    "type":"raw",
    "return":outtype41177.toLowerCase(),
    "value":outstr41178
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