const xtd = require("@xtalk/lang/common-data.js")

const xtm = require("@xtalk/lang/common-math.js")

function new_derived(impl,f,arr){
  let {add_listener,create_val,get_value,set_value} = impl;
  let thunk_fn = function (){
    let vals = arr.map(get_value);
    return f.apply(null,vals);
  };
  let derived = create_val(thunk_fn());
  let listener_fn = function (v){
    let nval = thunk_fn();
    if(nval != get_value(derived)){
      set_value(derived,nval);
    }
  };
  for(let v of arr){
    add_listener(v,listener_fn);
  };
  return derived;
}

function listen_single(impl,ref,ind,f){
  if(null == f){
    f = (function (_){
      return {};
    });
  }
  let {add_listener,get_value,set_props} = impl;
  let trigger_fn = function (_){
    let props = f(get_value(ind));
    if((null != ref) && (null != ref["current"])){
      set_props(ref["current"],props);
    }
    return props;
  };
  add_listener(ind,trigger_fn);
  return trigger_fn(null);
}

function listen_array(impl,ref,arr,f){
  if(null == f){
    f = (function (_){
      return {};
    });
  }
  let {add_listener,get_value,set_props} = impl;
  let trigger_fn = function (_){
    let vals = arr.map(get_value);
    let props = f.apply(null,vals);
    if((null != ref) && (null != ref["current"])){
      set_props(ref["current"],props);
    }
    return props;
  };
  for(let ind of arr){
    add_listener(ind,trigger_fn);
  };
  return trigger_fn(null);
}

function get_map_paths_inner(impl,m,parent,all){
  if(null == parent){
    parent = [];
  }
  if(null == all){
    all = [];
  }
  let {is_animated} = impl;
  for(let [k,x] of Object.entries(m)){
    if(is_animated(x)){
      all.push([[...parent],k,x]);
    }
    else if((null != x) && ("object" == (typeof x)) && !Array.isArray(x)){
      all.push([[...parent],k,false]);
      let nparent = [...parent];
      nparent.push(k);
      get_map_paths_inner(impl,x,nparent,all);
    }
  };
  return all;
}

function get_map_paths(impl,m){
  return get_map_paths_inner(impl,m,[],[]);
}

function get_map_input(impl,paths){
  let {get_value} = impl;
  let out = {};
  for(let e of paths){
    let [path,key,v] = e;
    let val = null;
    if((null != v) && (false != v)){
      val = get_value(v);
    }
    if(false == v){
      val = {};
    }
    let entry = {};
    if((null == path) || (0 == path.length)){
      entry[key] = val;
    }
    else{
      let leaf = {};
      leaf[key] = val;
      xtd.set_in(entry,path,leaf);
    }
    xtd.obj_assign_nested(out,entry);
  };
  return out;
}

function listen_map(impl,ref,m,f){
  if(null == f){
    f = (function (_){
      return {};
    });
  }
  let {add_listener,set_props} = impl;
  let paths = get_map_paths(impl,m);
  let keep_indicator = function (entry){
    let indicator = entry[entry.length + -1];
    if((null != indicator) && (false != indicator)){
      return indicator;
    }
  };
  let animated = xtd.arr_keep(paths,keep_indicator);
  let trigger_fn = function (_){
    let input = get_map_input(impl,paths);
    let props = f(input);
    if((null != ref) && (null != ref["current"])){
      set_props(ref["current"],props);
    }
    return props;
  };
  for(let ind of animated){
    add_listener(ind,trigger_fn);
  };
  return trigger_fn(null);
}

function listen_transformations(impl,ref,indicators,transformations,get_chord){
  let {is_animated,set_props} = impl;
  if(null == transformations){
    return {};
  }
  else if("function" == (typeof transformations)){
    if(null == indicators){
      return;
    }
    else if(is_animated(indicators)){
      return listen_single(impl,ref,indicators,function (v){
        return transformations(v,get_chord());
      });
    }
    else{
      return listen_map(impl,ref,indicators,function (m){
        return transformations(m,get_chord());
      });
    }
  }
  else{
    let out = {};
    for(let [key,subtransformations] of Object.entries(transformations)){
      let subindicators = indicators[key];
      let subout = listen_transformations(impl,ref,subindicators,subtransformations,get_chord);
      out[key] = subout;
    };
    return out;
  }
}

