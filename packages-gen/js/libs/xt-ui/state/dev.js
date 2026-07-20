function create(enabled,capabilities){
  return {
    "enabled":true == enabled,
    "capabilities":capabilities || {},
    "status":"idle",
    "summary":{},
    "panels":{}
  };
}

function availablep(state,capability_id){
  return (true == state["enabled"]) && (true == state["capabilities"][capability_id]);
}

function set_summaryf(state,summary){
  state["summary"] = (summary || {});
  state["status"] = "ready";
  return state;
}

module.exports = {
  ["create"]:create,
  ["availablep"]:availablep,
  ["set_summaryf"]:set_summaryf
}