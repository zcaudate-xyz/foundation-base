import 'package:xtalk_lang/common-data.dart' as xtd;


clone(value) {
  return xtd.clone_nested(value ?? <dynamic, dynamic>{});
}

create(values, validators) {
  var initial = clone(values);
  return <dynamic, dynamic>{
    "initial":initial,
    "draft":clone(initial),
    "validators":validators ?? <dynamic, dynamic>{},
    "errors":<dynamic, dynamic>{},
    "touched":<dynamic, dynamic>{},
    "dirty":false,
    "valid":true,
    "pending":false
  };
}

validate_value(validators, value, draft) {
  var message = null;
  var arr_52947 = validators ?? <dynamic>[];
  for(var i52948 = 0; i52948 < arr_52947.length; ++i52948){
    var validator = arr_52947[i52948];
    if(null == message){
      message = validator(value,draft);
    }
  };
  return message;
}

validatef(form) {
  var errors = <dynamic, dynamic>{};
  var draft = form["draft"];
  for(var entry_52969 in form["validators"].entries){
    var field = entry_52969.key;
    var validators = entry_52969.value;
    var message = validate_value(validators,draft[field],draft);
    if(null != message){
      errors[field] = message;
    }
  };
  form["errors"] = errors;
  form["valid"] = (0 == List<dynamic>.from(( errors ).keys).length);
  return form["valid"];
}

set_fieldf(form, path, value) {
  var draft = clone(form["draft"]);
  xtd.set_in(draft,path,value);
  form["draft"] = draft;
  xtd.set_in(form["touched"],path,true);
  form["dirty"] = true;
  validatef(form);
  return draft;
}

resetf(form) {
  form["draft"] = clone(form["initial"]);
  form["errors"] = <dynamic, dynamic>{};
  form["touched"] = <dynamic, dynamic>{};
  form["dirty"] = false;
  form["valid"] = true;
  form["pending"] = false;
  return form;
}

pendingf(form, pending) {
  form["pending"] = (true == pending);
  return form;
}