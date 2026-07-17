const xtd = require("@xtalk/lang/common-data.js")

const xtst = require("@xtalk/lang/common-sort-topo.js")

function new_task(id,deps,args,opts){
  return Object.assign({"::":"loader.task","id":id,"deps":deps,"args":args},opts);
}

function task_load(task){
  let {args,assert_fn,check_fn,get_fn,id,load_fn,load_no_check} = task;
  if("function" == (typeof assert_fn)){
    if(!assert_fn()){
      throw "Assertion Failed - " + id;
    }
  }
  if(true != load_no_check){
    let curr = ("function" == (typeof get_fn)) ? get_fn() : null;
    let check = ("function" == (typeof check_fn)) ? check_fn(curr) : null;
    if(true == check){
      return Promise.resolve().then(function (){
        return curr;
      });
    }
  }
  if("function" == (typeof args)){
    args = args();
  }
  args = ((null == args) ? [] : args);
  return Promise.resolve().then(function (){
    return load_fn.apply(null,args);
  });
}

function task_unload(task){
  let {check_fn,get_fn,unload_fn,unload_no_check} = task;
  if(true != unload_no_check){
    let curr = ("function" == (typeof get_fn)) ? get_fn() : null;
    let check = ("function" == (typeof check_fn)) ? check_fn(curr) : null;
    if(true != check){
      return Promise.resolve().then(function (){
        return false;
      });
    }
  }
  return Promise.resolve().then(function (){
    return unload_fn();
  }).then(function (_){
    return true;
  });
}

function unload_tasks_loop(tasks,completed,ids,out,hook_fn){
  if(0 == ids.length){
    return Promise.resolve().then(function (){
      return out;
    });
  }
  let id = ids[0];
  let rest = ids.slice(1,ids.length);
  if(true != completed[id]){
    return unload_tasks_loop(tasks,completed,rest,out,hook_fn);
  }
  delete(completed[id]);
  let task = tasks[id];
  return task_unload(task).then(function (unloaded){
    if(null != hook_fn){
      hook_fn(id,unloaded);
    }
    out.push([id,unloaded]);
    return unload_tasks_loop(tasks,completed,rest,out,hook_fn);
  });
}

function new_loader_blank(){
  return {
    "::":"loader",
    "completed":{},
    "loading":{},
    "errored":null,
    "order":[],
    "tasks":{}
  };
}

function add_tasks(loader,tasks){
  let prev = loader["tasks"];
  let all = xtd.arr_assign(Object.values(prev),tasks);
  let deps = all.map(function (e){
    return [e["id"],e["deps"]];
  });
  return Object.assign(loader,{
    "order":xtst.sort_topo(deps),
    "tasks":xtd.arr_juxt(all,function (e){
        return e["id"];
      },xtd.clone_nested)
  });
}

function new_loader(tasks){
  return add_tasks(new_loader_blank(),tasks);
}

function list_loading(loader){
  return Object.keys(loader["loading"]);
}

function list_completed(loader){
  return Object.keys(loader["completed"]);
}

function list_incomplete(loader){
  let {completed,order,tasks} = loader;
  let out = [];
  for(let id of order){
    if((null != tasks[id]) && (true != completed[id])){
      out.push(id);
    }
  };
  return out;
}

function list_waiting(loader){
  let {completed,loading,order,tasks} = loader;
  let out = [];
  for(let id of order){
    let task = tasks[id];
    if((null != task) && (true != loading[id]) && (true != completed[id]) && task["deps"].every(function (dep_id){
      return true == completed[dep_id];
    })){
      out.push(id);
    }
  };
  return out;
}

function load_tasks_single(loader,id,hook_fn,complete_fn,loop_fn){
  let {completed,loading,tasks} = loader;
  let task = tasks[id];
  loading[id] = true;
  return task_load(task).catch(function (err){
    delete(loading[id]);
    loader["errored"] = id;
    if(null != hook_fn){
      hook_fn(id,false);
    }
    if(null != complete_fn){
      complete_fn(err);
    }
    throw err;
  }).then(function (res){
    delete(loading[id]);
    completed[id] = true;
    if(null != hook_fn){
      hook_fn(id,true);
    }
    return (null != loop_fn) ? loop_fn(loader,hook_fn,complete_fn) : res;
  });
}

function load_tasks(loader,hook_fn,complete_fn){
  let {errored,order} = loader;
  if(null != errored){
    let reject_fn = function (){
      throw "ERR - Task Errored - " + errored;
    };
    return Promise.resolve().then(reject_fn);
  }
  let waiting = list_waiting(loader);
  if(0 < waiting.length){
    let waiting_lu = xtd.arr_juxt(waiting,function (id){
      return id;
    },function (id){
      return true;
    });
    let next_id =     (xtd.arr_keep(order,function (id){
          if(true == waiting_lu[id]){
            return id;
          }
        }))[0];
    return load_tasks_single(loader,next_id,hook_fn,complete_fn,load_tasks);
  }
  let incomplete = list_incomplete(loader);
  if(0 == incomplete.length){
    if(null != complete_fn){
      complete_fn(true);
    }
    return Promise.resolve().then(function (){
      return loader;
    });
  }
  return Promise.resolve().then(function (){
    return loader;
  });
}

function unload_tasks(loader,hook_fn){
  let {completed,order,tasks} = loader;
  let rorder = order.slice().reverse();
  return unload_tasks_loop(tasks,completed,rorder,[],hook_fn);
}

module.exports = {
  ["new_task"]:new_task,
  ["task_load"]:task_load,
  ["task_unload"]:task_unload,
  ["unload_tasks_loop"]:unload_tasks_loop,
  ["new_loader_blank"]:new_loader_blank,
  ["add_tasks"]:add_tasks,
  ["new_loader"]:new_loader,
  ["list_loading"]:list_loading,
  ["list_completed"]:list_completed,
  ["list_incomplete"]:list_incomplete,
  ["list_waiting"]:list_waiting,
  ["load_tasks_single"]:load_tasks_single,
  ["load_tasks"]:load_tasks,
  ["unload_tasks"]:unload_tasks
}