function new_progressing(){
  return {"running":false,"animation":null,"queued":[]};
}

function run_with_cancel(impl,animate_fn,progressing,progress_fn){
  let {animation,running} = progressing;
  let {stop_transition} = impl;
  if(running && (null != animation)){
    stop_transition(animation);
    Object.assign(progressing,{"running":false});
    progressing["animation"] = null;
  }
  let finish_fn = function (finished){
    Object.assign(progressing,{"running":false});
    progressing["animation"] = null;
    if(null != progress_fn){
      progress_fn({"status":"stopped","finished":finished});
    }
  };
  let anim = animate_fn(finish_fn);
  Object.assign(progressing,{"animation":anim});
  if(null != progress_fn){
    progress_fn({"status":"running"});
  }
  return progressing;
}

function animate_chained_cleanup(impl,progressing,progress_fn){
  Object.assign(progressing,{"running":false,"queued":[]});
  progressing["animation"] = null;
  if(null != progress_fn){
    progress_fn({"status":"cleanup"});
  }
  return progressing;
}

function animate_chained_one(impl,progressing,progress_fn){
  let {queued} = progressing;
  if(0 == queued.length){
    return animate_chained_cleanup(impl,progressing,progress_fn);
  }
  let queued_fn = queued[0];
  if(null == queued_fn){
    return animate_chained_cleanup(impl,progressing,progress_fn);
  }
  let anim = queued_fn(function (){
    return animate_chained_cleanup(impl,progressing,progress_fn);
  });
  Object.assign(progressing,{"running":true,"animation":anim});
  if(null != progress_fn){
    progress_fn({"status":"running"});
  }
  return progressing;
}

function animate_chained_all(impl,progressing,progress_fn){
  let {queued} = progressing;
  if(0 == queued.length){
    return animate_chained_cleanup(impl,progressing,progress_fn);
  }
  let queued_fn = queued[0];
  queued.shift();
  if(null != queued_fn){
    let anim = queued_fn(function (res){
      return animate_chained_all(impl,progressing,progress_fn);
    });
    if(null != anim){
      Object.assign(progressing,{"running":true,"animation":anim});
      if(null != progress_fn){
        progress_fn({"status":"running"});
      }
    }
  }
  return progressing;
}

function run_with_chained(impl,type,animate_fn,progressing,progress_fn){
  let callback_fn = function (){
    return animate_chained_all(impl,progressing,progress_fn);
  };
  if(type == "chained-one"){
    callback_fn = (function (){
      return animate_chained_one(impl,progressing,progress_fn);
    });
  }
  let {queued,running} = progressing;
  if(!running){
    let anim = animate_fn(callback_fn);
    if(null != anim){
      Object.assign(progressing,{"running":true,"animation":anim});
    }
  }
  else if((type == "chained-one") && (0 < queued.length) && (null != queued[0])){
    return progressing;
  }
  else{
    queued.push(animate_fn);
  }
  return progressing;
}

