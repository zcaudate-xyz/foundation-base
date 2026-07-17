const xtd = require("@xtalk/lang/common-data.js")

const impl_common = require("@xtalk/db/system/impl-common.js")

const supabase_ws = require("@xtalk/db/system/impl-supabase-ws.js")

const websocket = require("@xtalk/net/ws-native.js")

const http_util = require("@xtalk/net/http-util.js")

const phoenix = require("@xtalk/net/ws-phoenix.js")

function prepare_connect_url(impl,params){
  let {client} = impl;
  let {defaults} = client;
  let path = "/realtime/v1/websocket" + "?" + http_util.encode_query_params(
    Object.assign({"vsn":"1.0.0","apikey":defaults["apikey"]},params)
  );
  return websocket.prepare_url(client,{"path":path});
}

function get_auth_token(impl){
  return xtd.get_in(impl,["state","session","access_token"]) || xtd.get_in(impl,["client","defaults","apikey"]);
}

function topic_join_payload(impl,topic){
  let auth_token = get_auth_token(impl);
  let payload = {"config":{"broadcast":{"ack":false,"self":false}}};
  if(null != auth_token){
    payload["access_token"] = auth_token;
  }
  return phoenix.make_frame_join(payload,{"topic":topic,"ref":"#/join/" + topic});
}

function topic_leave_payload(impl,topic){
  return phoenix.make_frame_leave({"topic":topic,"ref":"#/leave/" + topic});
}

function create_realtime_on_message(realtime_client){
  return phoenix.wrap_phoenix({
    "broadcast":function (frame){
        let envelope = frame["payload"];
        let event = envelope["event"];
        if(("xt.db/event" == event) || ("db/sync" == event) || ("db/remove" == event)){
          let payload = envelope["payload"];
          let topic = frame["topic"];
          let callbacks = xtd.get_in(realtime_client,["state","callbacks"]);
          for(let [_id,callback] of Object.entries(callbacks)){
            callback(Object.assign({"topic":topic},payload));
          };
        }
      },
    "phx_reply":function (frame){
        let topic = frame["topic"];
        let entry = xtd.get_in(realtime_client,["state","topics",topic]);
        if((null != entry) && ("object" == (typeof entry)) && !Array.isArray(entry)){
          let status = xtd.get_in(frame,["payload","status"]);
          let ok = status == "ok";
          let deferred = entry["deferred"];
          let resolve = deferred["resolve"];
          xtd.set_in(realtime_client,["state","topics",topic,"ready"],ok);
          if("function" == (typeof resolve)){
            resolve(ok);
          }
        }
      }
  });
}

function create_realtime(impl,conn_id){
  let realtime_client = supabase_ws.create_ws_client(impl,{"id":conn_id});
  xtd.set_in(realtime_client,["state","callbacks"],{});
  xtd.set_in(realtime_client,["state","topics"],{});
  let ws_url = prepare_connect_url(impl,{});
  let init = websocket.connect(realtime_client,{"url":ws_url});
  xtd.set_in(realtime_client,["state","init"],init);
  websocket.add_listeners(realtime_client,{
    "open":function (raw){
        phoenix.start_heartbeat(realtime_client);
      },
    "message":create_realtime_on_message(realtime_client)
  });
  return realtime_client;
}

function get_realtime(impl,conn_id){
  return xtd.get_in(impl,["state","realtimes",conn_id]);
}

function set_realtime(impl,conn_id,client){
  xtd.set_in(impl,["state","realtimes",conn_id],client);
  return client;
}

function ensure_realtime(impl,conn_id){
  let client = get_realtime(impl,conn_id);
  if(null != client){
    return client;
  }
  client = create_realtime(impl,conn_id);
  set_realtime(impl,conn_id,client);
  return client;
}

function remove_realtime(impl,conn_id){
  let client = get_realtime(impl,conn_id);
  if(null != client){
    xtd.set_in(client,["state","topics"],{});
    websocket.disconnect(client);
  }
  delete(xtd.get_in(impl,["state","realtimes"])[conn_id]);
  return client;
}

