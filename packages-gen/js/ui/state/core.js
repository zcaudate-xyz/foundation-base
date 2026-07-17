function controller_create(initial_state,handlers,lifecycle,deps){
  return {
    "state":initial_state || {},
    "revision":0,
    "handlers":handlers || {},
    "lifecycle":lifecycle || {},
    "deps":deps || {},
    "listeners":{},
    "opened":false
  };
}

function snapshot(controller){
  return controller["state"];
}

function revision(controller){
  return controller["revision"];
}

function notifyf(controller){
  for(let listener of Object.values(controller["listeners"])){
    listener(snapshot(controller),revision(controller));
  };
  return controller;
}

function set_statef(controller,state){
  controller["state"] = (state || {});
  controller["revision"] = (1 + revision(controller));
  notifyf(controller);
  return state;
}

function update_statef(controller,update_fn){
  return set_statef(controller,update_fn(snapshot(controller)));
}

function subscribef(controller,listener_id,listener){
  controller["listeners"][listener_id] = listener;
  return listener_id;
}

function unsubscribef(controller,listener_id){
  delete(controller["listeners"][listener_id]);
  return true;
}

function dispatchf(controller,action_id,payload){
  let handler = (controller["handlers"])[action_id];
  if(!("function" == (typeof handler))){
    return Promise.resolve().then(function (){
      return {"status":"unavailable","action":action_id};
    });
  }
  return Promise.resolve().then(function (){
    return handler(controller,payload,controller["deps"]);
  });
}

function actions_create(controller,action_ids){
  let actions = {};
  for(let action_id of action_ids || []){
    actions[action_id] = (function (payload){
      return dispatchf(controller,action_id,payload);
    });
  };
  return actions;
}

function openf(controller){
  if(true == controller["opened"]){
    return Promise.resolve().then(function (){
      return controller;
    });
  }
  controller["opened"] = true;
  let handler = (controller["lifecycle"])["open"];
  if(!("function" == (typeof handler))){
    return Promise.resolve().then(function (){
      return controller;
    });
  }
  return Promise.resolve().then(function (){
    return handler(controller,controller["deps"]);
  }).then(function (_){
    return controller;
  });
}

function closef(controller){
  if(true != controller["opened"]){
    return Promise.resolve().then(function (){
      return true;
    });
  }
  controller["opened"] = false;
  let handler = (controller["lifecycle"])["close"];
  let finish = function (_){
    controller["listeners"] = {};
    return true;
  };
  if(!("function" == (typeof handler))){
    return Promise.resolve().then(function (){
      return null;
    }).then(finish);
  }
  return Promise.resolve().then(function (){
    return handler(controller,controller["deps"]);
  }).then(finish);
}

module.exports = {
  ["controller_create"]:controller_create,
  ["snapshot"]:snapshot,
  ["revision"]:revision,
  ["notifyf"]:notifyf,
  ["set_statef"]:set_statef,
  ["update_statef"]:update_statef,
  ["subscribef"]:subscribef,
  ["unsubscribef"]:unsubscribef,
  ["dispatchf"]:dispatchf,
  ["actions_create"]:actions_create,
  ["openf"]:openf,
  ["closef"]:closef
}