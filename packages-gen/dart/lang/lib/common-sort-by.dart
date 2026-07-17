import 'package:xtalk_lang/common-data.dart' as xt.lang.common_data;

sort_by(arr, inputs) {
  var keys = xt.lang.common_data.arr_map(inputs,(e) {
    return ((e.runtimeType).toString().startsWith("List") || (e.runtimeType).toString().startsWith("_GrowableList")) ? e[0] : e;
  });
  var inverts = xt.lang.common_data.arr_map(inputs,(e) {
    return ((e.runtimeType).toString().startsWith("List") || (e.runtimeType).toString().startsWith("_GrowableList")) ? e[1] : false;
  });
  var get_fn = (e, key) {
    if((key.runtimeType).toString().contains("Function") || (key.runtimeType).toString().contains("=>") || (key).toString().startsWith("Closure")){
      return key(e);
    }
    else{
      return e[key];
    }
  };
  var key_fn = (e) {
    return xt.lang.common_data.arr_map(keys,(key) {
      return Function.apply((get_fn as Function),<dynamic>[e,key]);
    });
  };
  var comp_fn = (a0, a1) {
    var arr_39640 = a0;
    for(var i = 0; i < arr_39640.length; ++i){
      var v0 = arr_39640[i];
      var v1 = a1[i];
      var invert = inverts[i];
      if(v0 != v1){
        if((null != invert) && (false != invert)){
          if(("int" == (v0.runtimeType).toString()) || ("double" == (v0.runtimeType).toString()) || ("num" == (v0.runtimeType).toString())){
            return (("String" == (v1.runtimeType).toString()) || ("String" == (v0.runtimeType).toString())) ? ((v1).toString().compareTo((v0).toString()) < 0) : (v1 < v0);
          }
          else{
            return ((v1).toString()).toString().compareTo(((v0).toString()).toString()) < 0;
          }
        }
        else{
          if(("int" == (v0.runtimeType).toString()) || ("double" == (v0.runtimeType).toString()) || ("num" == (v0.runtimeType).toString())){
            return (("String" == (v0.runtimeType).toString()) || ("String" == (v1.runtimeType).toString())) ? ((v0).toString().compareTo((v1).toString()) < 0) : (v0 < v1);
          }
          else{
            return ((v0).toString()).toString().compareTo(((v1).toString()).toString()) < 0;
          }
        }
      }
    };
    return false;
  };
  var out = xt.lang.common_data.arr_clone(arr);
  xt.lang.common_data.arr_sort(out,key_fn,comp_fn);
  return out;
}