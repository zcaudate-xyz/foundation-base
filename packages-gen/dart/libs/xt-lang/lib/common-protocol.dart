import 'package:xtalk_lang/common-data.dart' as xtd;






raw_method(on, typename, method) {
  return xtd.get_in(
    (__globals__["xt.lang.common_protocol/PROTOCOLS"] ??= <dynamic, dynamic>{}),
    <dynamic>[on,"impls",typename,method]
  );
}

protocol_exists(typename) {
  return (__globals__["xt.lang.common_protocol/IMPLEMENTATIONS"] ??= <dynamic, dynamic>{}).containsKey(typename);
}

protocol_implements(obj, protocol) {
  var type = obj["::"];
  if(null == type){
    return false;
  }
  var entry = (__globals__["xt.lang.common_protocol/PROTOCOLS"] ??= <dynamic, dynamic>{})[protocol];
  if(null == entry){
    return false;
  }
  var impls = entry["impls"];
  return impls.containsKey(type);
}

protocol_method(obj, on, method) {
  var type = obj["::"];
  var override_map = obj["::/override"];
  if(!(null == override_map)){
    var override_fn = override_map[method];
    if(!(null == override_fn)){
      return override_fn;
    }
  }
  var local_impls = obj["::/protocol-impls"];
  var local_impl_map = ((null != local_impls) && (false != local_impls)) ? local_impls[on] : local_impls;
  var protocol = (__globals__["xt.lang.common_protocol/PROTOCOLS"] ??= <dynamic, dynamic>{})[on];
  if((null == protocol) && (null == local_impl_map)){
    throw "Missing protocol entry " + on;
  }
  var protocol_impls = ((null != protocol) && (false != protocol)) ? protocol["impls"] : protocol;
  var protocol_impl_map = (((null != protocol_impls) && (false != protocol_impls)) ? protocol_impls[type] : protocol_impls) ?? local_impl_map;
  if(null == protocol_impl_map){
    throw "Missing protocol implementation " + on + " for " + type;
  }
  var method_fn = protocol_impl_map[method];
  if(null == method_fn){
    throw "Missing protocol method " + on + "/" + method + " for " + type;
  }
  return method_fn;
}

protocol_has_method(obj, protocol, method) {
  try{
    protocol_method(obj,protocol,method);
    return true;
  }
  catch(err){
    return false;
  }
}

register_protocol_impl(protocolname, typename, impl_map) {
  var protocol = (__globals__["xt.lang.common_protocol/PROTOCOLS"] ??= <dynamic, dynamic>{})[protocolname];
  if(null == protocol){
    throw "Missing protocol " + protocolname;
  }
  var impls = protocol["impls"];
  impls[typename] = impl_map;
  return impl_map;
}

register_protocol(protocol) {
  (__globals__["xt.lang.common_protocol/PROTOCOLS"] ??= <dynamic, dynamic>{})[protocol["on"]] = protocol;
  return protocol;
}

create_protocol_fn(on, sig_map) {
  var protocol = <dynamic, dynamic>{
    "::":"type/protocol",
    "on":on,
    "sigs":sig_map,
    "impls":<dynamic, dynamic>{}
  };
  return register_protocol(protocol);
}