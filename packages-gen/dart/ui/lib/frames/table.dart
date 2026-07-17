import 'package:xtalk_ui/core.dart' as ui;

view(frame, state, actions) {
  var collection = state["collection"] ?? state;
  var columns = (frame["opts"])["columns"] ?? <dynamic>[];
  var header = <dynamic>[];
  var arr_50332 = columns;
  for(var i50333 = 0; i50333 < arr_50332.length; ++i50333){
    var column = arr_50332[i50333];
    header.add(ui.node(
      "ui/table-cell",
      <dynamic, dynamic>{"value":column["label"] ?? column["id"]},
      <dynamic>[]
    ));
  };
  var rows = <dynamic>[];
  var arr_50354 = collection["items"] ?? <dynamic>[];
  for(var i50355 = 0; i50355 < arr_50354.length; ++i50355){
    var item = arr_50354[i50355];
    var cells = <dynamic>[];
    var arr_50376 = columns;
    for(var i50377 = 0; i50377 < arr_50376.length; ++i50377){
      var column = arr_50376[i50377];
      cells.add(ui.node(
        "ui/table-cell",
        <dynamic, dynamic>{"value":item[column["id"]]},
        <dynamic>[]
      ));
    };
    rows.add(
      ui.node("ui/table-row",<dynamic, dynamic>{"key":item["id"]},cells)
    );
  };
  return ui.node("ui/column",<dynamic, dynamic>{"class":"gap-4"},<dynamic>[
    ui.slot(frame["id"] + "/toolbar",<dynamic>[],<dynamic, dynamic>{}),
    ui.node("ui/table",<dynamic, dynamic>{"class":"w-full"},<dynamic>[
      ui.node("ui/table-header",<dynamic, dynamic>{},header),
      ui.node("ui/table-body",<dynamic, dynamic>{},rows)
    ])
  ]);
}