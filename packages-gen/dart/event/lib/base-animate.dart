import 'package:xtalk_lang/common-data.dart' as xtd;

import 'package:xtalk_lang/common-math.dart' as xtm;

new_derived(impl, f, arr) {
  var add_listener = impl["add_listener"];
  var create_val = impl["create_val"];
  var get_value = impl["get_value"];
  var set_value = impl["set_value"];
  var thunk_fn = () {
    var vals = xtd.arr_map(arr,get_value);
    return Function.apply(f,vals);
  };
  var derived = create_val(Function.apply((thunk_fn as Function),<dynamic>[]));
  var listener_fn = (v) {
    var nval = Function.apply((thunk_fn as Function),<dynamic>[]);
    if(nval != get_value(derived)){
      set_value(derived,nval);
    }
  };
  var arr_41487 = arr;
  for(var i41488 = 0; i41488 < arr_41487.length; ++i41488){
    var v = arr_41487[i41488];
    add_listener(v,listener_fn);
  };
  return derived;
}

listen_single(impl, ref, ind, f) {
  if(null == f){
    f = ((_) {
      return <dynamic, dynamic>{};
    });
  }
  var add_listener = impl["add_listener"];
  var get_value = impl["get_value"];
  var set_props = impl["set_props"];
  var trigger_fn = (_) {
    var props = f(get_value(ind));
    if((null != ref) && ref.containsKey("current")){
      set_props(ref["current"],props);
    }
    return props;
  };
  add_listener(ind,trigger_fn);
  return Function.apply((trigger_fn as Function),<dynamic>[null]);
}

listen_array(impl, ref, arr, f) {
  if(null == f){
    f = ((_) {
      return <dynamic, dynamic>{};
    });
  }
  var add_listener = impl["add_listener"];
  var get_value = impl["get_value"];
  var set_props = impl["set_props"];
  var trigger_fn = (_) {
    var vals = xtd.arr_map(arr,get_value);
    var props = Function.apply(f,vals);
    if((null != ref) && ref.containsKey("current")){
      set_props(ref["current"],props);
    }
    return props;
  };
  var arr_41509 = arr;
  for(var i41510 = 0; i41510 < arr_41509.length; ++i41510){
    var ind = arr_41509[i41510];
    add_listener(ind,trigger_fn);
  };
  return Function.apply((trigger_fn as Function),<dynamic>[null]);
}

get_map_paths_inner(impl, m, parent, all) {
  if(null == parent){
    parent = <dynamic>[];
  }
  if(null == all){
    all = <dynamic>[];
  }
  var is_animated = impl["is_animated"];
  for(var entry_41531 in m.entries){
    var k = entry_41531.key;
    var x = entry_41531.value;
    if((() {
      var dart_truthy__41486 = is_animated(x);
      return (null != dart_truthy__41486) && (false != dart_truthy__41486);
    })()){
      all.add(<dynamic>[<dynamic>[...parent],k,x]);
    }
    else if(("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")){
      all.add(<dynamic>[<dynamic>[...parent],k,false]);
      var nparent = <dynamic>[...parent];
      nparent.add(k);
      get_map_paths_inner(impl,x,nparent,all);
    }
  };
  return all;
}

get_map_paths(impl, m) {
  return get_map_paths_inner(impl,m,<dynamic>[],<dynamic>[]);
}

get_map_input(impl, paths) {
  var get_value = impl["get_value"];
  var out = <dynamic, dynamic>{};
  var arr_41532 = paths;
  for(var i41533 = 0; i41533 < arr_41532.length; ++i41533){
    var e = arr_41532[i41533];
    var value_41554 = e;
    var path = value_41554[0];
    var key = value_41554[1];
    var v = value_41554[2];
    var val = null;
    if((null != v) && (false != v)){
      val = get_value(v);
    }
    if(false == v){
      val = <dynamic, dynamic>{};
    }
    var entry = <dynamic, dynamic>{};
    if((null == path) || (0 == path.length)){
      entry[key] = val;
    }
    else{
      var leaf = <dynamic, dynamic>{};
      leaf[key] = val;
      xtd.set_in(entry,path,leaf);
    }
    xtd.obj_assign_nested(out,entry);
  };
  return out;
}

