import 'package:xtalk_ui/core.dart' as ui;


view(frame, content) {
  var opts = frame["opts"];
  return ui.node("ui/column",<dynamic, dynamic>{"class":opts["class"] ?? "mx-auto w-full gap-6 p-4 md:p-8"},<dynamic>[
    ui.node("ui/column",<dynamic, dynamic>{"class":"gap-1"},<dynamic>[
      ui.node(
        "ui/title",
        <dynamic, dynamic>{"value":opts["title"] ?? ""},
        <dynamic>[]
      ),
      ui.node(
        "ui/description",
        <dynamic, dynamic>{"value":opts["description"] ?? ""},
        <dynamic>[]
      )
    ]),
    ui.slot(
      frame["id"] + "/content",
      content ?? <dynamic>[],
      <dynamic, dynamic>{}
    )
  ]);
}