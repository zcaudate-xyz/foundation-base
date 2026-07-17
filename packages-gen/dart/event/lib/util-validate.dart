import 'package:xtalk_lang/common-data.dart' as xtd;

validate_step(form, field, guards, index, result, hook_fn, complete_fn) {
  guards = ((null == guards) ? <dynamic>[] : guards);
  if(index < guards.length){
    var guard = guards[index];
    var value_41292 = guard;
    var id = value_41292[0];
    var m = value_41292[1];
    var check = m["check"];
    var message = m["message"];
    var error_fn = () {
      xtd.obj_assign(result["fields"][field],<dynamic, dynamic>{
        "status":"errored",
        "id":id,
        "data":form[field],
        "message":message
      });
      if(null != hook_fn){
        Function.apply((hook_fn as Function),<dynamic>[id,false]);
      }
      if(null != complete_fn){
        Function.apply((complete_fn as Function),<dynamic>[false,result]);
      }
      return result;
    };
    return (() async { try { return await ((Future.sync(() => ((Future.sync(() => Future.sync(() {
      return check(form[field],form);
    }))) as Future<dynamic>).then((value) async { return await Function.apply((ok) {
      if(ok == false){
        return Function.apply((error_fn as Function),<dynamic>[]);
      }
      else{
        if(null != hook_fn){
          Function.apply((hook_fn as Function),<dynamic>[id,true]);
        }
        return validate_step(form,field,guards,index + 1,result,hook_fn,complete_fn);
      }
    },<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((_) {
      return Function.apply((error_fn as Function),<dynamic>[]);
    },<dynamic>[err])); } })();
  }
  else{
    var entry = result["fields"][field];
    if(null != entry){
      if(entry.containsKey("id")){
        entry.remove("id");
      }
      if(entry.containsKey("data")){
        entry.remove("data");
      }
      if(entry.containsKey("message")){
        entry.remove("message");
      }
      xtd.obj_assign(entry,<dynamic, dynamic>{"status":"ok"});
    }
    if(null != complete_fn){
      Function.apply((complete_fn as Function),<dynamic>[true,result]);
    }
    return Future.sync(() {
      return result;
    });
  }
}

validate_field(form, field, validators, result, hook_fn, complete_fn) {
  var guards = validators[field];
  var complete_status_fn = (passed, status) {
    if(!((null != passed) && (false != passed))){
      result["status"] = "errored";
    }
    if(null != complete_fn){
      Function.apply((complete_fn as Function),<dynamic>[passed,status]);
    }
  };
  return validate_step(form,field,guards,0,result,hook_fn,complete_status_fn);
}

validate_fields_loop(form, validators, result, fields, index, hook_fn, complete_fn) {
  if("errored" == result["status"]){
    if(null != complete_fn){
      Function.apply((complete_fn as Function),<dynamic>[false,result]);
    }
    return Future.sync(() {
      return result;
    });
  }
  if(index >= fields.length){
    result["status"] = "ok";
    if(null != complete_fn){
      Function.apply((complete_fn as Function),<dynamic>[true,result]);
    }
    return Future.sync(() {
      return result;
    });
  }
  var field = fields[index];
  return ((Future.sync(() => validate_field(form,field,validators,result,hook_fn,null))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return validate_fields_loop(form,validators,result,fields,index + 1,hook_fn,complete_fn);
  },<dynamic>[value]); });
}

validate_all(form, validators, result, hook_fn, complete_fn) {
  var fields = List<dynamic>.from(( validators ).keys);
  return validate_fields_loop(form,validators,result,fields,0,hook_fn,complete_fn);
}

create_result(validators) {
  var result = <dynamic, dynamic>{
    "::":"validation.result",
    "status":"pending",
    "fields":xtd.obj_map(validators,(_) {
        return <dynamic, dynamic>{"status":"pending"};
      })
  };
  return result;
}