listen_map(impl, ref, m, f) {
  if(null == f){
    f = ((_) {
      return <dynamic, dynamic>{};
    });
  }
  var add_listener = impl["add_listener"];
  var set_props = impl["set_props"];
  var paths = get_map_paths(impl,m);
  var keep_indicator = (entry) {
    var indicator = entry[entry.length + -1];
    if((null != indicator) && (false != indicator)){
      return indicator;
    }
  };
  var animated = xtd.arr_keep(paths,keep_indicator);
  var trigger_fn = (_) {
    var input = get_map_input(impl,paths);
    var props = f(input);
    if((null != ref) && ref.containsKey("current")){
      set_props(ref["current"],props);
    }
    return props;
  };
  var arr_41555 = animated;
  for(var i41556 = 0; i41556 < arr_41555.length; ++i41556){
    var ind = arr_41555[i41556];
    add_listener(ind,trigger_fn);
  };
  return Function.apply((trigger_fn as Function),<dynamic>[null]);
}

listen_transformations(impl, ref, indicators, transformations, get_chord) {
  var is_animated = impl["is_animated"];
  var set_props = impl["set_props"];
  if(null == transformations){
    return <dynamic, dynamic>{};
  }
  else if((transformations.runtimeType).toString().contains("Function") || (transformations.runtimeType).toString().contains("=>") || (transformations).toString().startsWith("Closure")){
    if(null == indicators){
      return;
    }
    else if((() {
      var dart_truthy__41485 = is_animated(indicators);
      return (null != dart_truthy__41485) && (false != dart_truthy__41485);
    })()){
      return listen_single(impl,ref,indicators,(v) {
        return transformations(v,get_chord());
      });
    }
    else{
      return listen_map(impl,ref,indicators,(m) {
        return transformations(m,get_chord());
      });
    }
  }
  else{
    var out = <dynamic, dynamic>{};
    for(var entry_41577 in transformations.entries){
      var key = entry_41577.key;
      var subtransformations = entry_41577.value;
      var subindicators = indicators[key];
      var subout = listen_transformations(impl,ref,subindicators,subtransformations,get_chord);
      out[key] = subout;
    };
    return out;
  }
}

new_progressing() {
  return <dynamic, dynamic>{"running":false,"animation":null,"queued":<dynamic>[]};
}

run_with_cancel(impl, animate_fn, progressing, progress_fn) {
  var animation = progressing["animation"];
  var running = progressing["running"];
  var stop_transition = impl["stop_transition"];
  if(((null != running) && (false != running)) && (null != animation)){
    stop_transition(animation);
    xtd.obj_assign(progressing,<dynamic, dynamic>{"running":false});
    progressing["animation"] = null;
  }
  var finish_fn = (finished) {
    xtd.obj_assign(progressing,<dynamic, dynamic>{"running":false});
    progressing["animation"] = null;
    if(null != progress_fn){
      Function.apply(
        (progress_fn as Function),
        <dynamic>[<dynamic, dynamic>{"status":"stopped","finished":finished}]
      );
    }
  };
  var anim = Function.apply((animate_fn as Function),<dynamic>[finish_fn]);
  xtd.obj_assign(progressing,<dynamic, dynamic>{"animation":anim});
  if(null != progress_fn){
    Function.apply(
      (progress_fn as Function),
      <dynamic>[<dynamic, dynamic>{"status":"running"}]
    );
  }
  return progressing;
}

animate_chained_cleanup(impl, progressing, progress_fn) {
  xtd.obj_assign(
    progressing,
    <dynamic, dynamic>{"running":false,"queued":<dynamic>[]}
  );
  progressing["animation"] = null;
  if(null != progress_fn){
    Function.apply(
      (progress_fn as Function),
      <dynamic>[<dynamic, dynamic>{"status":"cleanup"}]
    );
  }
  return progressing;
}

