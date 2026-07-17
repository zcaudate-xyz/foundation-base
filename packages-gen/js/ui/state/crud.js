const form = require("@xtalk/ui/state/form.js")

const collection = require("@xtalk/ui/state/collection.js")

function spec(id,models,fields,columns,actions,opts){
  return {
    "id":id,
    "strategy":"page_controller",
    "models":models || {},
    "fields":fields || [],
    "columns":columns || [],
    "actions":actions || [],
    "opts":opts || {}
  };
}

function create_state(crud_spec,values){
  return {
    "status":"idle",
    "spec":crud_spec,
    "collection":collection.create({}),
    "form":form.create(values || {},{}),
    "record":null,
    "mode":"list",
    "errors":{}
  };
}

function set_modef(state,mode,record){
  state["mode"] = mode;
  state["record"] = record;
  return state;
}

module.exports = {
  ["spec"]:spec,
  ["create_state"]:create_state,
  ["set_modef"]:set_modef
}