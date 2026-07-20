const view = require("@xtalk/substrate/view.js")

function sink_section(title,children){
  return view.node("ui/column",{"class":"flex flex-col gap-3"},[
    view.node("ui/label",{
      "value":title,
      "class":"text-xs font-semibold uppercase tracking-wide text-slate-400"
    },[]),
    children
  ]);
}

function collect_ids(value,out){
  if(Array.isArray(value)){
    for(let item of value){
      collect_ids(item,out);
    };
  }
  else if((null != value) && ("object" == (typeof value)) && !Array.isArray(value)){
    out.push(value["component"]);
    collect_ids(value["children"] || [],out);
  }
  else{
    null;
  }
  return out;
}

function has_valp(arr,value){
  let found = false;
  for(let item of arr){
    if(item == value){
      found = true;
    }
  };
  return found;
}

function kitchen_sink_render(snapshot){
  let pending = true == snapshot["pending"];
  let fills = snapshot["fills"] || [
    ["10:02","BUY","12.50","4"],
    ["10:05","SELL","12.55","2"],
    ["10:11","BUY","12.45","6"]
  ];
  return view.node("ui/column",{"class":"flex flex-col gap-8 p-8 max-w-3xl"},[
    view.node("ui/row",{"class":"flex flex-row items-center justify-between"},[
      view.node("ui/column",{"class":"flex flex-col gap-1"},[
        view.node("ui/title",{"value":"Kitchen sink"},[]),
        view.node("ui/description",{
          "value":"Every portable ui/ component in one serializable tree."
        },[])
      ]),
      view.node("ui/badge",{"value":"v1","variant":"secondary"},[])
    ]),
    view.node("ui/separator",{"class":"bg-slate-200"},[]),
    sink_section("Text",[
      view.node(
        "ui/text",
        {"value":"Body text - span on web, WText in Wind."},
        []
      ),
      view.node("ui/label",{"value":"Inline label","for":"sink-name"},[]),
      view.node("ui/row",{"class":"flex flex-row items-center gap-2"},[
        view.node("ui/icon",{"value":"info","aria_label":"Info"},[]),
        view.node("ui/spinner",{"value":"Loading…"},[])
      ]),
      view.node(
        "ui/text",
        {"value":"you should not see this","hidden":true},
        []
      )
    ]),
    sink_section("Badges",[
      view.node("ui/row",{"class":"flex flex-row gap-2"},[
        view.node("ui/badge",{"value":"default"},[]),
        view.node("ui/badge",{"value":"secondary","variant":"secondary"},[]),
        view.node("ui/badge",{"value":"outline","variant":"outline"},[]),
        view.node("ui/badge",{"value":"destructive","variant":"destructive"},[])
      ])
    ]),
    sink_section("Buttons",[
      view.node("ui/row",{"class":"flex flex-row flex-wrap gap-2"},[
        view.node("ui/button",{},["Default"]),
        view.node("ui/button",{"variant":"secondary"},["Secondary"]),
        view.node("ui/button",{"variant":"outline"},["Outline"]),
        view.node("ui/button",{"variant":"ghost"},["Ghost"]),
        view.node("ui/button",{"variant":"destructive"},["Delete"]),
        view.node("ui/button",{"variant":"link"},["Link"]),
        view.node("ui/button",{"size":"sm"},["Small"]),
        view.node("ui/button",{"disabled":true},["Disabled"]),
        view.node("ui/button",{"pending":pending},["Saving"]),
        view.node("ui/button",{
          "variant":"default",
          "on_press":view.action("demo/sink-press",null)
        },["Fire action"])
      ])
    ]),
    sink_section("Alerts",[
      view.node(
        "ui/alert",
        {"variant":"default"},
        [view.node("ui/text",{"value":"Default alert body"},[])]
      ),
      view.node(
        "ui/alert",
        {"variant":"destructive"},
        [view.node("ui/text",{"value":"Destructive alert body"},[])]
      )
    ]),
    sink_section("Form",[
      view.node("ui/card",{"class":"flex flex-col gap-4 p-6"},[
        view.node("ui/card-header",{"class":"flex flex-col gap-1 p-0"},[
          view.node("ui/card-title",{"value":"Create organisation"},[]),
          view.node(
            "ui/card-description",
            {"value":"Names are unique within your workspace."},
            []
          )
        ]),
        view.node("ui/card-content",{"class":"flex flex-col gap-4 p-0"},[
          view.node("ui/input",{
            "id":"sink-name",
            "value":snapshot["name"] || "",
            "placeholder":"Acme Trading",
            "on_change":view.action("demo/set-name",view.event_value(["value"]))
          },[]),
          view.node("ui/textarea",{
            "placeholder":"What does this organisation trade?",
            "rows":3,
            "on_change":view.action("demo/set-about",view.event_value(["value"]))
          },[])
        ]),
        view.node("ui/card-footer",{"class":"flex flex-row justify-end gap-2 p-0"},[
          view.node("ui/button",{"variant":"ghost"},["Cancel"]),
          view.node("ui/button",{
            "pending":pending,
            "on_press":view.action("demo/create-org",null)
          },["Create"])
        ])
      ])
    ]),
    sink_section("Image",[
      view.node(
        "ui/image",
        {"src":"/demo.png","alt":"Demo","class":"h-16 w-16 rounded"},
        []
      )
    ]),
    sink_section("Table",[
      view.node("ui/table",{"class":"w-full text-sm"},[
        view.node("ui/table-header",{},[
          view.node("ui/table-row",{},[
            view.node("ui/table-head",{"value":"Time"},[]),
            view.node("ui/table-head",{"value":"Side"},[]),
            view.node("ui/table-head",{"value":"Price"},[]),
            view.node("ui/table-head",{"value":"Size"},[])
          ])
        ]),
        view.node("ui/table-body",{},fills.map(function (fill){
          return view.node("ui/table-row",{},[
            view.node("ui/table-cell",{"value":fill[0]},[]),
            view.node("ui/table-cell",{"value":fill[1]},[]),
            view.node("ui/table-cell",{"value":fill[2]},[]),
            view.node("ui/table-cell",{"value":fill[3]},[])
          ]);
        }))
      ])
    ]),
    sink_section("Scroll",[
      view.node("ui/scroll",{
        "class":"h-24 overflow-auto rounded border border-slate-200 p-2"
      },[
        view.node("ui/column",{"class":"flex flex-col gap-1"},[
          view.node("ui/text",{"value":"row 1"},[]),
          view.node("ui/text",{"value":"row 2"},[]),
          view.node("ui/text",{"value":"row 3"},[]),
          view.node("ui/text",{"value":"row 4"},[]),
          view.node("ui/text",{"value":"row 5"},[]),
          view.node("ui/text",{"value":"row 6"},[])
        ])
      ])
    ]),
    view.node("ui/fragment",{},[
      view.node("ui/separator",{"class":"bg-slate-200"},[]),
      view.node("ui/description",{
        "value":"Platform-only figma components live in demo/web-escape."
      },[])
    ])
  ]);
}

