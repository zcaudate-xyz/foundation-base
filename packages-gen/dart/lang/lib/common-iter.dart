iter_eq(it0, it1, eq_fn) {
  return (() {
    while(it0.moveNext()){
      if(!it1.moveNext()){
        return false;
      }
      if(!eq_fn(it0.current,it1.current)){
        return false;
      }
    }
    return !it1.moveNext();
  })();
}

iter_null() {
  return <dynamic>[].iterator;
}

iterp(x) {
  return (null != x) && (((null != x) && (x.runtimeType).toString().contains("Iterator")) || ((("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")) && (x.containsKey("::") && ("iterator" == x["::"]))));
}

iter(x) {
  if(null == x){
    return iter_null();
  }
  else if((() {
    var dart_truthy__40035 = iterp(x);
    return (null != dart_truthy__40035) && (false != dart_truthy__40035);
  })()){
    return x;
  }
  else if((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")){
    return x.iterator;
  }
  else if(("Map" == (x.runtimeType).toString()) || (x.runtimeType).toString().startsWith("_Map") || (x.runtimeType).toString().startsWith("LinkedMap")){
    return x.entries.map((e) {
      return <dynamic>[e.key,e.value];
    }).iterator;
  }
  else if(((null != x) && (x.runtimeType).toString().contains("Iterator")) || (x.runtimeType).toString().contains("Iterable") || (x.runtimeType).toString().contains("List") || (x.runtimeType).toString().contains("Set")){
    return x.iterator;
  }
  else{
    return null;
  }
}

collect(it, f, init) {
  var out = init;
  var iter_40049 = iter(it);
  while(iter_40049.moveNext()){
    var e = iter_40049.current;
    out = f(out,e);
  };
  return out;
}

nil_lt(it) {
  var iter_40062 = iter(it);
  while(iter_40062.moveNext()){
    var e = iter_40062.current;
  };
  return null;
}

arr_lt(it) {
  var out = <dynamic>[];
  var iter_40075 = iter(it);
  while(iter_40075.moveNext()){
    var e = iter_40075.current;
    out.add(e);
  };
  return out;
}

obj_lt(it) {
  var out = <dynamic, dynamic>{};
  var iter_40088 = iter(it);
  while(iter_40088.moveNext()){
    var e = iter_40088.current;
    out[e[0]] = e[1];
  };
  return out;
}

constantly(val)  sync* {
  while(true){
    yield val;
  }
}

iterate(f, val)  sync* {
  while(true){
    yield val;
    val = f(val);
  }
}

repeatedly(f)  sync* {
  while(true){
    yield f();
  }
}

cycle(seq)  sync* {
  var arr = ((seq.runtimeType).toString().startsWith("List") || (seq.runtimeType).toString().startsWith("_GrowableList")) ? seq : arr_lt(seq);
  if(0 == arr.length){
    throw "Cannot be empty";
  }
  while(true){
    var arr_40101 = arr;
    for(var i40102 = 0; i40102 < arr_40101.length; ++i40102){
      var e = arr_40101[i40102];
      yield e;
    };
  }
}

range(x)  sync* {
  var arr = ((x.runtimeType).toString().startsWith("List") || (x.runtimeType).toString().startsWith("_GrowableList")) ? x : <dynamic>[x];
  var arrlen = arr.length;
  var start = (1 < arrlen) ? arr[0] : 0;
  var finish = (1 < arrlen) ? arr[1] : arr[0];
  var step = (2 < arrlen) ? arr[2] : 1;
  var i = start;
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

drop(n, seq)  sync* {
  var i = n;
  var iter_40123 = iter(seq);
  while(iter_40123.moveNext()){
    var e = iter_40123.current;
    if(0 < i){
      i = (i - 1);
    }
    else{
      yield e;
    }
  };
}

peek(f, seq)  sync* {
  var iter_40136 = iter(seq);
  while(iter_40136.moveNext()){
    var e = iter_40136.current;
    f(e);
    yield e;
  };
}

take(n, seq)  sync* {
  var i = 0;
  var iter_40149 = iter(seq);
  while(iter_40149.moveNext()){
    var e = iter_40149.current;
    if(i < n){
      i = (i + 1);
      yield e;
    }
    else{
      return;
    }
  };
}

map(f, seq)  sync* {
  var iter_40162 = iter(seq);
  while(iter_40162.moveNext()){
    var e = iter_40162.current;
    yield f(e);
  };
}

mapcat(f, seq)  sync* {
  var iter_40175 = iter(seq);
  while(iter_40175.moveNext()){
    var e0 = iter_40175.current;
    var s0 = f(e0);
    var iter_40188 = iter(s0);
    while(iter_40188.moveNext()){
      var e1 = iter_40188.current;
      yield e1;
    };
  };
}

concat(seq)  sync* {
  var iter_40201 = iter(mapcat((x) {
    return x;
  },seq));
  while(iter_40201.moveNext()){
    var e = iter_40201.current;
    yield e;
  };
}

filter(pred, seq)  sync* {
  var iter_40214 = iter(seq);
  while(iter_40214.moveNext()){
    var e = iter_40214.current;
    if((() {
      var dart_truthy__40036 = pred(e);
      return (null != dart_truthy__40036) && (false != dart_truthy__40036);
    })()){
      yield e;
    }
  };
}

keep(f, seq)  sync* {
  var iter_40227 = iter(seq);
  while(iter_40227.moveNext()){
    var e = iter_40227.current;
    var v = f(e);
    if(null != v){
      yield v;
    }
  };
}

partition(n, seq)  sync* {
  if(1 > n){
    throw "Partition should be positive";
  }
  var out = <dynamic>[];
  var iter_40240 = iter(seq);
  while(iter_40240.moveNext()){
    var e = iter_40240.current;
    if(out.length < n){
      out.add(e);
    }
    else{
      yield out;
      out = <dynamic>[];
    }
  };
  if(1 < out.length){
    yield out;
  }
}

take_nth(n, seq)  sync* {
  if(1 > n){
    throw "Partition should be positive";
  }
  var i = 0;
  var iter_40253 = iter(seq);
  while(iter_40253.moveNext()){
    var e = iter_40253.current;
    if(i == 0){
      yield e;
      i = (n - 1);
    }
    else{
      i = (i - 1);
    }
  };
}