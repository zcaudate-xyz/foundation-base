function iter_eq(it0,it1,eq_fn){
  for(let x0 of it0){
    let r1 = it1.next();
    if(r1.done){
      return false;
    }
    else if(!eq_fn(x0,r1.value)){
      return false;
    }
  }
  return it1.next().done;
}

function iter_null(){
  return [][Symbol.iterator]();
}

function iterp(x){
  return (null != x) && (("function" == (typeof x["next"])) || (((null != x) && ("object" == (typeof x)) && !Array.isArray(x)) && ("iterator" == x["::"])));
}

function iter(x){
  if(null == x){
    return iter_null();
  }
  else if(iterp(x)){
    return x;
  }
  else if(Array.isArray(x)){
    return x[Symbol.iterator]();
  }
  else if((null != x) && ("object" == (typeof x)) && !Array.isArray(x)){
    return Object.entries(x)[Symbol.iterator]();
  }
  else if(null != x[Symbol.iterator]){
    return x[Symbol.iterator]();
  }
  else{
    return null;
  }
}

function collect(it,f,init){
  let out = init;
  for(let e of iter(it)){
    out = f(out,e);
  };
  return out;
}

function nil_lt(it){
  for(let e of iter(it)){
    
  };
  return null;
}

function arr_lt(it){
  let out = [];
  for(let e of iter(it)){
    out.push(e);
  };
  return out;
}

function obj_lt(it){
  let out = {};
  for(let e of iter(it)){
    out[e[0]] = e[1];
  };
  return out;
}

function* constantly(val){
  while(true){
    yield val;
  }
}

function* iterate(f,val){
  while(true){
    yield val;
    val = f(val);
  }
}

function* repeatedly(f){
  while(true){
    yield f();
  }
}

function* cycle(seq){
  let arr = Array.isArray(seq) ? seq : arr_lt(seq);
  if(0 == arr.length){
    throw "Cannot be empty";
  }
  while(true){
    for(let e of arr){
      yield e;
    };
  }
}

function* range(x){
  let arr = Array.isArray(x) ? x : [x];
  let arrlen = arr.length;
  let start = (1 < arrlen) ? arr[0] : 0;
  let finish = (1 < arrlen) ? arr[1] : arr[0];
  let step = (2 < arrlen) ? arr[2] : 1;
  let i = start;
  if((step > 0) && (start < finish)){
    while(i < finish){
      yield i;
      i = (i + step);
    }
  }
  else if((step < 0) && (finish < start)){
    while(i > finish){
      yield i;
      i = (i + step);
    }
  }
  else{
    return;
  }
}

function* drop(n,seq){
  let i = n;
  for(let e of iter(seq)){
    if(0 < i){
      i = (i - 1);
    }
    else{
      yield e;
    }
  };
}

function* peek(f,seq){
  for(let e of iter(seq)){
    f(e);
    yield e;
  };
}

function* take(n,seq){
  let i = 0;
  for(let e of iter(seq)){
    if(i < n){
      i = (i + 1);
      yield e;
    }
    else{
      return;
    }
  };
}

function* map(f,seq){
  for(let e of iter(seq)){
    yield f(e);
  };
}

function* mapcat(f,seq){
  for(let e0 of iter(seq)){
    let s0 = f(e0);
    for(let e1 of iter(s0)){
      yield e1;
    };
  };
}

function* concat(seq){
  for(let e of iter(mapcat(function (x){
    return x;
  },seq))){
    yield e;
  };
}

function* filter(pred,seq){
  for(let e of iter(seq)){
    if(pred(e)){
      yield e;
    }
  };
}

function* keep(f,seq){
  for(let e of iter(seq)){
    let v = f(e);
    if(null != v){
      yield v;
    }
  };
}

function* partition(n,seq){
  if(1 > n){
    throw "Partition should be positive";
  }
  let out = [];
  for(let e of iter(seq)){
    if(out.length < n){
      out.push(e);
    }
    else{
      yield out;
      out = [];
    }
  };
  if(1 < out.length){
    yield out;
  }
}

function* take_nth(n,seq){
  if(1 > n){
    throw "Partition should be positive";
  }
  let i = 0;
  for(let e of iter(seq)){
    if(i == 0){
      yield e;
      i = (n - 1);
    }
    else{
      i = (i - 1);
    }
  };
}

module.exports = {
  ["iter_eq"]:iter_eq,
  ["iter_null"]:iter_null,
  ["iterp"]:iterp,
  ["iter"]:iter,
  ["collect"]:collect,
  ["nil_lt"]:nil_lt,
  ["arr_lt"]:arr_lt,
  ["obj_lt"]:obj_lt
}