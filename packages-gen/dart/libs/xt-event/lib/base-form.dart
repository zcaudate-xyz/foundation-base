import 'package:xtalk_event/base-listener.dart' as event_common;
import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_event/util-validate.dart' as validate;
import 'dart:async';




make_form(initial, validators) {
  var result = validate.create_result(validators);
  return event_common.make_container(
    initial,
    "event.form",
    <dynamic, dynamic>{"result":result,"validators":validators}
  );
}

check_event(event, fields) {
  var arr_51011 = fields;
  for(var i51012 = 0; i51012 < arr_51011.length; ++i51012){
    var field = arr_51011[i51012];
    var arr_51033 = event["fields"];
    for(var i51034 = 0; i51034 < arr_51033.length; ++i51034){
      var evfield = arr_51033[i51034];
      if(evfield == field){
        return true;
      }
    };
  };
  return false;
}

add_listener(form, listener_id, fields, callback, meta) {
  fields = event_common.arrayify_path(fields);
  return event_common.add_listener(form,listener_id,"form",callback,xtd.obj_assign(<dynamic, dynamic>{"form/fields":fields},meta),(event) {
    return check_event(event,fields);
  });
}

var remove_listener = event_common.remove_listener;

var list_listeners = event_common.list_listeners;

trigger_all(form, event_type) {
  var validators = form["validators"];
  var fields = List<dynamic>.from(( validators ).keys);
  return event_common.trigger_listeners(form,<dynamic, dynamic>{"type":event_type,"fields":fields});
}

trigger_field(form, fields, event_type) {
  return event_common.trigger_listeners(form,<dynamic, dynamic>{
    "type":event_type,
    "fields":event_common.arrayify_path(fields)
  });
}

set_field(form, field, value) {
  var data = form["data"];
  data[field] = value;
  return trigger_field(form,field,"form.data");
}

get_field(form, field) {
  var data = form["data"];
  return data[field];
}

toggle_field(form, field) {
  return set_field(form,field,!get_field(form,field));
}

field_fn(form, field) {
  return (value) {
    return set_field(form,field,value);
  };
}

get_result(form) {
  return form["result"];
}

get_field_result(form, field) {
  var result = form["result"];
  var fields = result["fields"];
  return fields[field];
}

get_data(form) {
  return form["data"];
}

set_data(form, m) {
  var data = form["data"];
  xtd.obj_assign(data,m);
  var fields = List<dynamic>.from(( m ).keys);
  return trigger_field(form,fields,"form.data");
}

reset_all_data(form) {
  var initial = form["initial"];
  var data = initial();
  form["data"] = data;
  return trigger_all(form,"form.data");
}

reset_field_data(form, field) {
  var data = form["data"];
  var initial = form["initial"];
  var value = (initial())[field];
  data[field] = value;
  return trigger_field(form,field,"form.data");
}

validate_all(form, hook_fn, complete_fn) {
  var data = form["data"];
  var result = form["result"];
  var validators = form["validators"];
  return ((Future.sync(() => validate.validate_all(data,validators,result,hook_fn,null))) as Future<dynamic>).then((value) async { return await Function.apply((res) {
    trigger_all(form,"form.validation");
    if((null != complete_fn) && (false != complete_fn)){
      Function.apply(
        (complete_fn as Function),
        <dynamic>["ok" == res["status"],res]
      );
    }
    return res;
  },<dynamic>[value]); });
}

validate_field(form, field, hook_fn, complete_fn) {
  var data = form["data"];
  var result = form["result"];
  var validators = form["validators"];
  return ((Future.sync(() => validate.validate_field(data,field,validators,result,hook_fn,null))) as Future<dynamic>).then((value) async { return await Function.apply((res) {
    trigger_field(form,field,"form.validation");
    if((null != complete_fn) && (false != complete_fn)){
      Function.apply((complete_fn as Function),<dynamic>[
        "ok" == xtd.get_in(res["fields"],<dynamic>[field,"status"]),
        res
      ]);
    }
    return res;
  },<dynamic>[value]); });
}

reset_field_validator(form, field) {
  var result = form["result"];
  result[field] = <dynamic, dynamic>{"status":"pending"};
  trigger_field(form,field,"form.validation");
  return result;
}

reset_all_validators(form) {
  var result = form["result"];
  var validators = form["validators"];
  form["result"] = validate.create_result(validators);
  trigger_all(form,"form.validation");
  return result;
}

reset_all(form) {
  reset_all_data(form);
  return reset_all_validators(form);
}

check_field_passed(form, field) {
  var result = form["result"];
  var fields = result["fields"];
  return "ok" == xtd.get_in(fields,<dynamic>[field,"status"]);
}

check_field_errored(form, field) {
  var result = form["result"];
  var fields = result["fields"];
  return "errored" == xtd.get_in(fields,<dynamic>[field,"status"]);
}

check_all_passed(form) {
  var result = form["result"];
  var fields = result["fields"];
  for(var v in fields.values){
    if("ok" != v["status"]){
      return false;
    }
  };
  return true;
}

check_any_errored(form) {
  var result = form["result"];
  var fields = result["fields"];
  for(var v in fields.values){
    if("errored" == v["status"]){
      return true;
    }
  };
  return false;
}