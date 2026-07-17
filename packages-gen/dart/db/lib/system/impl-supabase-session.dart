import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_net/addon-supabase.dart' as addon;

import 'package:xtalk_net/http-fetch.dart' as http_fetch;

import 'package:xtalk_lang/common-string.dart' as xts;

import 'package:xtalk_net/http-util.dart' as http_util;

get_session(impl) {
  return xtd.get_in(impl,<dynamic>["state","session"]);
}

set_session(impl, session) {
  xtd.set_in(impl,<dynamic>["state","session"],session);
  return session;
}

refresh_session(impl) {
  var client = impl["client"];
  var session = xtd.get_in(impl,<dynamic>["state","session"]);
  var refresh_token = xtd.get_in(session,"refresh_token");
  if(null == refresh_token){
    return Future.sync(() {
      return null;
    });
  }
  else{
    return (() async { try { return await ((Future.sync(() => ((Future.sync(() => http_fetch.request_http(client,addon.cmd_token_refresh(
      <dynamic, dynamic>{"refresh_token":refresh_token},
      <dynamic, dynamic>{}
    )))) as Future<dynamic>).then((value) async { return await Function.apply((response) {
      return set_session(impl,http_util.get_body_data(response));
    },<dynamic>[value]); }))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
      return err;
    },<dynamic>[err])); } })();
  }
}

auto_refresh_interval(impl) {
  var expires_in = xtd.get_in(impl,<dynamic>["state","session","expires_in"]);
  if(null != expires_in){
    return ((expires_in * 1000) >= 60000) ? ((expires_in * 1000) - 60000) : 1000;
  }
  else{
    return xtd.get_in(impl,<dynamic>["opts","auto_refresh_interval"]) ?? 300000;
  }
}

auto_refresh_fn(impl, delay, refresh_id) {
  var current_id = xtd.get_in(impl,<dynamic>["state","auto_refresh","current"]);
  if(current_id == refresh_id){
    ((Future.sync(() => (() async { try { return await ((Future.sync(() => refresh_session(impl))) as Future<dynamic>); } catch (err) { return await Future.sync(() => Function.apply((err) {
      return err;
    },<dynamic>[err])); } })())) as Future<dynamic>).then((value) async { return await Function.apply((_) {
      return Future.delayed(Duration(milliseconds:  delay )).then((_) {
        return Future.sync(() {
          return Function.apply(() {
            return Function.apply(
              (auto_refresh_fn as Function),
              <dynamic>[impl,auto_refresh_interval(impl),refresh_id]
            );
          },<dynamic>[]);
        });
      });
    },<dynamic>[value]); });
    return refresh_id;
  }
  else{
    return current_id;
  }
}

auto_refresh_stop(impl) {
  var current_id = xtd.get_in(impl,<dynamic>["state","auto_refresh","current"]);
  xtd.set_in(impl,<dynamic>["state","auto_refresh","current"],null);
  return current_id;
}

auto_refresh_start(impl) {
  var current_id = xtd.get_in(impl,<dynamic>["state","auto_refresh","current"]);
  if(null != current_id){
    return current_id;
  }
  var refresh_id = xts.str_rand(8);
  xtd.set_in(impl,<dynamic>["state","auto_refresh","current"],refresh_id);
  return Function.apply(
    (auto_refresh_fn as Function),
    <dynamic>[impl,auto_refresh_interval(impl),refresh_id]
  );
}