create() {
  return <dynamic, dynamic>{"pending":false,"error":null,"message":null,"retryable":false};
}

pendingf(state, value) {
  state["pending"] = (true == value);
  if(true == value){
    state["error"] = null;
  }
  return state;
}

failf(state, error, retryable) {
  state["pending"] = false;
  state["error"] = error;
  state["retryable"] = (true == retryable);
  return state;
}

clearf(state) {
  state["pending"] = false;
  state["error"] = null;
  state["message"] = null;
  state["retryable"] = false;
  return state;
}