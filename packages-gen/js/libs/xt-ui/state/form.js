const xtd = require("@xtalk/lang/common-data.js")

function clone(value){
  return xtd.clone_nested(value || {});
}

function create(values,validators){
  let initial = clone(values);
  return {
    "initial":initial,
    "draft":clone(initial),
    "validators":validators || {},
    "errors":{},
    "touched":{},
    "dirty":false,
    "valid":true,
    "pending":false
  };
}

function validate_value(validators,value,draft){
  let message = null;
  for(let validator of validators || []){
    if(null == message){
      message = validator(value,draft);
    }
  };
  return message;
}

function validatef(form){
  let errors = {};
  let draft = form["draft"];
  for(let [field,validators] of Object.entries(form["validators"])){
    let message = validate_value(validators,draft[field],draft);
    if(null != message){
      errors[field] = message;
    }
  };
  form["errors"] = errors;
  form["valid"] = (0 == Object.keys(errors).length);
  return form["valid"];
}

function set_fieldf(form,path,value){
  let draft = clone(form["draft"]);
  xtd.set_in(draft,path,value);
  form["draft"] = draft;
  xtd.set_in(form["touched"],path,true);
  form["dirty"] = true;
  validatef(form);
  return draft;
}

function resetf(form){
  form["draft"] = clone(form["initial"]);
  form["errors"] = {};
  form["touched"] = {};
  form["dirty"] = false;
  form["valid"] = true;
  form["pending"] = false;
  return form;
}

function pendingf(form,pending){
  form["pending"] = (true == pending);
  return form;
}

module.exports = {
  ["clone"]:clone,
  ["create"]:create,
  ["validate_value"]:validate_value,
  ["validatef"]:validatef,
  ["set_fieldf"]:set_fieldf,
  ["resetf"]:resetf,
  ["pendingf"]:pendingf
}