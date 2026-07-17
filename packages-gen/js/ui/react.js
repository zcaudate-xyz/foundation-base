const React = require("react")

const ReactDOM = require("react-dom/client")

const xtt = require("@xtalk/lang/common-tree.js")

const xtd = require("@xtalk/lang/common-data.js")

const k = require("@xtalk/lang/common-lib.js")

const math = require("@xtalk/lang/common-math.js")

const str = require("@xtalk/lang/common-string.js")

function getDOMRoot(domNode){
  let internalKey = xtd.arr_find(xtd.obj_keys(domNode),function (key){
    return key.startsWith("__reactContainer$");
  });
  if(internalKey > 0){
    return domNode[internalKey];
  }
  return null;
}

function renderDOMRoot(id,Component){
  let rootElement = document.getElementById(id);
  let root = getDOMRoot(rootElement);
  if(null == root){
    root = ReactDOM.createRoot(rootElement);
  }
  root.render((
    <Component/>));
  return true;
}

function useStateFor(controls,key){
  let setterKey = "set" + str.capitalize(key);
  return [controls[key],controls[setterKey]];
}

class Try extends React.Component{
  constructor(props) {
    super(props);
    this.state = {"hasError":false,"error":null};
  }
  static getDerivedStateFromError(error){
    return {error,"hasError":true};
  }
  render() {
    if(this.state.hasError){
      return this.props.fallback;
    }
    else{
      return this.props.children;
    }
  }
}

function id(n){
  // eslint-disable-next-line react-hooks/rules-of-hooks
  return React.useRef(Math.random().toString(36).substr(2,n || 6)).current;
}

function useStep(f){
  let [done,setDone] = React.useState();
  React.useEffect(function (){
    if(!done){
      return f(setDone);
    }
  });
  return [done,setDone];
}

function makeLazy(component){
  if("function" == (typeof component)){
    return component;
  }
  else{
    return React.lazy(function (){
      return component;
    });
  }
}

function useLazy(component){
  if("function" == (typeof component)){
    return component;
  }
  else{
    // eslint-disable-next-line react-hooks/rules-of-hooks
    return React.useRef(React.lazy(function (){
      return component;
    })).current;
  }
}

function useRefresh(){
  let [flag,setFlag] = React.useState(true);
  let refresh = function (){
    return setFlag(!flag);
  };
  return refresh;
}

function useGetCount(n){
  let counterRef = React.useRef(n || 0);
  React.useEffect(function (){
    counterRef.current = (1 + counterRef.current);
  });
  let getCount = React.useRef(function (){
    return counterRef.current;
  }).current;
  return getCount;
}

function useFollowRef(value,f){
  f = (f || k.identity);
  // eslint-disable-next-line react-hooks/rules-of-hooks
  let valueRef = React.useRef(f(value));
  // eslint-disable-next-line react-hooks/rules-of-hooks
  React.useEffect(function (){
    valueRef.current = f(value);
  },[value]);
  return valueRef;
}

function useIsMounted(){
  let mountedRef = React.useRef(true);
  let isMounted = React.useRef(function (){
    return mountedRef.current;
  }).current;
  React.useEffect(function (){
    return function (){
      mountedRef.current = false;
    };
  },[]);
  return isMounted;
}

function useIsMountedWrap(){
  let isMounted = useIsMounted();
  return function (f){
    return function (...args){
      if(isMounted()){
        f(...args);
      }
    };
  };
}

function useMountedCallback(cb){
  let cbRef = useFollowRef(cb);
  React.useEffect(function (){
    if(cbRef.current){
      cbRef.current(true);
    }
    return function (){
      if(cbRef.current){
        cbRef.current(false);
      }
    };
  },[]);
}

function useFollowDelayed(value,delay,isMounted){
  if(0 == delay){
    return [value,k.noop];
  }
  // eslint-disable-next-line react-hooks/rules-of-hooks
  let [delayed,setDelayed] = React.useState(value);
  // eslint-disable-next-line react-hooks/rules-of-hooks
  React.useEffect(function (){
    new Promise(function (resolve,reject){
      setTimeout(function (){
        new Promise(function (inner_resolve){
          inner_resolve((function (){
            if(isMounted()){
              setDelayed(value);
            }
          })());
        }).then(function (value){
          resolve(value);
        }).catch(function (err){
          reject(err);
        });
      },delay);
    });
  },[value]);
  return [delayed,setDelayed];
}

function useStablized(input,isStabilized){
  let [output,setOutput] = React.useState(input);
  React.useEffect(function (){
    if(isStabilized && (null != input) && xtt.eq_nested(input,output)){
      setOutput(input);
    }
  },[input]);
  return isStabilized ? output : input;
}

function runIntervalStop(intervalRef){
  let interval = intervalRef.current;
  if(null != interval){
    clearInterval(interval);
    intervalRef.current = null;
  }
  return interval;
}

