import 'package:xtalk_substrate/view.dart' as view;


sink_section(title, children) {
  return view.node("ui/column",<dynamic, dynamic>{"class":"flex flex-col gap-3"},<dynamic>[
    view.node("ui/label",<dynamic, dynamic>{
      "value":title,
      "class":"text-xs font-semibold uppercase tracking-wide text-slate-400"
    },<dynamic>[]),
    children
  ]);
}

collect_ids(value, out) {
  if((value.runtimeType).toString().startsWith("List") || (value.runtimeType).toString().startsWith("_GrowableList")){
    var arr_51182 = value;
    for(var i51183 = 0; i51183 < arr_51182.length; ++i51183){
      var item = arr_51182[i51183];
      collect_ids(item,out);
    };
  }
  else if(("Map" == (value.runtimeType).toString()) || (value.runtimeType).toString().startsWith("_Map") || (value.runtimeType).toString().startsWith("LinkedMap")){
    out.add(value["component"]);
    collect_ids(value["children"] ?? <dynamic>[],out);
  }
  else{
    null;
  }
  return out;
}

has_valp(arr, value) {
  var found = false;
  var arr_51204 = arr;
  for(var i51205 = 0; i51205 < arr_51204.length; ++i51205){
    var item = arr_51204[i51205];
    if(item == value){
      found = true;
    }
  };
  return found;
}

