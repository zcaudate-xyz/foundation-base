import 'package:xtalk_lang/common-data.dart' as xtd;

trace_log() {
  if((() {
    var dart_truthy__39899 = !(null == __globals__["TRACE"]);
    return (null != dart_truthy__39899) && (false != dart_truthy__39899);
  })()){
    return __globals__["TRACE"];
  }
  else{
    __globals__["TRACE"] = <dynamic>[];
    return __globals__["TRACE"];
  }
}

trace_log_clear() {
  __globals__["TRACE"] = <dynamic>[];
  return __globals__["TRACE"];
}

trace_log_add(data, tag, opts) {
  var log = trace_log();
  var m = xtd.obj_assign(<dynamic, dynamic>{
    "tag":tag,
    "data":data,
    "time":DateTime.now().millisecondsSinceEpoch
  },opts);
  log.add(m);
  return log.length;
}

trace_filter(tag) {
  return xtd.arr_filter(trace_log(),(e) {
    return tag == e["tag"];
  });
}

trace_last_entry(tag) {
  var log = trace_log();
  if(null == tag){
    return log[log.length + -1];
  }
  else{
    var tagged = trace_filter(tag);
    return tagged[tagged.length + -1];
  }
}

trace_data(tag) {
  return xtd.arr_map(trace_log(),(e) {
    return e["data"];
  });
}

trace_last(tag) {
  return (trace_last_entry(tag))["data"];
}

trace_run(f) {
  trace_log_clear();
  f();
  return trace_log();
}