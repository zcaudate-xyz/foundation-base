const ui = require("@xtalk/ui/core.js")

function view(state,fallback){
  if(true == state["pending"]){
    return ui.node("ui/spinner",{"label":"Loading"},[]);
  }
  if(null != state["error"]){
    return ui.node(
      "ui/alert",
      {"tone":"error"},
      [ui.text(String(state["error"]),{})]
    );
  }
  return fallback;
}

module.exports = {["view"]:view}