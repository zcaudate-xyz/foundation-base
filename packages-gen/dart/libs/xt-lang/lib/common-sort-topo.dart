import 'package:xtalk_lang/common-data.dart' as xtd;


sort_edges_build(nodes, edge) {
  var n_from = edge[0];
  var n_to = edge[1];
  if(!nodes.containsKey(n_from)){
    nodes[n_from] = <dynamic, dynamic>{"id":n_from,"links":<dynamic>[]};
  }
  if(!nodes.containsKey(n_to)){
    nodes[n_to] = <dynamic, dynamic>{"id":n_to,"links":<dynamic>[]};
  }
  var links = nodes[n_from]["links"];
  return links.add(n_to);
}

sort_edges_visit(nodes, visited, sorted, id, ancestors) {
  if(true == visited[id]){
    return;
  }
  var node = nodes[id];
  if(null == node){
    throw "Not available: " + id;
  }
  if(null == ancestors){
    ancestors = <dynamic>[];
  }
  ancestors.add(id);
  visited[id] = true;
  var input = node["links"];
  var arr_49184 = input;
  for(var i49185 = 0; i49185 < arr_49184.length; ++i49185){
    var link = arr_49184[i49185];
    sort_edges_visit(nodes,visited,sorted,link,xtd.arr_clone(ancestors));
  };
  return sorted.insert(0,id);
}

sort_edges(edges) {
  var nodes = <dynamic, dynamic>{};
  var sorted = <dynamic>[];
  var visited = <dynamic, dynamic>{};
  var arr_49206 = edges;
  for(var i49207 = 0; i49207 < arr_49206.length; ++i49207){
    var e = arr_49206[i49207];
    sort_edges_build(nodes,e);
  };
  for(var id in nodes.keys){
    sort_edges_visit(nodes,visited,sorted,id,null);
  };
  return sorted;
}

sort_topo(input) {
  var edges = <dynamic>[];
  var arr_49228 = input;
  for(var i49229 = 0; i49229 < arr_49228.length; ++i49229){
    var link = arr_49228[i49229];
    var root = link[0];
    var deps = link[1];
    var arr_49250 = deps;
    for(var i49251 = 0; i49251 < arr_49250.length; ++i49251){
      var d = arr_49250[i49251];
      edges.add(<dynamic>[root,d]);
    };
  };
  var sorted = sort_edges(edges);
  var out = <dynamic>[];
  for(var i = sorted.length; i > 0; i = (i + -1)){
    out.add(sorted[i + -1]);
  };
  return out;
}