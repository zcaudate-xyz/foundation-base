import 'package:xtalk_ui/core.dart' as ui;


view(frame, state, actions) {
  var collection = state["collection"] ?? state;
  var columns = (frame["opts"])["columns"] ?? <dynamic>[];
  var header = <dynamic>[];
  var arr_52878 = columns;
  for(var i52879 = 0; i52879 < arr_52878.length; ++i52879){
    var column = arr_52878[i52879];
    header.add(ui.node(
      "ui/table-cell",
      <dynamic, dynamic>{"value":column["label"] ?? column["id"]},
      <dynamic>[]
    ));
  };
  var rows = <dynamic>[];
  var arr_52900 = collection["items"] ?? <dynamic>[];
  for(var i52901 = 0; i52901 < arr_52900.length; ++i52901){
    var item = arr_52900[i52901];
    var cells = <dynamic>[];
    var arr_52922 = columns;
    for(var i52923 = 0; i52923 < arr_52922.length; ++i52923){
      var column = arr_52922[i52923];
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