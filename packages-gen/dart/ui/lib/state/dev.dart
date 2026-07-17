create(enabled, capabilities) {
  return <dynamic, dynamic>{
    "enabled":true == enabled,
    "capabilities":capabilities ?? <dynamic, dynamic>{},
    "status":"idle",
    "summary":<dynamic, dynamic>{},
    "panels":<dynamic, dynamic>{}
  };
}

availablep(state, capability_id) {
  return (true == state["enabled"]) && (true == state["capabilities"][capability_id]);
}

set_summaryf(state, summary) {
  state["summary"] = (summary ?? <dynamic, dynamic>{});
  state["status"] = "ready";
  return state;
}