kitchen_sink_render(snapshot) {
  var pending = true == snapshot["pending"];
  var fills = snapshot["fills"] ?? <dynamic>[
    <dynamic>["10:02","BUY","12.50","4"],
    <dynamic>["10:05","SELL","12.55","2"],
    <dynamic>["10:11","BUY","12.45","6"]
  ];
  return view.node("ui/column",<dynamic, dynamic>{"class":"flex flex-col gap-8 p-8 max-w-3xl"},<dynamic>[
    view.node("ui/row",<dynamic, dynamic>{"class":"flex flex-row items-center justify-between"},<dynamic>[
      view.node("ui/column",<dynamic, dynamic>{"class":"flex flex-col gap-1"},<dynamic>[
        view.node(
          "ui/title",
          <dynamic, dynamic>{"value":"Kitchen sink"},
          <dynamic>[]
        ),
        view.node("ui/description",<dynamic, dynamic>{
          "value":"Every portable ui/ component in one serializable tree."
        },<dynamic>[])
      ]),
      view.node(
        "ui/badge",
        <dynamic, dynamic>{"value":"v1","variant":"secondary"},
        <dynamic>[]
      )
    ]),
    view.node(
      "ui/separator",
      <dynamic, dynamic>{"class":"bg-slate-200"},
      <dynamic>[]
    ),
    sink_section("Text",<dynamic>[
      view.node(
        "ui/text",
        <dynamic, dynamic>{"value":"Body text - span on web, WText in Wind."},
        <dynamic>[]
      ),
      view.node(
        "ui/label",
        <dynamic, dynamic>{"value":"Inline label","for":"sink-name"},
        <dynamic>[]
      ),
      view.node("ui/row",<dynamic, dynamic>{"class":"flex flex-row items-center gap-2"},<dynamic>[
        view.node(
          "ui/icon",
          <dynamic, dynamic>{"value":"info","aria_label":"Info"},
          <dynamic>[]
        ),
        view.node(
          "ui/spinner",
          <dynamic, dynamic>{"value":"Loading…"},
          <dynamic>[]
        )
      ]),
      view.node(
        "ui/text",
        <dynamic, dynamic>{"value":"you should not see this","hidden":true},
        <dynamic>[]
      )
    ]),
    sink_section("Badges",<dynamic>[
      view.node("ui/row",<dynamic, dynamic>{"class":"flex flex-row gap-2"},<dynamic>[
        view.node("ui/badge",<dynamic, dynamic>{"value":"default"},<dynamic>[]),
        view.node(
          "ui/badge",
          <dynamic, dynamic>{"value":"secondary","variant":"secondary"},
          <dynamic>[]
        ),
        view.node(
          "ui/badge",
          <dynamic, dynamic>{"value":"outline","variant":"outline"},
          <dynamic>[]
        ),
        view.node(
          "ui/badge",
          <dynamic, dynamic>{"value":"destructive","variant":"destructive"},
          <dynamic>[]
        )
      ])
    ]),
    sink_section("Buttons",<dynamic>[
      view.node("ui/row",<dynamic, dynamic>{"class":"flex flex-row flex-wrap gap-2"},<dynamic>[
        view.node("ui/button",<dynamic, dynamic>{},<dynamic>["Default"]),
        view.node(
          "ui/button",
          <dynamic, dynamic>{"variant":"secondary"},
          <dynamic>["Secondary"]
        ),
        view.node(
          "ui/button",
          <dynamic, dynamic>{"variant":"outline"},
          <dynamic>["Outline"]
        ),
        view.node(
          "ui/button",
          <dynamic, dynamic>{"variant":"ghost"},
          <dynamic>["Ghost"]
        ),
        view.node(
          "ui/button",
          <dynamic, dynamic>{"variant":"destructive"},
          <dynamic>["Delete"]
        ),
        view.node(
          "ui/button",
          <dynamic, dynamic>{"variant":"link"},
          <dynamic>["Link"]
        ),
        view.node(
          "ui/button",
          <dynamic, dynamic>{"size":"sm"},
          <dynamic>["Small"]
        ),
        view.node(
          "ui/button",
          <dynamic, dynamic>{"disabled":true},
          <dynamic>["Disabled"]
        ),
        view.node(
          "ui/button",
          <dynamic, dynamic>{"pending":pending},
          <dynamic>["Saving"]
        ),
        view.node("ui/button",<dynamic, dynamic>{
          "variant":"default",
          "on_press":view.action("demo/sink-press",null)
        },<dynamic>["Fire action"])
      ])
    ]),
    sink_section("Alerts",<dynamic>[
      view.node("ui/alert",<dynamic, dynamic>{"variant":"default"},<dynamic>[
        view.node(
          "ui/text",
          <dynamic, dynamic>{"value":"Default alert body"},
          <dynamic>[]
        )
      ]),
      view.node("ui/alert",<dynamic, dynamic>{"variant":"destructive"},<dynamic>[
        view.node(
          "ui/text",
          <dynamic, dynamic>{"value":"Destructive alert body"},
          <dynamic>[]
        )
      ])
    ]),
    sink_section("Form",<dynamic>[
      view.node("ui/card",<dynamic, dynamic>{"class":"flex flex-col gap-4 p-6"},<dynamic>[
        view.node("ui/card-header",<dynamic, dynamic>{"class":"flex flex-col gap-1 p-0"},<dynamic>[
          view.node(
            "ui/card-title",
            <dynamic, dynamic>{"value":"Create organisation"},
            <dynamic>[]
          ),
          view.node(
            "ui/card-description",
            <dynamic, dynamic>{"value":"Names are unique within your workspace."},
            <dynamic>[]
          )
        ]),
        view.node("ui/card-content",<dynamic, dynamic>{"class":"flex flex-col gap-4 p-0"},<dynamic>[
          view.node("ui/input",<dynamic, dynamic>{
            "id":"sink-name",
            "value":snapshot["name"] ?? "",
            "placeholder":"Acme Trading",
            "on_change":view.action("demo/set-name",view.event_value(<dynamic>["value"]))
          },<dynamic>[]),
          view.node("ui/textarea",<dynamic, dynamic>{
            "placeholder":"What does this organisation trade?",
            "rows":3,
            "on_change":view.action("demo/set-about",view.event_value(<dynamic>["value"]))
          },<dynamic>[])
        ]),
        view.node("ui/card-footer",<dynamic, dynamic>{"class":"flex flex-row justify-end gap-2 p-0"},<dynamic>[
          view.node(
            "ui/button",
            <dynamic, dynamic>{"variant":"ghost"},
            <dynamic>["Cancel"]
          ),
          view.node("ui/button",<dynamic, dynamic>{
            "pending":pending,
            "on_press":view.action("demo/create-org",null)
          },<dynamic>["Create"])
        ])
      ])
    ]),
    sink_section("Image",<dynamic>[
      view.node(
        "ui/image",
        <dynamic, dynamic>{"src":"/demo.png","alt":"Demo","class":"h-16 w-16 rounded"},
        <dynamic>[]
      )
    ]),
    sink_section("Table",<dynamic>[
      view.node("ui/table",<dynamic, dynamic>{"class":"w-full text-sm"},<dynamic>[
        view.node("ui/table-header",<dynamic, dynamic>{},<dynamic>[
          view.node("ui/table-row",<dynamic, dynamic>{},<dynamic>[
            view.node(
              "ui/table-head",
              <dynamic, dynamic>{"value":"Time"},
              <dynamic>[]
            ),
            view.node(
              "ui/table-head",
              <dynamic, dynamic>{"value":"Side"},
              <dynamic>[]
            ),
            view.node(
              "ui/table-head",
              <dynamic, dynamic>{"value":"Price"},
              <dynamic>[]
            ),
            view.node(
              "ui/table-head",
              <dynamic, dynamic>{"value":"Size"},
              <dynamic>[]
            )
          ])
        ]),
        view.node("ui/table-body",<dynamic, dynamic>{},xt.lang.common_data.arr_map(fills,(fill) {
          return view.node("ui/table-row",<dynamic, dynamic>{},<dynamic>[
            view.node(
                "ui/table-cell",
                <dynamic, dynamic>{"value":fill[0]},
                <dynamic>[]
              ),
            view.node(
                "ui/table-cell",
                <dynamic, dynamic>{"value":fill[1]},
                <dynamic>[]
              ),
            view.node(
                "ui/table-cell",
                <dynamic, dynamic>{"value":fill[2]},
                <dynamic>[]
              ),
            view.node(
                "ui/table-cell",
                <dynamic, dynamic>{"value":fill[3]},
                <dynamic>[]
              )
          ]);
        }))
      ])
    ]),
    sink_section("Scroll",<dynamic>[
      view.node("ui/scroll",<dynamic, dynamic>{
        "class":"h-24 overflow-auto rounded border border-slate-200 p-2"
      },<dynamic>[
        view.node("ui/column",<dynamic, dynamic>{"class":"flex flex-col gap-1"},<dynamic>[
          view.node("ui/text",<dynamic, dynamic>{"value":"row 1"},<dynamic>[]),
          view.node("ui/text",<dynamic, dynamic>{"value":"row 2"},<dynamic>[]),
          view.node("ui/text",<dynamic, dynamic>{"value":"row 3"},<dynamic>[]),
          view.node("ui/text",<dynamic, dynamic>{"value":"row 4"},<dynamic>[]),
          view.node("ui/text",<dynamic, dynamic>{"value":"row 5"},<dynamic>[]),
          view.node("ui/text",<dynamic, dynamic>{"value":"row 6"},<dynamic>[])
        ])
      ])
    ]),
    view.node("ui/fragment",<dynamic, dynamic>{},<dynamic>[
      view.node(
        "ui/separator",
        <dynamic, dynamic>{"class":"bg-slate-200"},
        <dynamic>[]
      ),
      view.node("ui/description",<dynamic, dynamic>{
        "value":"Platform-only figma components live in demo/web-escape."
      },<dynamic>[])
    ])
  ]);
}