function get_realtime_callback(impl,conn_id,callback_id){
  return xtd.get_in(get_realtime(impl,conn_id),["state","callbacks",callback_id]);
}

function add_realtime_callback(impl,conn_id,callback_id,handler){
  let client = ensure_realtime(impl,conn_id);
  let callbacks = xtd.get_in(client,["state","callbacks"]);
  if(null == callbacks){
    callbacks = {};
    xtd.set_in(client,["state","callbacks"],callbacks);
  }
  callbacks[callback_id] = handler;
  return handler;
}

function remove_realtime_callback(impl,conn_id,callback_id){
  let client = get_realtime(impl,conn_id);
  if(null != client){
    let callbacks = xtd.get_in(client,["state","callbacks"]);
    delete(callbacks[callback_id]);
  }
  return true;
}

function get_topics(impl,conn_id){
  let client = get_realtime(impl,conn_id);
  if(null != client){
    return xtd.get_in(client,["state","topics"]);
  }
  else{
    return {};
  }
}

function create_sync_callback(impl){
  return function (event){
    let caching_fn = xtd.get_in(impl,["state","caching_fn"]);
    if("function" == (typeof caching_fn)){
      let caching_impl = caching_fn();
      if((null != caching_impl) && ((null != event["db/sync"]) || (null != event["db/remove"]))){
        let payload = Object.assign({},event);
        delete(payload["topic"]);
        impl_common.sync_process_payload(caching_impl,payload);
      }
    }
  };
}

function subscribe(impl,conn_id,topics){
  let client = ensure_realtime(impl,conn_id);
  add_realtime_callback(impl,conn_id,"db-sync",create_sync_callback(impl));
  for(let topic of topics){
    let join_ref = "#/join/" + topic;
    let deferred = {"resolve":null,"reject":null};
    let init = new Promise(function (resolve,reject){
      (function (resolve,reject){
        deferred["resolve"] = resolve;
        deferred["reject"] = reject;
      })(resolve,reject);
    });
    xtd.set_in(client,["state","topics",topic],{
      "init":init,
      "join_ref":join_ref,
      "deferred":deferred,
      "ready":false
    });
  };
  return xtd.get_in(client,["state","init"]).then(function (_){
    for(let topic of topics){
      phoenix.send_frame(client,topic_join_payload(impl,topic));
    };
    return Promise.all(topics.map(function (topic){
      return xtd.get_in(client,["state","topics",topic,"init"]);
    }));
  });
}

function unsubscribe(impl,conn_id,topics){
  return Promise.resolve().then(function (){
    let client = get_realtime(impl,conn_id);
    if(null != client){
      for(let topic of topics){
        let entry = xtd.get_in(client,["state","topics",topic]);
        if(null != entry){
          phoenix.send_frame(client,topic_leave_payload(impl,topic));
          delete(xtd.get_in(client,["state","topics"])[topic]);
        }
      };
    }
    return true;
  });
}

module.exports = {
  ["prepare_connect_url"]:prepare_connect_url,
  ["get_auth_token"]:get_auth_token,
  ["topic_join_payload"]:topic_join_payload,
  ["topic_leave_payload"]:topic_leave_payload,
  ["create_realtime_on_message"]:create_realtime_on_message,
  ["create_realtime"]:create_realtime,
  ["get_realtime"]:get_realtime,
  ["set_realtime"]:set_realtime,
  ["ensure_realtime"]:ensure_realtime,
  ["remove_realtime"]:remove_realtime,
  ["get_realtime_callback"]:get_realtime_callback,
  ["add_realtime_callback"]:add_realtime_callback,
  ["remove_realtime_callback"]:remove_realtime_callback,
  ["get_topics"]:get_topics,
  ["create_sync_callback"]:create_sync_callback,
  ["subscribe"]:subscribe,
  ["unsubscribe"]:unsubscribe
}