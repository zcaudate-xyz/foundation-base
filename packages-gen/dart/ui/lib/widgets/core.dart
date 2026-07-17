import 'package:xtalk_ui/core.dart' as ui;

import 'package:xtalk_lang/common-data.dart' as xtd;

register(registry, component_id, props, events, slots) {
  return ui.registry_register_contract(
    registry,
    ui.component_contract(component_id,"portable",props,events,slots,null)
  );
}

semantic_registry() {
  var registry = ui.registry_create("xt.ui/widgets");
  register(
    registry,
    "ui/card",
    <dynamic>["class","hidden","key"],
    <dynamic>[],
    <dynamic>["header","footer"]
  );
  register(
    registry,
    "ui/card-header",
    <dynamic>["class","hidden","key"],
    <dynamic>[],
    <dynamic>[]
  );
  register(
    registry,
    "ui/card-content",
    <dynamic>["class","hidden","key"],
    <dynamic>[],
    <dynamic>[]
  );
  register(
    registry,
    "ui/title",
    <dynamic>["value","class","hidden","key"],
    <dynamic>[],
    <dynamic>[]
  );
  register(
    registry,
    "ui/description",
    <dynamic>["value","class","hidden","key"],
    <dynamic>[],
    <dynamic>[]
  );
  register(
    registry,
    "ui/label",
    <dynamic>["value","for","class","hidden","key"],
    <dynamic>[],
    <dynamic>[]
  );
  register(registry,"ui/input",<dynamic>[
    "id",
    "value",
    "placeholder",
    "type",
    "class",
    "disabled",
    "read_only",
    "aria_label",
    "key"
  ],<dynamic>["on_change","on_submit"],<dynamic>[]);
  register(registry,"ui/textarea",<dynamic>[
    "id",
    "value",
    "placeholder",
    "class",
    "disabled",
    "read_only",
    "rows",
    "aria_label",
    "key"
  ],<dynamic>["on_change"],<dynamic>[]);
  register(
    registry,
    "ui/button",
    <dynamic>["variant","size","class","disabled","pending","aria_label","key"],
    <dynamic>["on_press"],
    <dynamic>[]
  );
  register(
    registry,
    "ui/alert",
    <dynamic>["tone","class","hidden","key"],
    <dynamic>[],
    <dynamic>[]
  );
  register(
    registry,
    "ui/spinner",
    <dynamic>["class","label","hidden","key"],
    <dynamic>[],
    <dynamic>[]
  );
  register(
    registry,
    "ui/table",
    <dynamic>["class","hidden","key"],
    <dynamic>[],
    <dynamic>["header","body"]
  );
  register(
    registry,
    "ui/table-header",
    <dynamic>["class","hidden","key"],
    <dynamic>[],
    <dynamic>[]
  );
  register(
    registry,
    "ui/table-body",
    <dynamic>["class","hidden","key"],
    <dynamic>[],
    <dynamic>[]
  );
  register(
    registry,
    "ui/table-row",
    <dynamic>["class","hidden","key","selected"],
    <dynamic>["on_press"],
    <dynamic>[]
  );
  register(
    registry,
    "ui/table-cell",
    <dynamic>["class","hidden","key","value"],
    <dynamic>[],
    <dynamic>[]
  );
  return registry;
}

registry() {
  return ui.registry_compose(<dynamic>[ui.base_registry(),semantic_registry()]);
}

widget(component, props, children) {
  return ui.node(
    component,
    props ?? <dynamic, dynamic>{},
    children ?? <dynamic>[]
  );
}

field(component, id, label, value, props, on_change) {
  return ui.node("ui/column",<dynamic, dynamic>{"class":"gap-2"},<dynamic>[
    ui.node(
      "ui/label",
      <dynamic, dynamic>{"value":label,"for":id},
      <dynamic>[]
    ),
    ui.node(component,xtd.obj_assign(
      <dynamic, dynamic>{"id":id,"value":value ?? "","on_change":on_change},
      props ?? <dynamic, dynamic>{}
    ),<dynamic>[])
  ]);
}