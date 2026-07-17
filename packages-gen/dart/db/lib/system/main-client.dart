import 'xtalk_db/net/http-fetch.dart' as dart_fetch;

import 'xtalk_db/net/ws-native.dart' as dart_ws;

import 'xtalk_db/net/conn-postgres.dart' as dart_postgres;

import 'package:xtalk_net/addon-supabase.dart' as addon;

import 'xtalk_db/net/conn-sqlite.dart' as dart_sqlite;

create_client(type, defaults) {
  if(type == "sqlite"){
    return dart_sqlite.create(defaults);
  }
  else if(type == "postgres"){
    return dart_postgres.create(defaults);
  }
  else if(type == "supabase"){
    var client = dart_fetch.create(defaults,addon.middleware_supabase());
    client["create_ws_client"] = ((ws_defaults) {
      return dart_ws.create(
        xt.lang.common_data.obj_assign(ws_defaults,<dynamic, dynamic>{"background":true})
      );
    });
    return client;
  }
  else{
    return null;
  }
}