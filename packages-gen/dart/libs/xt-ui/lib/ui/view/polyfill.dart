import 'package:xtalk_substrate/view-catalog.dart' as catalog;
import 'package:xtalk_substrate/view.dart' as view;



lower_to(component_id) {
  var lowering = (node) {
    return view.node(component_id,node["props"],node["children"]);
  };
  return lowering;
}

lower_variant(source_id, target_id) {
  var lowering = (node) {
    var props = xt.lang.common_data.obj_clone(node["props"] ?? <dynamic, dynamic>{});
    var classes = catalog.variant_classes(source_id,props["variant"]);
    if(null != classes){
      var class_name = props["class"] ?? "";
      if(0 < class_name.length){
        class_name = (class_name + " ");
      }
      props["class"] = (class_name + classes);
    }
    props.remove("variant");
    return view.node(target_id,props,node["children"]);
  };
  return lowering;
}

registry() {
  return <dynamic, dynamic>{
    "ui/card-content":lower_to("ui/column"),
    "ui/textarea":(node) {
        var props = xt.lang.common_data.obj_clone(node["props"]);
        props["maxLines"] = (props["rows"] ?? 4);
        props.remove("rows");
        return view.node("ui/input",props,<dynamic>[]);
      },
    "ui/card-description":lower_to("ui/text"),
    "ui/separator":lower_to("ui/row"),
    "ui/badge":lower_variant("ui/badge","ui/text"),
    "ui/card-title":lower_to("ui/text"),
    "ui/card-footer":lower_to("ui/column"),
    "ui/table-row":lower_to("ui/row"),
    "ui/table":lower_to("ui/column"),
    "ui/table-body":lower_to("ui/column"),
    "ui/card":lower_to("ui/column"),
    "ui/table-header":lower_to("ui/column"),
    "ui/table-cell":lower_to("ui/text"),
    "ui/table-head":lower_to("ui/text"),
    "ui/card-header":lower_to("ui/column")
  };
}