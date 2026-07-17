import 'package:xtalk_lang/common-math.dart' as xtm;

import 'package:xtalk_lang/common-string.dart' as xts;

var HEX = <dynamic, dynamic>{
  "0":0,
  "1":1,
  "2":2,
  "3":3,
  "4":4,
  "5":5,
  "6":6,
  "7":7,
  "8":8,
  "9":9,
  "A":10,
  "B":11,
  "C":12,
  "D":13,
  "E":14,
  "F":15
};

var LU = <dynamic, dynamic>{
  0:"0",
  1:"1",
  2:"2",
  3:"3",
  4:"4",
  5:"5",
  6:"6",
  7:"7",
  8:"8",
  9:"9",
  10:"A",
  11:"B",
  12:"C",
  13:"D",
  14:"E",
  15:"F"
};

var NAMED = <dynamic, dynamic>{
  "aliceblue":<dynamic>[240,248,255],
  "antiquewhite":<dynamic>[250,235,215],
  "aqua":<dynamic>[0,255,255],
  "aquamarine":<dynamic>[127,255,212],
  "azure":<dynamic>[240,255,255],
  "beige":<dynamic>[245,245,220],
  "bisque":<dynamic>[255,228,196],
  "black":<dynamic>[0,0,0],
  "blanchedalmond":<dynamic>[255,235,205],
  "blue":<dynamic>[0,0,255],
  "blueviolet":<dynamic>[138,43,226],
  "brown":<dynamic>[165,42,42],
  "burlywood":<dynamic>[222,184,135],
  "cadetblue":<dynamic>[95,158,160],
  "chartreuse":<dynamic>[127,255,0],
  "chocolate":<dynamic>[210,105,30],
  "coral":<dynamic>[255,127,80],
  "cornflowerblue":<dynamic>[100,149,237],
  "cornsilk":<dynamic>[255,248,220],
  "crimson":<dynamic>[220,20,60],
  "cyan":<dynamic>[0,255,255],
  "darkblue":<dynamic>[0,0,139],
  "darkcyan":<dynamic>[0,139,139],
  "darkgoldenrod":<dynamic>[184,134,11],
  "darkgray":<dynamic>[169,169,169],
  "darkgreen":<dynamic>[0,100,0],
  "darkgrey":<dynamic>[169,169,169],
  "darkkhaki":<dynamic>[189,183,107],
  "darkmagenta":<dynamic>[139,0,139],
  "darkolivegreen":<dynamic>[85,107,47],
  "darkorange":<dynamic>[255,140,0],
  "darkorchid":<dynamic>[153,50,204],
  "darkred":<dynamic>[139,0,0],
  "darksalmon":<dynamic>[233,150,122],
  "darkseagreen":<dynamic>[143,188,143],
  "darkslateblue":<dynamic>[72,61,139],
  "darkslategray":<dynamic>[47,79,79],
  "darkslategrey":<dynamic>[47,79,79],
  "darkturquoise":<dynamic>[0,206,209],
  "darkviolet":<dynamic>[148,0,211],
  "deeppink":<dynamic>[255,20,147],
  "deepskyblue":<dynamic>[0,191,255],
  "dimgray":<dynamic>[105,105,105],
  "dimgrey":<dynamic>[105,105,105],
  "dodgerblue":<dynamic>[30,144,255],
  "firebrick":<dynamic>[178,34,34],
  "floralwhite":<dynamic>[255,250,240],
  "forestgreen":<dynamic>[34,139,34],
  "fuchsia":<dynamic>[255,0,255],
  "gainsboro":<dynamic>[220,220,220],
  "ghostwhite":<dynamic>[248,248,255],
  "gold":<dynamic>[255,215,0],
  "goldenrod":<dynamic>[218,165,32],
  "gray":<dynamic>[128,128,128],
  "green":<dynamic>[0,128,0],
  "greenyellow":<dynamic>[173,255,47],
  "grey":<dynamic>[128,128,128],
  "honeydew":<dynamic>[240,255,240],
  "hotpink":<dynamic>[255,105,180],
  "indianred":<dynamic>[205,92,92],
  "indigo":<dynamic>[75,0,130],
  "ivory":<dynamic>[255,255,240],
  "khaki":<dynamic>[240,230,140],
  "lavender":<dynamic>[230,230,250],
  "lavenderblush":<dynamic>[255,240,245],
  "lawngreen":<dynamic>[124,252,0],
  "lemonchiffon":<dynamic>[255,250,205],
  "lightblue":<dynamic>[173,216,230],
  "lightcoral":<dynamic>[240,128,128],
  "lightcyan":<dynamic>[224,255,255],
  "lightgoldenrodyellow":<dynamic>[250,250,210],
  "lightgray":<dynamic>[211,211,211],
  "lightgreen":<dynamic>[144,238,144],
  "lightgrey":<dynamic>[211,211,211],
  "lightpink":<dynamic>[255,182,193],
  "lightsalmon":<dynamic>[255,160,122],
  "lightseagreen":<dynamic>[32,178,170],
  "lightskyblue":<dynamic>[135,206,250],
  "lightslategray":<dynamic>[119,136,153],
  "lightslategrey":<dynamic>[119,136,153],
  "lightsteelblue":<dynamic>[176,196,222],
  "lightyellow":<dynamic>[255,255,224],
  "lime":<dynamic>[0,255,0],
  "limegreen":<dynamic>[50,205,50],
  "linen":<dynamic>[250,240,230],
  "magenta":<dynamic>[255,0,255],
  "maroon":<dynamic>[128,0,0],
  "mediumaquamarine":<dynamic>[102,205,170],
  "mediumblue":<dynamic>[0,0,205],
  "mediumorchid":<dynamic>[186,85,211],
  "mediumpurple":<dynamic>[147,112,219],
  "mediumseagreen":<dynamic>[60,179,113],
  "mediumslateblue":<dynamic>[123,104,238],
  "mediumspringgreen":<dynamic>[0,250,154],
  "mediumturquoise":<dynamic>[72,209,204],
  "mediumvioletred":<dynamic>[199,21,133],
  "midnightblue":<dynamic>[25,25,112],
  "mintcream":<dynamic>[245,255,250],
  "mistyrose":<dynamic>[255,228,225],
  "moccasin":<dynamic>[255,228,181],
  "navajowhite":<dynamic>[255,222,173],
  "navy":<dynamic>[0,0,128],
  "oldlace":<dynamic>[253,245,230],
  "olive":<dynamic>[128,128,0],
  "olivedrab":<dynamic>[107,142,35],
  "orange":<dynamic>[255,165,0],
  "orangered":<dynamic>[255,69,0],
  "orchid":<dynamic>[218,112,214],
  "palegoldenrod":<dynamic>[238,232,170],
  "palegreen":<dynamic>[152,251,152],
  "paleturquoise":<dynamic>[175,238,238],
  "palevioletred":<dynamic>[219,112,147],
  "papayawhip":<dynamic>[255,239,213],
  "peachpuff":<dynamic>[255,218,185],
  "peru":<dynamic>[205,133,63],
  "pink":<dynamic>[255,192,203],
  "plum":<dynamic>[221,160,221],
  "powderblue":<dynamic>[176,224,230],
  "purple":<dynamic>[128,0,128],
  "rebeccapurple":<dynamic>[102,51,153],
  "red":<dynamic>[255,0,0],
  "rosybrown":<dynamic>[188,143,143],
  "royalblue":<dynamic>[65,105,225],
  "saddlebrown":<dynamic>[139,69,19],
  "salmon":<dynamic>[250,128,114],
  "sandybrown":<dynamic>[244,164,96],
  "seagreen":<dynamic>[46,139,87],
  "seashell":<dynamic>[255,245,238],
  "sienna":<dynamic>[160,82,45],
  "silver":<dynamic>[192,192,192],
  "skyblue":<dynamic>[135,206,235],
  "slateblue":<dynamic>[106,90,205],
  "slategray":<dynamic>[112,128,144],
  "slategrey":<dynamic>[112,128,144],
  "snow":<dynamic>[255,250,250],
  "springgreen":<dynamic>[0,255,127],
  "steelblue":<dynamic>[70,130,180],
  "tan":<dynamic>[210,180,140],
  "teal":<dynamic>[0,128,128],
  "thistle":<dynamic>[216,191,216],
  "tomato":<dynamic>[255,99,71],
  "transparent":<dynamic>[0,0,0],
  "turquoise":<dynamic>[64,224,208],
  "violet":<dynamic>[238,130,238],
  "wheat":<dynamic>[245,222,179],
  "white":<dynamic>[255,255,255],
  "whitesmoke":<dynamic>[245,245,245],
  "yellow":<dynamic>[255,255,0],
  "yellowgreen":<dynamic>[154,205,50]
};

