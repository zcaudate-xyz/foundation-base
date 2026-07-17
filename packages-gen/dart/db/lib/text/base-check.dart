is_uuidp(s) {
  if(!("String" == (s.runtimeType).toString())){
    return false;
  }
  if(!(36 == s.length)){
    return false;
  }
  var parts = s.split("-");
  return (5 == parts.length) && (8 == parts[0].length) && (4 == parts[1].length) && (4 == parts[2].length) && (4 == parts[3].length) && (12 == parts[4].length);
}

check_arg_type(arg_type, arg) {
  if(arg_type == "any"){
    return true;
  }
  else if((arg_type == "citext") || (arg_type == "inet") || (arg_type == "text")){
    return "String" == (arg.runtimeType).toString();
  }
  else if(arg_type == "uuid"){
    return is_uuidp(arg);
  }
  else if(arg_type == "boolean"){
    return "bool" == (arg.runtimeType).toString();
  }
  else if((arg_type == "integer") || (arg_type == "int") || (arg_type == "long") || (arg_type == "bigint") || (arg_type == "float")){
    return ("int" == (arg.runtimeType).toString()) || ("double" == (arg.runtimeType).toString()) || ("num" == (arg.runtimeType).toString());
  }
  else if(arg_type == "numeric"){
    return (("int" == (arg.runtimeType).toString()) || ("double" == (arg.runtimeType).toString()) || ("num" == (arg.runtimeType).toString())) || ("String" == (arg.runtimeType).toString());
  }
  else if(arg_type == "jsonb"){
    return (("Map" == (arg.runtimeType).toString()) || (arg.runtimeType).toString().startsWith("_Map") || (arg.runtimeType).toString().startsWith("LinkedMap")) || ((arg.runtimeType).toString().startsWith("List") || (arg.runtimeType).toString().startsWith("_GrowableList"));
  }
  else{
    return false;
  }
}

check_args_type(args, targs) {
  var i = 0;
  var arr_43012 = targs;
  for(var i43013 = 0; i43013 < arr_43012.length; ++i43013){
    var spec = arr_43012[i43013];
    var arg = args[i];
    if(!(() {
      var dart_truthy__43011 = check_arg_type(spec["type"],arg);
      return (null != dart_truthy__43011) && (false != dart_truthy__43011);
    })()){
      return <dynamic>[
        false,
        <dynamic, dynamic>{
              "status":"error",
              "tag":"net/arg-typecheck-failed",
              "data":<dynamic, dynamic>{"input":arg,"spec":spec}
            }
      ];
    }
    i = (i + 1);
  };
  return <dynamic>[true,null];
}

check_args_length(args, targs) {
  if(args.length != targs.length){
    return <dynamic>[
      false,
      <dynamic, dynamic>{
          "status":"error",
          "tag":"net/args-not-same-length",
          "data":<dynamic, dynamic>{"expected":targs.length,"actual":args.length,"input":args}
        }
    ];
  }
  return <dynamic>[true,null];
}