kitchen_sink_spec() {
  return view.view_spec(
    "demo/kitchen-sink",
    <dynamic, dynamic>{},
    kitchen_sink_render(<dynamic, dynamic>{})
  );
}

web_escape_render(_snapshot) {
  return view.node("ui/column",<dynamic, dynamic>{"class":"flex flex-col gap-4 p-6"},<dynamic>[
    view.node(
      "ui/title",
      <dynamic, dynamic>{"value":"Web-only components"},
      <dynamic>[]
    ),
    view.node("ui/description",<dynamic, dynamic>{
      "value":"These lower straight to @xtalk/figma-ui; the Wind backend rejects them."
    },<dynamic>[]),
    view.node("fg/hover-card",<dynamic, dynamic>{},<dynamic>[
      view.node("fg/hover-card-trigger",<dynamic, dynamic>{},<dynamic>[
        view.node(
          "ui/button",
          <dynamic, dynamic>{"variant":"link"},
          <dynamic>["Hover me"]
        )
      ]),
      view.node("fg/hover-card-content",<dynamic, dynamic>{},<dynamic>[
        view.node(
          "ui/text",
          <dynamic, dynamic>{"value":"Rendered by Radix on web"},
          <dynamic>[]
        )
      ])
    ]),
    view.node("fg/progress",<dynamic, dynamic>{"class":"w-1/2"},<dynamic>[])
  ]);
}

web_escape_spec() {
  return view.view_spec(
    "demo/web-escape",
    <dynamic, dynamic>{},
    web_escape_render(<dynamic, dynamic>{})
  );
}