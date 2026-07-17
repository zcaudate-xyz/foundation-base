import 'package:xtalk_ui/core.dart' as ui;

view(state, fallback) {
  if(true == state["pending"]){
    return ui.node(
      "ui/spinner",
      <dynamic, dynamic>{"label":"Loading"},
      <dynamic>[]
    );
  }
  if(null != state["error"]){
    return ui.node(
      "ui/alert",
      <dynamic, dynamic>{"tone":"error"},
      <dynamic>[ui.text((state["error"]).toString(),<dynamic, dynamic>{})]
    );
  }
  return fallback;
}