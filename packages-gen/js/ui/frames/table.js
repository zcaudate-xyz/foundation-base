const ui = require("@xtalk/ui/core.js")

function view(frame,state,actions){
  let collection = state["collection"] || state;
  let columns = (frame["opts"])["columns"] || [];
  let header = [];
  for(let column of columns){
    header.push(
      ui.node("ui/table-cell",{"value":column["label"] || column["id"]},[])
    );
  };
  let rows = [];
  for(let item of collection["items"] || []){
    let cells = [];
    for(let column of columns){
      cells.push(ui.node("ui/table-cell",{"value":item[column["id"]]},[]));
    };
    rows.push(ui.node("ui/table-row",{"key":item["id"]},cells));
  };
  return ui.node("ui/column",{"class":"gap-4"},[
    ui.slot(frame["id"] + "/toolbar",[],{}),
    ui.node("ui/table",{"class":"w-full"},[
      ui.node("ui/table-header",{},header),
      ui.node("ui/table-body",{},rows)
    ])
  ]);
}

module.exports = {["view"]:view}