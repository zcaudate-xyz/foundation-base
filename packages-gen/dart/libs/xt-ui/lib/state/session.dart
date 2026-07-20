project(session, profile, capabilities) {
  return <dynamic, dynamic>{
    "authenticated":true == (session ?? <dynamic, dynamic>{})["authenticated"],
    "user_id":(session ?? <dynamic, dynamic>{})["user_id"],
    "profile":profile ?? <dynamic, dynamic>{},
    "capabilities":capabilities ?? <dynamic, dynamic>{}
  };
}

capablep(state, capability_id) {
  return true == state["capabilities"][capability_id];
}