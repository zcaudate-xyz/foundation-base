const xt.lang.common_data = require("@xtalk/lang/common-data.js")

function sort_by(arr,inputs){
  let keys = inputs.map(function (e){
    return Array.isArray(e) ? e[0] : e;
  });
  let inverts = inputs.map(function (e){
    return Array.isArray(e) ? e[1] : false;
  });
  let get_fn = function (e,key){
    if("function" == (typeof key)){
      return key(e);
    }
    else{
      return e[key];
    }
  };
  let key_fn = function (e){
    return keys.map(function (key){
      return get_fn(e,key);
    });
  };
  let comp_fn = function (a0,a1){
    for(let i = 0; i < a0.length; ++i){
      let v0 = a0[i];
      let v1 = a1[i];
      let invert = inverts[i];
      if(v0 != v1){
        if(invert){
          if("number" == (typeof v0)){
            return v1 < v0;
          }
          else{
            return 0 > String(v1).localeCompare(String(v0));
          }
        }
        else{
          if("number" == (typeof v0)){
            return v0 < v1;
          }
          else{
            return 0 > String(v0).localeCompare(String(v1));
          }
        }
      }
    };
    return false;
  };
  let out = arr.slice();
  xt.lang.common_data.arr_sort(out,key_fn,comp_fn);
  return out;
}

module.exports = {["sort_by"]:sort_by}