import 'package:xtalk_db/text/base-check.dart' as check;

import 'package:xtalk_db/text/sql-util.dart' as ut;

import 'package:xtalk_net/conn-sql.dart' as conn_sql;

decode_return(outstr, alt) {
  var out = jsonDecode(outstr);
  var data = out["data"];
  var status = out["status"];
  if("error" == status){
    throw "ERR - API: " + outstr;
  }
  return data;
}

call_format_input(spec, args) {
  var targs = spec["input"];
  var out = <dynamic>[];
  var arr_42699 = args;
  for(var i = 0; i < arr_42699.length; ++i){
    var arg = arr_42699[i];
    var input = targs[i];
    var dbarg = null;
    if(input["type"] == "jsonb"){
      if("String" == (arg.runtimeType).toString()){
        dbarg = arg;
      }
      else{
        dbarg = ut.encode_json(arg);
      }
    }
    else{
      dbarg = ut.encode_value(arg);
    }
    out.add(dbarg);
  };
  return out;
}

call_format_query(spec, args) {
  var id = spec["id"];
  var schema = spec["schema"];
  var dbname = "\"" + schema + "\"." + id.replaceAll("-","_") + "";
  var dbargs = call_format_input(spec,args).join(", ");
  return "SELECT " + dbname + "(" + dbargs + ");";
}

call_raw(client, spec, args) {
  var targs = spec["input"];
  var value_42720 = check.check_args_length(args,targs);
  var l_ok = value_42720[0];
  var l_err = value_42720[1];
  if(!((null != l_ok) && (false != l_ok))){
    throw "ERR: - " + jsonEncode(l_err);
  }
  var value_42721 = check.check_args_type(args,targs);
  var t_ok = value_42721[0];
  var t_err = value_42721[1];
  if(!((null != t_ok) && (false != t_ok))){
    throw "ERR: - " + jsonEncode(t_err);
  }
  var q = call_format_query(spec,args);
  var success_fn = (val) {
    if("jsonb" == spec["return"]){
      if((null == val) || (val == "")){
        return null;
      }
      else{
        return ("String" == (val.runtimeType).toString()) ? jsonDecode(val) : val;
      }
    }
    else{
      return val;
    }
  };
  var error_fn = (err) {
    throw "ERR: - " + jsonEncode(err);
  };
  return (() async { try { return await ((Future.sync(() => ((Future.sync(() => conn_sql.query_async(client,q))) as Future<dynamic>).then((value) async { return await Function.apply(success_fn,<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply(error_fn,<dynamic>[err])); } })();
}

call_api(client, spec, args) {
  var targs = spec["input"];
  var value_42722 = check.check_args_length(args,targs);
  var l_ok = value_42722[0];
  var l_err = value_42722[1];
  if(!((null != l_ok) && (false != l_ok))){
    return jsonEncode(<dynamic, dynamic>{"status":"error","data":l_err});
  }
  var value_42723 = check.check_args_type(args,targs);
  var t_ok = value_42723[0];
  var t_err = value_42723[1];
  if(!((null != t_ok) && (false != t_ok))){
    return jsonEncode(<dynamic, dynamic>{"status":"error","data":t_err});
  }
  var q = call_format_query(spec,args);
  var success_fn = (val) {
    return "{\"status\": \"ok\", \"data\":" + (("jsonb" == spec["return"]) ? (("String" == (val.runtimeType).toString()) ? val : jsonEncode(val)) : jsonEncode(val)) + "}";
  };
  var error_fn = (err) {
    if((() {
      var dart_truthy__42698 = err["status"];
      return (null != dart_truthy__42698) && (false != dart_truthy__42698);
    })()){
      return jsonEncode(err);
    }
    else{
      return jsonEncode(<dynamic, dynamic>{"status":"error","data":err});
    }
  };
  return (() async { try { return await ((Future.sync(() => ((Future.sync(() => conn_sql.query_async(client,q))) as Future<dynamic>).then((value) async { return await Function.apply(success_fn,<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply(error_fn,<dynamic>[err])); } })();
}