function runIntervalStart(fRef,msRef,intervalRef){
  let prev = runIntervalStop(intervalRef);
  if(null != msRef.current){
    let curr = setInterval(function (){
      fRef.current();
    },msRef.current);
    intervalRef.current = curr;
    return [prev,curr];
  }
  return [prev];
}

function useInterval(f,ms){
  let fRef = useFollowRef(f);
  let msRef = useFollowRef(ms);
  let intervalRef = React.useRef(null);
  let stopInterval = React.useRef(function (){
    return runIntervalStop(intervalRef);
  }).current;
  let startInterval = React.useRef(function (){
    return runIntervalStart(fRef,msRef,intervalRef);
  }).current;
  React.useEffect(function (){
    startInterval();
    return stopInterval;
  },[ms]);
  return {startInterval,stopInterval};
}

function runTimeoutStop(timeoutRef){
  let timeout = timeoutRef.current;
  if(null != timeout){
    clearTimeout(timeout);
    timeoutRef.current = null;
  }
  return timeout;
}

function runTimeoutStart(fRef,msRef,timeoutRef){
  let prev = runTimeoutStop(timeoutRef);
  let curr = setTimeout(function (){
    fRef.current();
  },msRef.current || 0);
  timeoutRef.current = curr;
  return [prev,curr];
}

function useTimeout(f,ms,init){
  let fRef = useFollowRef(f);
  let msRef = useFollowRef(ms);
  let timeoutRef = React.useRef(null);
  let stopTimeout = React.useRef(function (){
    return runTimeoutStop(timeoutRef);
  }).current;
  let startTimeout = React.useRef(function (){
    return runTimeoutStart(fRef,msRef,timeoutRef);
  }).current;
  React.useEffect(function (){
    if(false != init){
      startTimeout();
    }
    return stopTimeout;
  },[]);
  return {startTimeout,stopTimeout};
}

function useCountdown(initial,onComplete,opts){
  let {interval = 1000,step = 1,to = 0} = opts || {};
  let [current,setCurrent] = React.useState(initial);
  let {startInterval,stopInterval} = useInterval(function (){
    if(current > to){
      setCurrent(current - step);
    }
    else{
      stopInterval();
      if(onComplete){
        onComplete(current);
      }
    }
  },interval);
  return [
    current,
    setCurrent,
    {"startCountdown":startInterval,"stopCountdown":stopInterval}
  ];
}

function useNow(interval){
  let [now,setNow] = React.useState(Date.now());
  let {startInterval,stopInterval} = useInterval(function (){
    setNow(Date.now());
  },interval || 1000);
  return [now,{"startNow":startInterval,"stopNow":stopInterval}];
}

function useSubmit({
  result,
  delay = 200,
  setResult = (function (){
  return null;
}),
  onSubmit = (function (){
  return null;
}),
  onError = (function (res){
  console.log(" js.react/useSubmit 510\n\n","ERRORED",res);
  return res["body"];
}),
  onSuccess = k.identity,
  isMounted = (function (){
  return true;
})
}){
  let [waiting,setWaiting] = React.useState(function (){
    return false;
  });
  let onAction = function (){
    setWaiting(true);
    Promise.resolve().then(function (){
      if(onSubmit){
        return onSubmit();
      }
    }).then(function (res){
      if(isMounted()){
        setResult(onSuccess(res));
      }
      setTimeout(function (){
        if(isMounted()){
          setWaiting(false);
        }
      },delay);
    }).catch(function (err){
      setTimeout(function (){
        if(isMounted()){
          setWaiting(false);
        }
      },delay);
      if(isMounted()){
        setResult(onError(err));
      }
    });
  };
  let errored = result && ("error" == result["status"]);
  return {errored,onAction,setWaiting,waiting};
}

function useSubmitResult({onError,onResult,onSubmit,onSuccess,result,setResult}){
  let isMounted = useIsMounted();
  // eslint-disable-next-line react-hooks/rules-of-hooks
  [result,setResult] = ((null == setResult) ? React.useState() : [result,setResult]);
  React.useEffect(function (){
    if(onResult){
      onResult(result);
    }
  },[result]);
  let {errored,onAction,setWaiting,waiting} = useSubmit({isMounted,onError,onSubmit,onSuccess,result,setResult});
  return {errored,isMounted,onAction,result,setResult,setWaiting,waiting,"onActionPress":function (){
      return errored ? setResult(null) : onAction();
    },"onActionReset":function (){
      return setResult(null);
    }};
}

function convertIndex({data,value,setValue,allowNotFound,valueFn = k.identity}){
  let forwardFn = function (idx){
    let out = data && data[idx || 0];
    return out ? valueFn(out) : null;
  };
  let reverseFn = function (label){
    let idx = xtd.arr_find(xtd.arr_map(data,valueFn),function (item){
      return item == label;
    });
    return allowNotFound ? idx : Math.max(0,idx);
  };
  let setIndex = function (idx){
    setValue(forwardFn(idx));
  };
  let index = reverseFn(value);
  let items = xtd.arr_map(data,valueFn);
  return {index,items,setIndex};
}

