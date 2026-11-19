import * as router from './base-router.mjs'

import * as space from './base-space.mjs'

export function subscribe(node,space,signal,subscription_id,meta){
  return router.subscribe_frame(space,signal,subscription_id,meta);
}

export function unsubscribe(node,space,signal,subscription_id,meta){
  return router.unsubscribe_frame(space,signal,subscription_id,meta);
}

export function invoke_trigger(node,stream){
  let {signal} = stream;
  let entry = (node["triggers"])[signal];
  if(null == entry){
    return Promise.resolve().then(function (){
      return null;
    });
  }
  let current_space = space.ensure_space(node,stream["space"],null);
  let trigger_fn = entry["fn"];
  let output = trigger_fn(current_space,stream,node);
  if(output instanceof Promise){
    return output;
  }
  else{
    return Promise.resolve().then(function (){
      return output;
    });
  }
}

export function receive_publish(node,stream){
  space.ensure_space(node,stream["space"],null);
  return invoke_trigger(node,stream).then(function (_){
    return stream;
  });
}