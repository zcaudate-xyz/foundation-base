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
    var dart_truthy__50333 = iterp(x);
    return (null != dart_truthy__50333) && (false != dart_truthy__50333);
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
  var iter_50347 = iter(it);
  while(iter_50347.moveNext()){
    var e = iter_50347.current;
    out = f(out,e);
  };
  return out;
}

nil_lt(it) {
  var iter_50360 = iter(it);
  while(iter_50360.moveNext()){
    var e = iter_50360.current;
  };
  return null;
}

arr_lt(it) {
  var out = <dynamic>[];
  var iter_50373 = iter(it);
  while(iter_50373.moveNext()){
    var e = iter_50373.current;
    out.add(e);
  };
  return out;
}

obj_lt(it) {
  var out = <dynamic, dynamic>{};
  var iter_50386 = iter(it);
  while(iter_50386.moveNext()){
    var e = iter_50386.current;
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
    var arr_50399 = arr;
    for(var i50400 = 0; i50400 < arr_50399.length; ++i50400){
      var e = arr_50399[i50400];
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
  var iter_50421 = iter(seq);
  while(iter_50421.moveNext()){
    var e = iter_50421.current;
    if(0 < i){
      i = (i - 1);
    }
    else{
      yield e;
    }
  };
}

peek(f, seq)  sync* {
  var iter_50434 = iter(seq);
  while(iter_50434.moveNext()){
    var e = iter_50434.current;
    f(e);
    yield e;
  };
}

take(n, seq)  sync* {
  var i = 0;
  var iter_50447 = iter(seq);
  while(iter_50447.moveNext()){
    var e = iter_50447.current;
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
  var iter_50460 = iter(seq);
  while(iter_50460.moveNext()){
    var e = iter_50460.current;
    yield f(e);
  };
}

mapcat(f, seq)  sync* {
  var iter_50473 = iter(seq);
  while(iter_50473.moveNext()){
    var e0 = iter_50473.current;
    var s0 = f(e0);
    var iter_50486 = iter(s0);
    while(iter_50486.moveNext()){
      var e1 = iter_50486.current;
      yield e1;
    };
  };
}

concat(seq)  sync* {
  var iter_50499 = iter(mapcat((x) {
    return x;
  },seq));
  while(iter_50499.moveNext()){
    var e = iter_50499.current;
    yield e;
  };
}

filter(pred, seq)  sync* {
  var iter_50512 = iter(seq);
  while(iter_50512.moveNext()){
    var e = iter_50512.current;
    if((() {
      var dart_truthy__50334 = pred(e);
      return (null != dart_truthy__50334) && (false != dart_truthy__50334);
    })()){
      yield e;
    }
  };
}

keep(f, seq)  sync* {
  var iter_50525 = iter(seq);
  while(iter_50525.moveNext()){
    var e = iter_50525.current;
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
  var iter_50538 = iter(seq);
  while(iter_50538.moveNext()){
    var e = iter_50538.current;
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
  var iter_50551 = iter(seq);
  while(iter_50551.moveNext()){
    var e = iter_50551.current;
    if(i == 0){
      yield e;
      i = (n - 1);
    }
    else{
      i = (i - 1);
    }
  };
}