function kitchen_sink_spec(){
  return view.view_spec("demo/kitchen-sink",{},kitchen_sink_render({}));
}

function web_escape_render(_snapshot){
  return view.node("ui/column",{"class":"flex flex-col gap-4 p-6"},[
    view.node("ui/title",{"value":"Web-only components"},[]),
    view.node("ui/description",{
      "value":"These lower straight to @xtalk/figma-ui; the Wind backend rejects them."
    },[]),
    view.node("fg/hover-card",{},[
      view.node(
        "fg/hover-card-trigger",
        {},
        [view.node("ui/button",{"variant":"link"},["Hover me"])]
      ),
      view.node("fg/hover-card-content",{},[
        view.node("ui/text",{"value":"Rendered by Radix on web"},[])
      ])
    ]),
    view.node("fg/progress",{"class":"w-1/2"},[])
  ]);
}

function web_escape_spec(){
  return view.view_spec("demo/web-escape",{},web_escape_render({}));
}

module.exports = {
  ["sink_section"]:sink_section,
  ["collect_ids"]:collect_ids,
  ["has_valp"]:has_valp,
  ["kitchen_sink_render"]:kitchen_sink_render,
  ["kitchen_sink_spec"]:kitchen_sink_spec,
  ["web_escape_render"]:web_escape_render,
  ["web_escape_spec"]:web_escape_spec
}