animate_chained_one(impl, progressing, progress_fn) {
  var queued = progressing["queued"];
  if(0 == queued.length){
    return animate_chained_cleanup(impl,progressing,progress_fn);
  }
  var queued_fn = queued[0];
  if(null == queued_fn){
    return animate_chained_cleanup(impl,progressing,progress_fn);
  }
  var anim = Function.apply((queued_fn as Function),<dynamic>[
    () {
      return animate_chained_cleanup(impl,progressing,progress_fn);
    }
  ]);
  xtd.obj_assign(
    progressing,
    <dynamic, dynamic>{"running":true,"animation":anim}
  );
  if(null != progress_fn){
    Function.apply(
      (progress_fn as Function),
      <dynamic>[<dynamic, dynamic>{"status":"running"}]
    );
  }
  return progressing;
}

animate_chained_all(impl, progressing, progress_fn) {
  var queued = progressing["queued"];
  if(0 == queued.length){
    return animate_chained_cleanup(impl,progressing,progress_fn);
  }
  var queued_fn = queued[0];
  queued.removeAt(0);
  if(null != queued_fn){
    var anim = Function.apply((queued_fn as Function),<dynamic>[
      (res) {
          return animate_chained_all(impl,progressing,progress_fn);
        }
    ]);
    if(null != anim){
      xtd.obj_assign(
        progressing,
        <dynamic, dynamic>{"running":true,"animation":anim}
      );
      if(null != progress_fn){
        Function.apply(
          (progress_fn as Function),
          <dynamic>[<dynamic, dynamic>{"status":"running"}]
        );
      }
    }
  }
  return progressing;
}

run_with_chained(impl, type, animate_fn, progressing, progress_fn) {
  var callback_fn = () {
    return animate_chained_all(impl,progressing,progress_fn);
  };
  if(type == "chained-one"){
    callback_fn = (() {
      return animate_chained_one(impl,progressing,progress_fn);
    });
  }
  var queued = progressing["queued"];
  var running = progressing["running"];
  if(!((null != running) && (false != running))){
    var anim = Function.apply((animate_fn as Function),<dynamic>[callback_fn]);
    if(null != anim){
      xtd.obj_assign(
        progressing,
        <dynamic, dynamic>{"running":true,"animation":anim}
      );
    }
  }
  else if((type == "chained-one") && (0 < queued.length) && (null != queued[0])){
    return progressing;
  }
  else{
    queued.add(animate_fn);
  }
  return progressing;
}

run_with(impl, type, animate_fn, progressing, progress_fn) {
  if(type == "cancel"){
    return run_with_cancel(impl,animate_fn,progressing,progress_fn);
  }
  else if((type == "chained-one") || (type == "chained-all")){
    return run_with_chained(impl,type,animate_fn,progressing,progress_fn);
  }
  else{
    throw "Not a valid type: " + type;
  }
}

make_binary_transitions(impl, initial, tparams) {
  if(null == tparams){
    tparams = <dynamic, dynamic>{};
  }
  var create_transition = impl["create_transition"];
  var create_val = impl["create_val"];
  var initial_value = 0;
  if((null != initial) && (false != initial)){
    initial_value = 1;
  }
  var indicator = create_val(initial_value);
  var identity_fn = (x) {
    return x;
  };
  var zero_fn = create_transition(indicator,tparams,<dynamic>[1,0],identity_fn);
  var one_fn = create_transition(indicator,tparams,<dynamic>[0,1],identity_fn);
  var check = tparams["check"];
  var check_fn = check;
  if(null == check_fn){
    check_fn = identity_fn;
  }
  return <dynamic, dynamic>{
    "indicator":indicator,
    "zero_fn":zero_fn,
    "one_fn":one_fn,
    "check_fn":check_fn
  };
}

