const router = require("@xtalk/substrate/base-router.js")

const space = require("@xtalk/substrate/base-space.js")

function subscribe(node,space,signal,subscription_id,meta){
  return router.subscribe_frame(space,signal,subscription_id,meta);
}

function unsubscribe(node,space,signal,subscription_id,meta){
  return router.unsubscribe_frame(space,signal,subscription_id,meta);
}

function invoke_trigger(node,stream){
  let signal = stream["signal"];
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

function receive_publish(node,stream){
  space.ensure_space(node,stream["space"],null);
  return invoke_trigger(node,stream).then(function (_){
    return stream;
  });
}

module.exports = {
  ["subscribe"]:subscribe,
  ["unsubscribe"]:unsubscribe,
  ["invoke_trigger"]:invoke_trigger,
  ["receive_publish"]:receive_publish
}