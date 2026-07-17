import 'package:xtalk_substrate/base-router.dart' as router;

import 'package:xtalk_substrate/base-space.dart' as space;

subscribe(node, space, signal, subscription_id, meta) {
  return router.subscribe_frame(space,signal,subscription_id,meta);
}

unsubscribe(node, space, signal, subscription_id, meta) {
  return router.unsubscribe_frame(space,signal,subscription_id,meta);
}

invoke_trigger(node, stream) {
  var signal = stream["signal"];
  var entry = (node["triggers"])[signal];
  if(null == entry){
    return Future.sync(() {
      return null;
    });
  }
  var current_space = space.ensure_space(node,stream["space"],null);
  var trigger_fn = entry["fn"];
  var output = Function.apply(
    (trigger_fn as Function),
    <dynamic>[current_space,stream,node]
  );
  if((() {
    var dart_truthy__41921 = (null != output) && (("Future" == (output.runtimeType).toString()) || (output.runtimeType).toString().startsWith("Future<"));
    return (null != dart_truthy__41921) && (false != dart_truthy__41921);
  })()){
    return output;
  }
  else{
    return Future.sync(() {
      return output;
    });
  }
}

receive_publish(node, stream) {
  space.ensure_space(node,stream["space"],null);
  return ((Future.sync(() => invoke_trigger(node,stream))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return stream;
  },<dynamic>[value]); });
}