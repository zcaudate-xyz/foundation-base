import 'dart:async';

controller_create(initial_state, handlers, lifecycle, deps) {
  return <dynamic, dynamic>{
    "state":initial_state ?? <dynamic, dynamic>{},
    "revision":0,
    "handlers":handlers ?? <dynamic, dynamic>{},
    "lifecycle":lifecycle ?? <dynamic, dynamic>{},
    "deps":deps ?? <dynamic, dynamic>{},
    "listeners":<dynamic, dynamic>{},
    "opened":false
  };
}

snapshot(controller) {
  return controller["state"];
}

revision(controller) {
  return controller["revision"];
}

notifyf(controller) {
  for(var listener in controller["listeners"].values){
    listener(snapshot(controller),revision(controller));
  };
  return controller;
}

set_statef(controller, state) {
  controller["state"] = (state ?? <dynamic, dynamic>{});
  controller["revision"] = (1 + revision(controller));
  notifyf(controller);
  return state;
}

update_statef(controller, update_fn) {
  return set_statef(
    controller,
    Function.apply((update_fn as Function),<dynamic>[snapshot(controller)])
  );
}

subscribef(controller, listener_id, listener) {
  controller["listeners"][listener_id] = listener;
  return listener_id;
}

unsubscribef(controller, listener_id) {
  controller["listeners"].remove(listener_id);
  return true;
}

dispatchf(controller, action_id, payload) {
  var handler = (controller["handlers"])[action_id];
  if(!((handler.runtimeType).toString().contains("Function") || (handler.runtimeType).toString().contains("=>") || (handler).toString().startsWith("Closure"))){
    return Future.sync(() {
      return <dynamic, dynamic>{"status":"unavailable","action":action_id};
    });
  }
  return Future.sync(() {
    return Function.apply(
      (handler as Function),
      <dynamic>[controller,payload,controller["deps"]]
    );
  });
}

actions_create(controller, action_ids) {
  var actions = <dynamic, dynamic>{};
  var arr_52974 = action_ids ?? <dynamic>[];
  for(var i52975 = 0; i52975 < arr_52974.length; ++i52975){
    var action_id = arr_52974[i52975];
    actions[action_id] = ((payload) {
      return dispatchf(controller,action_id,payload);
    });
  };
  return actions;
}

openf(controller) {
  if(true == controller["opened"]){
    return Future.sync(() {
      return controller;
    });
  }
  controller["opened"] = true;
  var handler = (controller["lifecycle"])["open"];
  if(!((handler.runtimeType).toString().contains("Function") || (handler.runtimeType).toString().contains("=>") || (handler).toString().startsWith("Closure"))){
    return Future.sync(() {
      return controller;
    });
  }
  return ((Future.sync(() => Future.sync(() {
    return Function.apply(
      (handler as Function),
      <dynamic>[controller,controller["deps"]]
    );
  }))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    return controller;
  },<dynamic>[value]); });
}

closef(controller) {
  if(true != controller["opened"]){
    return Future.sync(() {
      return true;
    });
  }
  controller["opened"] = false;
  var handler = (controller["lifecycle"])["close"];
  var finish = (_) {
    controller["listeners"] = <dynamic, dynamic>{};
    return true;
  };
  if(!((handler.runtimeType).toString().contains("Function") || (handler.runtimeType).toString().contains("=>") || (handler).toString().startsWith("Closure"))){
    return ((Future.sync(() => Future.sync(() {
      return null;
    }))) as Future<dynamic>).then((value) async { return await Function.apply(finish,<dynamic>[value]); });
  }
  return ((Future.sync(() => Future.sync(() {
    return Function.apply(
      (handler as Function),
      <dynamic>[controller,controller["deps"]]
    );
  }))) as Future<dynamic>).then((value) async { return await Function.apply(finish,<dynamic>[value]); });
}