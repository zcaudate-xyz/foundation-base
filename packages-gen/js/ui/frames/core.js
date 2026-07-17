const xtd = require("@xtalk/lang/common-data.js")

function spec(id,kind,regions,opts){
  return {"id":id,"kind":kind,"regions":regions || {},"opts":opts || {}};
}

function region(frame,region_id,fallback){
  return frame["regions"][region_id] || fallback;
}

function override(frame,overrides){
  let next = xtd.clone_nested(frame);
  for(let [region_id,value] of Object.entries(overrides || {})){
    xtd.set_in(next,["regions",region_id],value);
  };
  return next;
}

module.exports = {["spec"]:spec,["region"]:region,["override"]:override}