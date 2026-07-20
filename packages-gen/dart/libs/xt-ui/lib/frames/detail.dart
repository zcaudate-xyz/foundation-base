import 'package:xtalk_ui/core.dart' as ui;


view(frame, record) {
  var fields = (frame["opts"])["fields"] ?? <dynamic>[];
  var children = <dynamic>[];
  var arr_52834 = fields;
  for(var i52835 = 0; i52835 < arr_52834.length; ++i52835){
    var field = arr_52834[i52835];
    var id = field["id"];
    children.add(ui.node("ui/row",<dynamic, dynamic>{"class":"justify-between gap-4","key":id},<dynamic>[
      ui.node(
          "ui/label",
          <dynamic, dynamic>{"value":field["label"] ?? id},
          <dynamic>[]
        ),
      ui.text(record[id] ?? "",<dynamic, dynamic>{})
    ]));
  };
  return ui.node(
    "ui/card-content",
    <dynamic, dynamic>{"class":"flex flex-col gap-3"},
    children
  );
}