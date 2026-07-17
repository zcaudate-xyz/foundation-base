import 'package:xtalk_lang/common-data.dart' as xtd;

spec(id, kind, regions, opts) {
  return <dynamic, dynamic>{
    "id":id,
    "kind":kind,
    "regions":regions ?? <dynamic, dynamic>{},
    "opts":opts ?? <dynamic, dynamic>{}
  };
}

region(frame, region_id, fallback) {
  return frame["regions"][region_id] ?? fallback;
}

override(frame, overrides) {
  var next = xtd.clone_nested(frame);
  for(var entry_50398 in (overrides ?? <dynamic, dynamic>{}).entries){
    var region_id = entry_50398.key;
    var value = entry_50398.value;
    xtd.set_in(next,<dynamic>["regions",region_id],value);
  };
  return next;
}