const event_common = require("@xtalk/event/base-listener.js")

const xtd = require("@xtalk/lang/common-data.js")

const validate = require("@xtalk/event/util-validate.js")

function make_form(initial,validators){
  let result = validate.create_result(validators);
  return event_common.make_container(
    initial,
    "event.form",
    {"result":result,"validators":validators}
  );
}

function check_event(event,fields){
  for(let field of fields){
    for(let evfield of event["fields"]){
      if(evfield == field){
        return true;
      }
    };
  };
  return false;
}

function add_listener(form,listener_id,fields,callback,meta){
  fields = event_common.arrayify_path(fields);
  return event_common.add_listener(form,listener_id,"form",callback,Object.assign({"form/fields":fields},meta),function (event){
    return check_event(event,fields);
  });
}

var remove_listener = event_common.remove_listener;

var list_listeners = event_common.list_listeners;

function trigger_all(form,event_type){
  let {validators} = form;
  let fields = Object.keys(validators);
  return event_common.trigger_listeners(form,{"type":event_type,"fields":fields});
}

function trigger_field(form,fields,event_type){
  return event_common.trigger_listeners(form,{
    "type":event_type,
    "fields":event_common.arrayify_path(fields)
  });
}

function set_field(form,field,value){
  let {data} = form;
  data[field] = value;
  return trigger_field(form,field,"form.data");
}

function get_field(form,field){
  let {data} = form;
  return data[field];
}

function toggle_field(form,field){
  return set_field(form,field,!get_field(form,field));
}

function field_fn(form,field){
  return function (value){
    return set_field(form,field,value);
  };
}

function get_result(form){
  return form["result"];
}

function get_field_result(form,field){
  let {result} = form;
  let {fields} = result;
  return fields[field];
}

function get_data(form){
  return form["data"];
}

function set_data(form,m){
  let {data} = form;
  Object.assign(data,m);
  let fields = Object.keys(m);
  return trigger_field(form,fields,"form.data");
}

function reset_all_data(form){
  let {initial} = form;
  let data = initial();
  form["data"] = data;
  return trigger_all(form,"form.data");
}

function reset_field_data(form,field){
  let {data,initial} = form;
  let value = (initial())[field];
  data[field] = value;
  return trigger_field(form,field,"form.data");
}

function validate_all(form,hook_fn,complete_fn){
  let {data,result,validators} = form;
  return validate.validate_all(data,validators,result,hook_fn,null).then(function (res){
    trigger_all(form,"form.validation");
    if(complete_fn){
      complete_fn("ok" == res["status"],res);
    }
    return res;
  });
}

function validate_field(form,field,hook_fn,complete_fn){
  let {data,result,validators} = form;
  return validate.validate_field(data,field,validators,result,hook_fn,null).then(function (res){
    trigger_field(form,field,"form.validation");
    if(complete_fn){
      complete_fn("ok" == xtd.get_in(res["fields"],[field,"status"]),res);
    }
    return res;
  });
}

function reset_field_validator(form,field){
  let {result} = form;
  result[field] = {"status":"pending"};
  trigger_field(form,field,"form.validation");
  return result;
}

function reset_all_validators(form){
  let {result,validators} = form;
  form["result"] = validate.create_result(validators);
  trigger_all(form,"form.validation");
  return result;
}

function reset_all(form){
  reset_all_data(form);
  reset_all_validators(form);
}

function check_field_passed(form,field){
  let {result} = form;
  let {fields} = result;
  return "ok" == xtd.get_in(fields,[field,"status"]);
}

function check_field_errored(form,field){
  let {result} = form;
  let {fields} = result;
  return "errored" == xtd.get_in(fields,[field,"status"]);
}

function check_all_passed(form){
  let {result} = form;
  let {fields} = result;
  for(let v of Object.values(fields)){
    if("ok" != v["status"]){
      return false;
    }
  };
  return true;
}

function check_any_errored(form){
  let {result} = form;
  let {fields} = result;
  for(let v of Object.values(fields)){
    if("errored" == v["status"]){
      return true;
    }
  };
  return false;
}

module.exports = {
  ["make_form"]:make_form,
  ["check_event"]:check_event,
  ["add_listener"]:add_listener,
  ["remove_listener"]:remove_listener,
  ["list_listeners"]:list_listeners,
  ["trigger_all"]:trigger_all,
  ["trigger_field"]:trigger_field,
  ["set_field"]:set_field,
  ["get_field"]:get_field,
  ["toggle_field"]:toggle_field,
  ["field_fn"]:field_fn,
  ["get_result"]:get_result,
  ["get_field_result"]:get_field_result,
  ["get_data"]:get_data,
  ["set_data"]:set_data,
  ["reset_all_data"]:reset_all_data,
  ["reset_field_data"]:reset_field_data,
  ["validate_all"]:validate_all,
  ["validate_field"]:validate_field,
  ["reset_field_validator"]:reset_field_validator,
  ["reset_all_validators"]:reset_all_validators,
  ["reset_all"]:reset_all,
  ["check_field_passed"]:check_field_passed,
  ["check_field_errored"]:check_field_errored,
  ["check_all_passed"]:check_all_passed,
  ["check_any_errored"]:check_any_errored
}