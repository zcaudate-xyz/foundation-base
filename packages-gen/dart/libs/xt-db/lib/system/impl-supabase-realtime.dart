import 'package:xtalk_lang/common-data.dart' as xtd;
import 'package:xtalk_db/system/impl-common.dart' as impl_common;
import 'package:xtalk_db/system/impl-supabase-ws.dart' as supabase_ws;
import 'package:xtalk_net/ws-native.dart' as websocket;
import 'package:xtalk_net/http-util.dart' as http_util;
import 'package:xtalk_net/ws-phoenix.dart' as phoenix;
import 'dart:async';







prepare_connect_url(impl, params) {
  var client = impl["client"];
  var defaults = client["defaults"];
  var path = "/realtime/v1/websocket" + "?" + http_util.encode_query_params(xtd.obj_assign(
    <dynamic, dynamic>{"vsn":"1.0.0","apikey":defaults["apikey"]},
    params
  ));
  return websocket.prepare_url(client,<dynamic, dynamic>{"path":path});
}

get_auth_token(impl) {
  return xtd.get_in(impl,<dynamic>["state","session","access_token"]) ?? xtd.get_in(impl,<dynamic>["client","defaults","apikey"]);
}

topic_join_payload(impl, topic) {
  var auth_token = get_auth_token(impl);
  var payload = <dynamic, dynamic>{
    "config":<dynamic, dynamic>{"broadcast":<dynamic, dynamic>{"ack":false,"self":false}}
  };
  if(null != auth_token){
    payload["access_token"] = auth_token;
  }
  return phoenix.make_frame_join(
    payload,
    <dynamic, dynamic>{"topic":topic,"ref":"#/join/" + topic}
  );
}

topic_leave_payload(impl, topic) {
  return phoenix.make_frame_leave(<dynamic, dynamic>{"topic":topic,"ref":"#/leave/" + topic});
}

create_realtime_on_message(realtime_client) {
  return phoenix.wrap_phoenix(<dynamic, dynamic>{
    "broadcast":(frame) {
        var envelope = frame["payload"];
        var event = envelope["event"];
        if(("xt.db/event" == event) || ("db/sync" == event) || ("db/remove" == event)){
          var payload = envelope["payload"];
          var topic = frame["topic"];
          var callbacks = xtd.get_in(realtime_client,<dynamic>["state","callbacks"]);
          for(var entry_52005 in callbacks.entries){
            var _id = entry_52005.key;
            var callback = entry_52005.value;
            callback(xtd.obj_assign(<dynamic, dynamic>{"topic":topic},payload));
          };
        }
      },
    "phx_reply":(frame) {
        var topic = frame["topic"];
        var entry = xtd.get_in(realtime_client,<dynamic>["state","topics",topic]);
        if(("Map" == (entry.runtimeType).toString()) || (entry.runtimeType).toString().startsWith("_Map") || (entry.runtimeType).toString().startsWith("LinkedMap")){
          var status = xtd.get_in(frame,<dynamic>["payload","status"]);
          var ok = status == "ok";
          var deferred = entry["deferred"];
          var resolve = deferred["resolve"];
          xtd.set_in(realtime_client,<dynamic>["state","topics",topic,"ready"],ok);
          if((resolve.runtimeType).toString().contains("Function") || (resolve.runtimeType).toString().contains("=>") || (resolve).toString().startsWith("Closure")){
            Function.apply((resolve as Function),<dynamic>[ok]);
          }
        }
      }
  });
}

create_realtime(impl, conn_id) {
  var realtime_client = supabase_ws.create_ws_client(impl,<dynamic, dynamic>{"id":conn_id});
  xtd.set_in(
    realtime_client,
    <dynamic>["state","callbacks"],
    <dynamic, dynamic>{}
  );
  xtd.set_in(
    realtime_client,
    <dynamic>["state","topics"],
    <dynamic, dynamic>{}
  );
  var ws_url = prepare_connect_url(impl,<dynamic, dynamic>{});
  var init = websocket.connect(realtime_client,<dynamic, dynamic>{"url":ws_url});
  xtd.set_in(realtime_client,<dynamic>["state","init"],init);
  websocket.add_listeners(realtime_client,<dynamic, dynamic>{
    "open":(raw) {
        return phoenix.start_heartbeat(realtime_client);
      },
    "message":create_realtime_on_message(realtime_client)
  });
  return realtime_client;
}

get_realtime(impl, conn_id) {
  return xtd.get_in(impl,<dynamic>["state","realtimes",conn_id]);
}

set_realtime(impl, conn_id, client) {
  xtd.set_in(impl,<dynamic>["state","realtimes",conn_id],client);
  return client;
}

ensure_realtime(impl, conn_id) {
  var client = get_realtime(impl,conn_id);
  if(null != client){
    return client;
  }
  client = create_realtime(impl,conn_id);
  set_realtime(impl,conn_id,client);
  return client;
}

