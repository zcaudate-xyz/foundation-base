import 'package:xtalk_ui/state/form.dart' as form;
import 'package:xtalk_ui/state/collection.dart' as collection;



spec(id, models, fields, columns, actions, opts) {
  return <dynamic, dynamic>{
    "id":id,
    "strategy":"page_controller",
    "models":models ?? <dynamic, dynamic>{},
    "fields":fields ?? <dynamic>[],
    "columns":columns ?? <dynamic>[],
    "actions":actions ?? <dynamic>[],
    "opts":opts ?? <dynamic, dynamic>{}
  };
}

create_state(crud_spec, values) {
  return <dynamic, dynamic>{
    "status":"idle",
    "spec":crud_spec,
    "collection":collection.create(<dynamic, dynamic>{}),
    "form":form.create(values ?? <dynamic, dynamic>{},<dynamic, dynamic>{}),
    "record":null,
    "mode":"list",
    "errors":<dynamic, dynamic>{}
  };
}

set_modef(state, mode, record) {
  state["mode"] = mode;
  state["record"] = record;
  return state;
}