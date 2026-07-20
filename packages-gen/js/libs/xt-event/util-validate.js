const xtd = require("@xtalk/lang/common-data.js")

function validate_step(form,field,guards,index,result,hook_fn,complete_fn){
  guards = ((null == guards) ? [] : guards);
  if(index < guards.length){
    let guard = guards[index];
    let [id,m] = guard;
    let {check,message} = m;
    let error_fn = function (){
      Object.assign(result["fields"][field],{
        "status":"errored",
        "id":id,
        "data":form[field],
        "message":message
      });
      if(null != hook_fn){
        hook_fn(id,false);
      }
      if(null != complete_fn){
        complete_fn(false,result);
      }
      return result;
    };
    return Promise.resolve().then(function (){
      return check(form[field],form);
    }).then(function (ok){
      if(ok == false){
        return error_fn();
      }
      else{
        if(null != hook_fn){
          hook_fn(id,true);
        }
        return validate_step(form,field,guards,index + 1,result,hook_fn,complete_fn);
      }
    }).catch(function (_){
      return error_fn();
    });
  }
  else{
    let entry = result["fields"][field];
    if(null != entry){
      if(null != entry["id"]){
        delete(entry["id"]);
      }
      if(null != entry["data"]){
        delete(entry["data"]);
      }
      if(null != entry["message"]){
        delete(entry["message"]);
      }
      Object.assign(entry,{"status":"ok"});
    }
    if(null != complete_fn){
      complete_fn(true,result);
    }
    return Promise.resolve().then(function (){
      return result;
    });
  }
}

function validate_field(form,field,validators,result,hook_fn,complete_fn){
  let guards = validators[field];
  let complete_status_fn = function (passed,status){
    if(!passed){
      result["status"] = "errored";
    }
    if(null != complete_fn){
      complete_fn(passed,status);
    }
  };
  return validate_step(form,field,guards,0,result,hook_fn,complete_status_fn);
}

function validate_fields_loop(form,validators,result,fields,index,hook_fn,complete_fn){
  if("errored" == result["status"]){
    if(null != complete_fn){
      complete_fn(false,result);
    }
    return Promise.resolve().then(function (){
      return result;
    });
  }
  if(index >= fields.length){
    result["status"] = "ok";
    if(null != complete_fn){
      complete_fn(true,result);
    }
    return Promise.resolve().then(function (){
      return result;
    });
  }
  let field = fields[index];
  return validate_field(form,field,validators,result,hook_fn,null).then(function (_){
    return validate_fields_loop(form,validators,result,fields,index + 1,hook_fn,complete_fn);
  });
}

function validate_all(form,validators,result,hook_fn,complete_fn){
  let fields = Object.keys(validators);
  return validate_fields_loop(form,validators,result,fields,0,hook_fn,complete_fn);
}

function create_result(validators){
  let result = {
    "::":"validation.result",
    "status":"pending",
    "fields":xtd.obj_map(validators,function (_){
        return {"status":"pending"};
      })
  };
  return result;
}

module.exports = {
  ["validate_step"]:validate_step,
  ["validate_field"]:validate_field,
  ["validate_fields_loop"]:validate_fields_loop,
  ["validate_all"]:validate_all,
  ["create_result"]:create_result
}