remove_realtime(impl, conn_id) {
  var client = get_realtime(impl,conn_id);
  if(null != client){
    xtd.set_in(client,<dynamic>["state","topics"],<dynamic, dynamic>{});
    websocket.disconnect(client);
  }
  xtd.get_in(impl,<dynamic>["state","realtimes"]).remove(conn_id);
  return client;
}

get_realtime_callback(impl, conn_id, callback_id) {
  return xtd.get_in(
    get_realtime(impl,conn_id),
    <dynamic>["state","callbacks",callback_id]
  );
}

add_realtime_callback(impl, conn_id, callback_id, handler) {
  var client = ensure_realtime(impl,conn_id);
  var callbacks = xtd.get_in(client,<dynamic>["state","callbacks"]);
  if(null == callbacks){
    callbacks = <dynamic, dynamic>{};
    xtd.set_in(client,<dynamic>["state","callbacks"],callbacks);
  }
  callbacks[callback_id] = handler;
  return handler;
}

remove_realtime_callback(impl, conn_id, callback_id) {
  var client = get_realtime(impl,conn_id);
  if(null != client){
    var callbacks = xtd.get_in(client,<dynamic>["state","callbacks"]);
    callbacks.remove(callback_id);
  }
  return true;
}

get_topics(impl, conn_id) {
  var client = get_realtime(impl,conn_id);
  if(null != client){
    return xtd.get_in(client,<dynamic>["state","topics"]);
  }
  else{
    return <dynamic, dynamic>{};
  }
}

create_sync_callback(impl) {
  return (event) {
    var caching_fn = xtd.get_in(impl,<dynamic>["state","caching_fn"]);
    if((caching_fn.runtimeType).toString().contains("Function") || (caching_fn.runtimeType).toString().contains("=>") || (caching_fn).toString().startsWith("Closure")){
      var caching_impl = Function.apply((caching_fn as Function),<dynamic>[]);
      if((null != caching_impl) && (event.containsKey("db/sync") || event.containsKey("db/remove"))){
        var payload = xtd.obj_clone(event);
        payload.remove("topic");
        impl_common.sync_process_payload(caching_impl,payload);
      }
    }
  };
}

subscribe(impl, conn_id, topics) {
  var client = ensure_realtime(impl,conn_id);
  add_realtime_callback(impl,conn_id,"db-sync",create_sync_callback(impl));
  var arr_52006 = topics;
  for(var i52007 = 0; i52007 < arr_52006.length; ++i52007){
    var topic = arr_52006[i52007];
    var join_ref = "#/join/" + topic;
    var deferred = <dynamic, dynamic>{"resolve":null,"reject":null};
    var init = Future.sync(() {
      var completer = Completer<dynamic>();
      Function.apply((resolve, reject) {
        deferred["resolve"] = resolve;
        return deferred["reject"] = reject;
      },<dynamic>[completer.complete,completer.completeError]);
      return completer.future;
    });
    xtd.set_in(client,<dynamic>["state","topics",topic],<dynamic, dynamic>{
      "init":init,
      "join_ref":join_ref,
      "deferred":deferred,
      "ready":false
    });
  };
  return ((Future.sync(() => xtd.get_in(client,<dynamic>["state","init"]))) as Future<dynamic>).then((value) async { return await Function.apply((_) {
    var out = <dynamic>[];
    var chain = Future.sync(() {
      return null;
    });
    var arr_52030 = topics;
    for(var i52031 = 0; i52031 < arr_52030.length; ++i52031){
      var topic = arr_52030[i52031];
      chain = ((Future.sync(() => chain)) as Future<dynamic>).then((value) async { return await Function.apply((_) {
        phoenix.send_frame(client,topic_join_payload(impl,topic));
        return ((Future.sync(() => xtd.get_in(client,<dynamic>["state","topics",topic,"init"]))) as Future<dynamic>).then((value) async { return await Function.apply((ready) {
          out.add(ready);
          return null;
        },<dynamic>[value]); });
      },<dynamic>[value]); });
    };
    return ((Future.sync(() => chain)) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return out;
    },<dynamic>[value]); });
  },<dynamic>[value]); });
}

unsubscribe(impl, conn_id, topics) {
  return Future.sync(() {
    var client = get_realtime(impl,conn_id);
    if(null != client){
      var arr_52052 = topics;
      for(var i52053 = 0; i52053 < arr_52052.length; ++i52053){
        var topic = arr_52052[i52053];
        var entry = xtd.get_in(client,<dynamic>["state","topics",topic]);
        if(null != entry){
          phoenix.send_frame(client,topic_leave_payload(impl,topic));
          xtd.get_in(client,<dynamic>["state","topics"]).remove(topic);
        }
      };
    }
    return true;
  });
}