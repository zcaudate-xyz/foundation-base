function is_uuidp(s){
  if(!("string" == (typeof s))){
    return false;
  }
  if(!(36 == s.length)){
    return false;
  }
  let parts = s.split("-");
  return (5 == parts.length) && (8 == parts[0].length) && (4 == parts[1].length) && (4 == parts[2].length) && (4 == parts[3].length) && (12 == parts[4].length);
}

function check_arg_type(arg_type,arg){
  if(arg_type == "any"){
    return true;
  }
  else if((arg_type == "citext") || (arg_type == "inet") || (arg_type == "text")){
    return "string" == (typeof arg);
  }
  else if(arg_type == "uuid"){
    return is_uuidp(arg);
  }
  else if(arg_type == "boolean"){
    return "boolean" == (typeof arg);
  }
  else if((arg_type == "integer") || (arg_type == "int") || (arg_type == "long") || (arg_type == "bigint") || (arg_type == "float")){
    return "number" == (typeof arg);
  }
  else if(arg_type == "numeric"){
    return ("number" == (typeof arg)) || ("string" == (typeof arg));
  }
  else if(arg_type == "jsonb"){
    return ((null != arg) && ("object" == (typeof arg)) && !Array.isArray(arg)) || Array.isArray(arg);
  }
  else{
    return false;
  }
}

function check_args_type(args,targs){
  let i = 0;
  for(let spec of targs){
    let arg = args[i];
    if(!check_arg_type(spec["type"],arg)){
      return [
        false,
        {
              "status":"error",
              "tag":"net/arg-typecheck-failed",
              "data":{"input":arg,"spec":spec}
            }
      ];
    }
    i = (i + 1);
  };
  return [true,null];
}

function check_args_length(args,targs){
  if(args.length != targs.length){
    return [
      false,
      {
          "status":"error",
          "tag":"net/args-not-same-length",
          "data":{"expected":targs.length,"actual":args.length,"input":args}
        }
    ];
  }
  return [true,null];
}

module.exports = {
  ["is_uuidp"]:is_uuidp,
  ["check_arg_type"]:check_arg_type,
  ["check_args_type"]:check_args_type,
  ["check_args_length"]:check_args_length
}