function convertModular({data,value,setValue,valueFn = k.identity,indexFn}){
  let forwardFn = function (idx){
    let out = data && data[math.mod_pos(idx || 0,data.length)];
    return out ? valueFn(out) : null;
  };
  let reverseFn = function (label){
    let pval = indexFn();
    let nval = Math.max(0,xtd.arr_find(xtd.arr_map(data,valueFn),function (item){
      return item == label;
    }));
    let offset = math.mod_offset(pval,nval,data.length);
    return pval + offset;
  };
  let setIndex = function (idx){
    setValue(forwardFn(idx));
  };
  let index = reverseFn(value);
  let items = xtd.arr_map(data,valueFn);
  return {index,items,setIndex};
}

function convertIndices({data,values,setValues,valueFn = k.identity}){
  let forwardFn = function (indices){
    let out = [];
    for(let i = 0; i < indices.length; ++i){
      let e = indices[i];
      if(e){
        out.push(data[i]);
      }
    };
    return out;
  };
  let reverseFn = function (values){
    return xtd.arr_map(data,function (e){
      return 0 <= xtd.arr_find(values,function (item){
        return item == e;
      });
    });
  };
  let setIndices = function (indices){
    setValues(forwardFn(indices));
  };
  let indices = reverseFn(values);
  let items = xtd.arr_map(data,valueFn);
  return {indices,items,setIndices};
}

function convertPosition({length,max,min,step}){
  let divisions = Math.floor((max - min) / step);
  let unit = length / divisions;
  let forwardFn = function (value){
    let n = Math.floor((value - min) / step);
    return n * unit;
  };
  let reverseFn = function (pos){
    let relative = Math.max(0,Math.min(length,pos));
    let n = math.round(relative / unit);
    let out = min + (n * step);
    return out;
  };
  return {forwardFn,reverseFn};
}

function useChanging(data,f,state){
  f = (f || xtd.first);
  data = (data || []);
  // eslint-disable-next-line react-hooks/rules-of-hooks
  let [value,setValue] = state || React.useState(f(data));
  React.useEffect(function (){
    if(xtd.not_emptyp(data) && ((null == value) || (0 > xtd.arr_find(data,function (item){
      return item == value;
    })))){
      setValue(f(data));
    }
  },[JSON.stringify(data)]);
  return [value,setValue];
}

function useTree({branchesFn,displayFn,formatFn,initial,parents,root,setInitial,targetFn,tree}){
  branchesFn = (branchesFn || (function (tree,_parents,_root){
    if(tree){
      let out = xtd.obj_keys(tree);
      out.sort();
      return out;
    }
    else{
      return [];
    }
  }));
  targetFn = (targetFn || (function (tree,branch,_parents,_root){
    if(tree){
      return tree[branch];
    }
    else{
      return null;
    }
  }));
  let branches = branchesFn(tree,parents,root);
  let [branch,setBranch] = React.useState(initial || xtd.first(branches));
  let target = (tree && branch) ? targetFn(tree,branch,parents,root) : null;
  React.useEffect(function (){
    if((null != branch) && (null == target) && xtd.not_emptyp(branches) && targetFn(tree,xtd.first(branches),parents,root)){
      setBranch(xtd.first(branches));
    }
    if((null != branch) && setInitial && (initial != branch)){
      setInitial(branch);
    }
  },[branch,initial]);
  let view = displayFn(target,branch,parents,root);
  return {branch,branches,setBranch,view};
}

module.exports = {
  ["getDOMRoot"]:getDOMRoot,
  ["renderDOMRoot"]:renderDOMRoot,
  ["useStateFor"]:useStateFor,
  ["Try"]:Try,
  ["id"]:id,
  ["useStep"]:useStep,
  ["makeLazy"]:makeLazy,
  ["useLazy"]:useLazy,
  ["useRefresh"]:useRefresh,
  ["useGetCount"]:useGetCount,
  ["useFollowRef"]:useFollowRef,
  ["useIsMounted"]:useIsMounted,
  ["useIsMountedWrap"]:useIsMountedWrap,
  ["useMountedCallback"]:useMountedCallback,
  ["useFollowDelayed"]:useFollowDelayed,
  ["useStablized"]:useStablized,
  ["runIntervalStop"]:runIntervalStop,
  ["runIntervalStart"]:runIntervalStart,
  ["useInterval"]:useInterval,
  ["runTimeoutStop"]:runTimeoutStop,
  ["runTimeoutStart"]:runTimeoutStart,
  ["useTimeout"]:useTimeout,
  ["useCountdown"]:useCountdown,
  ["useNow"]:useNow,
  ["useSubmit"]:useSubmit,
  ["useSubmitResult"]:useSubmitResult,
  ["convertIndex"]:convertIndex,
  ["convertModular"]:convertModular,
  ["convertIndices"]:convertIndices,
  ["convertPosition"]:convertPosition,
  ["useChanging"]:useChanging,
  ["useTree"]:useTree
}