function run_with(impl,type,animate_fn,progressing,progress_fn){
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

function make_binary_transitions(impl,initial,tparams){
  if(null == tparams){
    tparams = {};
  }
  let {create_transition,create_val} = impl;
  let initial_value = 0;
  if(initial){
    initial_value = 1;
  }
  let indicator = create_val(initial_value);
  let identity_fn = function (x){
    return x;
  };
  let zero_fn = create_transition(indicator,tparams,[1,0],identity_fn);
  let one_fn = create_transition(indicator,tparams,[0,1],identity_fn);
  let {check} = tparams;
  let check_fn = check;
  if(null == check_fn){
    check_fn = identity_fn;
  }
  return {
    "indicator":indicator,
    "zero_fn":zero_fn,
    "one_fn":one_fn,
    "check_fn":check_fn
  };
}

function make_binary_indicator(impl,initial,tparams,type,progressing,progress_fn){
  if(null == tparams){
    tparams = {};
  }
  let transitions = make_binary_transitions(impl,initial,tparams);
  let {check_fn,indicator,one_fn,zero_fn} = transitions;
  let trigger_fn = function (flag){
    if(null != progress_fn){
      progress_fn({"status":"started"});
    }
    if(check_fn(flag)){
      return run_with(impl,type,one_fn,progressing,progress_fn);
    }
    else{
      return run_with(impl,type,zero_fn,progressing,progress_fn);
    }
  };
  return {"indicator":indicator,"trigger_fn":trigger_fn};
}

function make_linear_indicator_inner(impl,initial,get_prev,set_prev,tparams,type,progressing,progress_fn,check_fn){
  let {create_transition,create_val} = impl;
  let indicator = create_val(initial);
  let trigger_fn = function (value){
    let should_run = true;
    if(null != check_fn){
      let check_out = check_fn(value);
      should_run = ((null != check_out) && (false != check_out));
    }
    if(should_run){
      let t_fn = create_transition(indicator,tparams,[get_prev(),value],function (x){
        return x;
      });
      if(null != progress_fn){
        progress_fn({"status":"started"});
      }
      let out = run_with(impl,type,t_fn,progressing,progress_fn);
      set_prev(value);
      return out;
    }
  };
  return {"indicator":indicator,"trigger_fn":trigger_fn};
}

function make_linear_indicator(impl,initial,get_prev,set_prev,tparams,type,progressing,progress_fn){
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

function make_circular_indicator_inner(impl,initial,get_prev,set_prev,tparams,type,modulo,progressing,progress_fn,check_fn){
  let {create_transition,create_val} = impl;
  let indicator = create_val(initial);
  let trigger_fn = function (value){
    let should_run = true;
    if(null != check_fn){
      let check_out = check_fn(value);
      should_run = ((null != check_out) && (false != check_out));
    }
    if(should_run){
      let pval = get_prev();
      let modulo_val = modulo;
      if(null == modulo_val){
        modulo_val = 360;
      }
      let offset = xtm.mod_offset(pval,value,modulo_val);
      let nval = pval + offset;
      let t_fn = create_transition(indicator,tparams,[pval,nval],function (x){
        return x;
      });
      if(null != progress_fn){
        progress_fn({"status":"started"});
      }
      let out = run_with(impl,type,t_fn,progressing,progress_fn);
      set_prev(value);
      return out;
    }
  };
  return {"indicator":indicator,"trigger_fn":trigger_fn};
}

function make_circular_indicator(impl,initial,get_prev,set_prev,tparams,type,modulo,progressing,progress_fn){
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

module.exports = {
  ["new_derived"]:new_derived,
  ["listen_single"]:listen_single,
  ["listen_array"]:listen_array,
  ["get_map_paths_inner"]:get_map_paths_inner,
  ["get_map_paths"]:get_map_paths,
  ["get_map_input"]:get_map_input,
  ["listen_map"]:listen_map,
  ["listen_transformations"]:listen_transformations,
  ["new_progressing"]:new_progressing,
  ["run_with_cancel"]:run_with_cancel,
  ["animate_chained_cleanup"]:animate_chained_cleanup,
  ["animate_chained_one"]:animate_chained_one,
  ["animate_chained_all"]:animate_chained_all,
  ["run_with_chained"]:run_with_chained,
  ["run_with"]:run_with,
  ["make_binary_transitions"]:make_binary_transitions,
  ["make_binary_indicator"]:make_binary_indicator,
  ["make_linear_indicator_inner"]:make_linear_indicator_inner,
  ["make_linear_indicator"]:make_linear_indicator,
  ["make_circular_indicator_inner"]:make_circular_indicator_inner,
  ["make_circular_indicator"]:make_circular_indicator
}