named__gtrgb(s) {
  return (null == NAMED[s]) ? <dynamic>[0,0,0] : NAMED[s];
}

hex__gtn(s) {
  var out = HEX[xts.to_uppercase(s)];
  if(null == out){
    out = 0;
  }
  return out;
}

n__gthex(n) {
  var v1 = n ~/ 16;
  var v0 = xtm.mod_pos(n,16);
  return ((null == LU[v1]) ? "0" : LU[v1]) + ((null == LU[v0]) ? "0" : LU[v0]);
}

hex__gtrgb(s) {
  var val_fn = (s, start, finish) {
    return hex__gtn(xts.substring(s,start,finish));
  };
  if(!(() {
    var dart_truthy__39636 = xts.starts_withp(s,"#");
    return (null != dart_truthy__39636) && (false != dart_truthy__39636);
  })()){
    throw "Not a valid hex color.";
  }
  if(4 == s.length){
    var r = Function.apply((val_fn as Function),<dynamic>[s,1,2]);
    var g = Function.apply((val_fn as Function),<dynamic>[s,2,3]);
    var b = Function.apply((val_fn as Function),<dynamic>[s,3,4]);
    return <dynamic>[(r * 16) + r,(g * 16) + g,(b * 16) + b];
  }
  if(7 == s.length){
    var r1 = Function.apply((val_fn as Function),<dynamic>[s,1,2]);
    var r0 = Function.apply((val_fn as Function),<dynamic>[s,2,3]);
    var g1 = Function.apply((val_fn as Function),<dynamic>[s,3,4]);
    var g0 = Function.apply((val_fn as Function),<dynamic>[s,4,5]);
    var b1 = Function.apply((val_fn as Function),<dynamic>[s,5,6]);
    var b0 = Function.apply((val_fn as Function),<dynamic>[s,6,7]);
    return <dynamic>[(r1 * 16) + r0,(g1 * 16) + g0,(b1 * 16) + b0];
  }
  return <dynamic>[0,0,0];
}