make_binary_indicator(impl, initial, tparams, type, progressing, progress_fn) {
  if(null == tparams){
    tparams = <dynamic, dynamic>{};
  }
  var transitions = make_binary_transitions(impl,initial,tparams);
  var check_fn = transitions["check_fn"];
  var indicator = transitions["indicator"];
  var one_fn = transitions["one_fn"];
  var zero_fn = transitions["zero_fn"];
  var trigger_fn = (flag) {
    if(null != progress_fn){
      Function.apply(
        (progress_fn as Function),
        <dynamic>[<dynamic, dynamic>{"status":"started"}]
      );
    }
    if((() {
      var dart_truthy__41484 = Function.apply((check_fn as Function),<dynamic>[flag]);
      return (null != dart_truthy__41484) && (false != dart_truthy__41484);
    })()){
      return run_with(impl,type,one_fn,progressing,progress_fn);
    }
    else{
      return run_with(impl,type,zero_fn,progressing,progress_fn);
    }
  };
  return <dynamic, dynamic>{"indicator":indicator,"trigger_fn":trigger_fn};
}

make_linear_indicator_inner(impl, initial, get_prev, set_prev, tparams, type, progressing, progress_fn, check_fn) {
  var create_transition = impl["create_transition"];
  var create_val = impl["create_val"];
  var indicator = create_val(initial);
  var trigger_fn = (value) {
    var should_run = true;
    if(null != check_fn){
      var check_out = Function.apply((check_fn as Function),<dynamic>[value]);
      should_run = ((null != check_out) && (false != check_out));
    }
    if((null != should_run) && (false != should_run)){
      var t_fn = create_transition(indicator,tparams,<dynamic>[get_prev(),value],(x) {
        return x;
      });
      if(null != progress_fn){
        Function.apply(
          (progress_fn as Function),
          <dynamic>[<dynamic, dynamic>{"status":"started"}]
        );
      }
      var out = run_with(impl,type,t_fn,progressing,progress_fn);
      set_prev(value);
      return out;
    }
  };
  return <dynamic, dynamic>{"indicator":indicator,"trigger_fn":trigger_fn};
}

make_linear_indicator(impl, initial, get_prev, set_prev, tparams, type, progressing, progress_fn) {
  return make_linear_indicator_inner(
    impl,
    initial,
    get_prev,
    set_prev,
    tparams,
    type,
    progressing,
    progress_fn,
    null
  );
}

make_circular_indicator_inner(impl, initial, get_prev, set_prev, tparams, type, modulo, progressing, progress_fn, check_fn) {
  var create_transition = impl["create_transition"];
  var create_val = impl["create_val"];
  var indicator = create_val(initial);
  var trigger_fn = (value) {
    var should_run = true;
    if(null != check_fn){
      var check_out = Function.apply((check_fn as Function),<dynamic>[value]);
      should_run = ((null != check_out) && (false != check_out));
    }
    if((null != should_run) && (false != should_run)){
      var pval = get_prev();
      var modulo_val = modulo;
      if(null == modulo_val){
        modulo_val = 360;
      }
      var offset = xtm.mod_offset(pval,value,modulo_val);
      var nval = pval + offset;
      var t_fn = create_transition(indicator,tparams,<dynamic>[pval,nval],(x) {
        return x;
      });
      if(null != progress_fn){
        Function.apply(
          (progress_fn as Function),
          <dynamic>[<dynamic, dynamic>{"status":"started"}]
        );
      }
      var out = run_with(impl,type,t_fn,progressing,progress_fn);
      set_prev(value);
      return out;
    }
  };
  return <dynamic, dynamic>{"indicator":indicator,"trigger_fn":trigger_fn};
}

make_circular_indicator(impl, initial, get_prev, set_prev, tparams, type, modulo, progressing, progress_fn) {
  return make_circular_indicator_inner(
    impl,
    initial,
    get_prev,
    set_prev,
    tparams,
    type,
    modulo,
    progressing,
    progress_fn,
    null
  );
}