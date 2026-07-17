get_char(s, i) {
  return s.codeUnitAt(i - 0);
}

split(s, tok) {
  return s.split(tok);
}

join(s, arr) {
  return arr.join(s);
}

replace(s, tok, replacement) {
  return s.replaceAll(tok,replacement);
}

index_of(s, tok) {
  return (s.indexOf(tok,0) + 0) - 0;
}

substring(s, start, finish) {
  return s.substring(start - 0,finish);
}

to_uppercase(s) {
  return (s).toUpperCase();
}

to_lowercase(s) {
  return (s).toLowerCase();
}

to_fixed(n, digits) {
  return n.toStringAsFixed(digits);
}

trim(s) {
  return (s).trim();
}

trim_left(s) {
  return s.trimLeft();
}

trim_right(s) {
  return s.trimRight();
}

sym_full(ns, name) {
  if(null == ns){
    return name;
  }
  else{
    return ns + "/" + name;
  }
}

sym_name(sym) {
  var idx = sym.indexOf("/",0) + 0;
  if(0 < idx){
    return (sym.split("/"))[sym.split("/").length + -1];
  }
  else{
    return sym;
  }
}

sym_ns(sym) {
  var idx = sym.indexOf("/",0) + 0;
  if(0 < idx){
    return (sym.split("/"))[0];
  }
  else{
    return null;
  }
}

sym_pair(sym) {
  return <dynamic>[sym_ns(sym),sym_name(sym)];
}

starts_withp(s, match) {
  if(match.length > s.length){
    return false;
  }
  return s.substring(0 - 0,match.length) == match;
}

ends_withp(s, match) {
  if(match.length > s.length){
    return false;
  }
  return match == s.substring((s.length - match.length) - 0,s.length);
}

capitalize(s) {
  return (s.substring(0 - 0,1)).toUpperCase() + s.substring(1 - 0);
}

decapitalize(s) {
  return (s.substring(0 - 0,1)).toLowerCase() + s.substring(1 - 0);
}

pad_left(s, n, ch) {
  var l = n - s.length;
  var out = s;
  for(var i = 0; i < l; i = (i + 1)){
    out = (ch + out);
  };
  return out;
}

pad_right(s, n, ch) {
  var l = n - s.length;
  var out = s;
  for(var i = 0; i < l; i = (i + 1)){
    out = (out + ch);
  };
  return out;
}

pad_lines(s, n, ch) {
  var lines = s.split("\n");
  var out = "";
  var arr_40266 = lines;
  for(var i40267 = 0; i40267 < arr_40266.length; ++i40267){
    var line = arr_40266[i40267];
    if(0 < out.length){
      out = (out + "\n");
    }
    out = (out + pad_left("",n," ") + line);
  };
  return out;
}

split_long(s, line_len) {
  if((null == s) || (0 == s.length)){
    return <dynamic>[];
  }
  line_len = ((null == line_len) ? 50 : line_len);
  var total = s.length;
  var lines = (total / line_len).ceil();
  var out = <dynamic>[];
  for(var i = 0; i < lines; i = (i + 1)){
    var line = substring(s,i * line_len,(i + 1) * line_len);
    if(0 < line.length){
      out.add(line);
    }
  };
  return out;
}

str_rand(n) {
  var choices = <dynamic>[
    "A",
    "B",
    "C",
    "D",
    "E",
    "F",
    "G",
    "H",
    "I",
    "J",
    "K",
    "L",
    "M",
    "N",
    "O",
    "P",
    "Q",
    "R",
    "S",
    "T",
    "U",
    "V",
    "W",
    "X",
    "Y",
    "Z",
    "a",
    "b",
    "c",
    "d",
    "e",
    "f",
    "g",
    "h",
    "i",
    "j",
    "k",
    "l",
    "m",
    "n",
    "o",
    "p",
    "q",
    "r",
    "s",
    "t",
    "u",
    "v",
    "w",
    "x",
    "y",
    "z",
    "0",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8"
  ];
  var out = "";
  for(var i = 0; i < n; i = (i + 1)){
    var rand_idx = (math.Random()).nextDouble() * choices.length;
    var idx = (rand_idx).floor();
    out = (out + choices[idx]);
  };
  return out;
}

tag_string(tag) {
  var value_40288 = sym_pair(tag);
  var ns = value_40288[0];
  var name = value_40288[1];
  var parts = ((null == ns) ? "" : ns).split(".");
  var part_count = parts.length;
  var desc = (null != ns) ? (parts[part_count + -1] + " ") : "";
  var clean_name = ((null == name) ? "" : name).replaceAll("_"," ");
  clean_name = clean_name.replaceAll("-"," ");
  clean_name = clean_name.replaceAll((desc).trim(),"");
  return desc + clean_name;
}