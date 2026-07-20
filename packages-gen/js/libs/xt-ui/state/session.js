function project(session,profile,capabilities){
  return {
    "authenticated":true == (session || {})["authenticated"],
    "user_id":(session || {})["user_id"],
    "profile":profile || {},
    "capabilities":capabilities || {}
  };
}

function capablep(state,capability_id){
  return true == state["capabilities"][capability_id];
}

module.exports = {["project"]:project,["capablep"]:capablep}