rgb__gthex(rgb) {
  var value_39637 = rgb;
  var r = value_39637[0];
  var g = value_39637[1];
  var b = value_39637[2];
  return "#" + n__gthex(r) + n__gthex(g) + n__gthex(b);
}

rgb__gthue(r, g, b, value, delta, fallback) {
  if(0 == delta){
    return fallback;
  }
  var segment;
  if(value == r){
    segment = ((g - b) / delta);
  }
  else if(value == g){
    segment = ((b - r) / delta);
  }
  else{
    segment = ((r - g) / delta);
  }
  var shift;
  if(value == r){
    if(segment < 0){
      shift = 6;
    }
    else{
      shift = 0;
    }
  }
  else if(value == g){
    shift = 2;
  }
  else{
    shift = 4;
  }
  return 60 * (segment + shift);
}

rgb__gthsl(rgb, fallback) {
  var value_39638 = rgb;
  var r = value_39638[0];
  var g = value_39638[1];
  var b = value_39638[2];
  var value = math.max(math.max(r,g),b);
  var whiteness = math.min(math.min(r,g),b);
  var delta = value - whiteness;
  if(null == fallback){
    fallback = 0;
  }
  var h = rgb__gthue(r,g,b,value,delta,fallback);
  var l = (100 * (value + whiteness) * 0.5) / 255;
  var s = null;
  if(delta == 0){
    s = 0;
  }
  else{
    s = (((delta / 255) * 10000) / (100 - ((2 * l) - 100).abs()));
  }
  return <dynamic>[h,math.min(100,s),math.min(100,l)];
}

hue__gtv(t1, t2, h) {
  if(h < 0){
    h = (h + 1);
  }
  else if(h > 1){
    h = (h - 1);
  }
  if((6 * h) < 1){
    return t1 + ((t2 - t1) * 6 * h);
  }
  else if((2 * h) < 1){
    return t2;
  }
  else if((3 * h) < 2){
    return t1 + ((t2 - t1) * 6 * ((2 / 3) - h));
  }
  else{
    return t1;
  }
}

hsl__gtrgb(hsl) {
  var value_39639 = hsl;
  var hi = value_39639[0];
  var si = value_39639[1];
  var li = value_39639[2];
  var h = hi / 360;
  var s = si / 100;
  var l = li / 100;
  if(s == 0){
    return <dynamic>[xtm.round(255 * l),xtm.round(255 * l),xtm.round(255 * l)];
  }
  else{
    var t2 = (l < 0.5) ? (l * (s + 1)) : ((l + s) - (s * l));
    var t1 = (2 * l) - t2;
    return <dynamic>[
      xtm.round(255 * hue__gtv(t1,t2,h + (1 / 3))),
      xtm.round(255 * hue__gtv(t1,t2,h)),
      xtm.round(255 * hue__gtv(t1,t2,h - (1 / 3)))
    ];
  }
}

named__gthsl(s) {
  return rgb__gthsl(named__gtrgb(s),null);
}

named__gthex(s) {
  return rgb__gthex(named__gtrgb(s));
}

hex__gthsl(s) {
  return rgb__gthsl(hex__gtrgb(s),null);
}