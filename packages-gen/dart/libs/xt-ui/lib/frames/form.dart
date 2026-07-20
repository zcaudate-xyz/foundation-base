import 'package:xtalk_ui/core.dart' as ui;


invoke(actions, action_id, payload) {
  var handler = (actions ?? <dynamic, dynamic>{})[action_id];
  if((handler.runtimeType).toString().contains("Function") || (handler.runtimeType).toString().contains("=>") || (handler).toString().startsWith("Closure")){
    return Function.apply((handler as Function),<dynamic>[payload]);
  }
  return null;
}

view(frame, state, actions) {
  var form = state["form"] ?? state;
  var draft = form["draft"] ?? <dynamic, dynamic>{};
  var errors = form["errors"] ?? <dynamic, dynamic>{};
  var fields = (frame["opts"])["fields"] ?? <dynamic>[];
  var children = <dynamic>[];
  var arr_52856 = fields;
  for(var i52857 = 0; i52857 < arr_52856.length; ++i52857){
    var field = arr_52856[i52857];
    var id = field["id"];
    var component = field["component"] ?? "ui/input";
    children.add(ui.node("ui/column",<dynamic, dynamic>{"class":"gap-2","key":id},<dynamic>[
      ui.node(
          "ui/label",
          <dynamic, dynamic>{"value":field["label"] ?? id,"for":id},
          <dynamic>[]
        ),
      ui.node(component,<dynamic, dynamic>{
          "id":id,
          "value":draft[id] ?? "",
          "disabled":true == form["pending"],
          "on_change":(value) {
                return invoke(
                  actions,
                  "set_field",
                  <dynamic, dynamic>{"field":id,"value":value}
                );
              }
        },<dynamic>[]),
      ui.node(
          "ui/alert",
          <dynamic, dynamic>{"tone":"error","hidden":null == errors[id]},
          <dynamic>[ui.text(errors[id] ?? "",<dynamic, dynamic>{})]
        )
    ]));
  };
  children.add(ui.node("ui/button",<dynamic, dynamic>{
    "pending":true == form["pending"],
    "disabled":(true != form["valid"]) || (true == form["pending"]),
    "on_press":(_) {
        return invoke(actions,"submit",draft);
      }
  },<dynamic>[ui.text("Save",<dynamic, dynamic>{})]));
  return ui.node("ui/column",<dynamic, dynamic>{"class":"gap-4"},children);
}