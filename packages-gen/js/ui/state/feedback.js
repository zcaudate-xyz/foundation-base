function create(){
  return {"pending":false,"error":null,"message":null,"retryable":false};
}

function pendingf(state,value){
  state["pending"] = (true == value);
  if(true == value){
    state["error"] = null;
  }
  return state;
}

function failf(state,error,retryable){
  state["pending"] = false;
  state["error"] = error;
  state["retryable"] = (true == retryable);
  return state;
}

function clearf(state){
  state["pending"] = false;
  state["error"] = null;
  state["message"] = null;
  state["retryable"] = false;
  return state;
}

module.exports = {
  ["create"]:create,
  ["pendingf"]:pendingf,
  ["failf"]:failf,
  ["clearf"]:clearf
}