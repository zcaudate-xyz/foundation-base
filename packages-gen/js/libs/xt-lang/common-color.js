const xtm = require("@xtalk/lang/common-math.js")

const xts = require("@xtalk/lang/common-string.js")

var HEX = {
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

var LU = {
  [0]:"0",
  [1]:"1",
  [2]:"2",
  [3]:"3",
  [4]:"4",
  [5]:"5",
  [6]:"6",
  [7]:"7",
  [8]:"8",
  [9]:"9",
  [10]:"A",
  [11]:"B",
  [12]:"C",
  [13]:"D",
  [14]:"E",
  [15]:"F"
};

var NAMED = {
  "aliceblue":[240,248,255],
  "antiquewhite":[250,235,215],
  "aqua":[0,255,255],
  "aquamarine":[127,255,212],
  "azure":[240,255,255],
  "beige":[245,245,220],
  "bisque":[255,228,196],
  "black":[0,0,0],
  "blanchedalmond":[255,235,205],
  "blue":[0,0,255],
  "blueviolet":[138,43,226],
  "brown":[165,42,42],
  "burlywood":[222,184,135],
  "cadetblue":[95,158,160],
  "chartreuse":[127,255,0],
  "chocolate":[210,105,30],
  "coral":[255,127,80],
  "cornflowerblue":[100,149,237],
  "cornsilk":[255,248,220],
  "crimson":[220,20,60],
  "cyan":[0,255,255],
  "darkblue":[0,0,139],
  "darkcyan":[0,139,139],
  "darkgoldenrod":[184,134,11],
  "darkgray":[169,169,169],
  "darkgreen":[0,100,0],
  "darkgrey":[169,169,169],
  "darkkhaki":[189,183,107],
  "darkmagenta":[139,0,139],
  "darkolivegreen":[85,107,47],
  "darkorange":[255,140,0],
  "darkorchid":[153,50,204],
  "darkred":[139,0,0],
  "darksalmon":[233,150,122],
  "darkseagreen":[143,188,143],
  "darkslateblue":[72,61,139],
  "darkslategray":[47,79,79],
  "darkslategrey":[47,79,79],
  "darkturquoise":[0,206,209],
  "darkviolet":[148,0,211],
  "deeppink":[255,20,147],
  "deepskyblue":[0,191,255],
  "dimgray":[105,105,105],
  "dimgrey":[105,105,105],
  "dodgerblue":[30,144,255],
  "firebrick":[178,34,34],
  "floralwhite":[255,250,240],
  "forestgreen":[34,139,34],
  "fuchsia":[255,0,255],
  "gainsboro":[220,220,220],
  "ghostwhite":[248,248,255],
  "gold":[255,215,0],
  "goldenrod":[218,165,32],
  "gray":[128,128,128],
  "green":[0,128,0],
  "greenyellow":[173,255,47],
  "grey":[128,128,128],
  "honeydew":[240,255,240],
  "hotpink":[255,105,180],
  "indianred":[205,92,92],
  "indigo":[75,0,130],
  "ivory":[255,255,240],
  "khaki":[240,230,140],
  "lavender":[230,230,250],
  "lavenderblush":[255,240,245],
  "lawngreen":[124,252,0],
  "lemonchiffon":[255,250,205],
  "lightblue":[173,216,230],
  "lightcoral":[240,128,128],
  "lightcyan":[224,255,255],
  "lightgoldenrodyellow":[250,250,210],
  "lightgray":[211,211,211],
  "lightgreen":[144,238,144],
  "lightgrey":[211,211,211],
  "lightpink":[255,182,193],
  "lightsalmon":[255,160,122],
  "lightseagreen":[32,178,170],
  "lightskyblue":[135,206,250],
  "lightslategray":[119,136,153],
  "lightslategrey":[119,136,153],
  "lightsteelblue":[176,196,222],
  "lightyellow":[255,255,224],
  "lime":[0,255,0],
  "limegreen":[50,205,50],
  "linen":[250,240,230],
  "magenta":[255,0,255],
  "maroon":[128,0,0],
  "mediumaquamarine":[102,205,170],
  "mediumblue":[0,0,205],
  "mediumorchid":[186,85,211],
  "mediumpurple":[147,112,219],
  "mediumseagreen":[60,179,113],
  "mediumslateblue":[123,104,238],
  "mediumspringgreen":[0,250,154],
  "mediumturquoise":[72,209,204],
  "mediumvioletred":[199,21,133],
  "midnightblue":[25,25,112],
  "mintcream":[245,255,250],
  "mistyrose":[255,228,225],
  "moccasin":[255,228,181],
  "navajowhite":[255,222,173],
  "navy":[0,0,128],
  "oldlace":[253,245,230],
  "olive":[128,128,0],
  "olivedrab":[107,142,35],
  "orange":[255,165,0],
  "orangered":[255,69,0],
  "orchid":[218,112,214],
  "palegoldenrod":[238,232,170],
  "palegreen":[152,251,152],
  "paleturquoise":[175,238,238],
  "palevioletred":[219,112,147],
  "papayawhip":[255,239,213],
  "peachpuff":[255,218,185],
  "peru":[205,133,63],
  "pink":[255,192,203],
  "plum":[221,160,221],
  "powderblue":[176,224,230],
  "purple":[128,0,128],
  "rebeccapurple":[102,51,153],
  "red":[255,0,0],
  "rosybrown":[188,143,143],
  "royalblue":[65,105,225],
  "saddlebrown":[139,69,19],
  "salmon":[250,128,114],
  "sandybrown":[244,164,96],
  "seagreen":[46,139,87],
  "seashell":[255,245,238],
  "sienna":[160,82,45],
  "silver":[192,192,192],
  "skyblue":[135,206,235],
  "slateblue":[106,90,205],
  "slategray":[112,128,144],
  "slategrey":[112,128,144],
  "snow":[255,250,250],
  "springgreen":[0,255,127],
  "steelblue":[70,130,180],
  "tan":[210,180,140],
  "teal":[0,128,128],
  "thistle":[216,191,216],
  "tomato":[255,99,71],
  "transparent":[0,0,0],
  "turquoise":[64,224,208],
  "violet":[238,130,238],
  "wheat":[245,222,179],
  "white":[255,255,255],
  "whitesmoke":[245,245,245],
  "yellow":[255,255,0],
  "yellowgreen":[154,205,50]
};

function named__gtrgb(s){
  return (null == NAMED[s]) ? [0,0,0] : NAMED[s];
}

function hex__gtn(s){
  let out = HEX[xts.to_uppercase(s)];
  if(null == out){
    out = 0;
  }
  return out;
}

function n__gthex(n){
  let v1 = Math.floor(n / 16);
  let v0 = xtm.mod_pos(n,16);
  return ((null == LU[v1]) ? "0" : LU[v1]) + ((null == LU[v0]) ? "0" : LU[v0]);
}

function hex__gtrgb(s){
  let val_fn = function (s,start,finish){
    return hex__gtn(xts.substring(s,start,finish));
  };
  if(!xts.starts_withp(s,"#")){
    throw "Not a valid hex color.";
  }
  if(4 == s.length){
    let r = val_fn(s,1,2);
    let g = val_fn(s,2,3);
    let b = val_fn(s,3,4);
    return [(r * 16) + r,(g * 16) + g,(b * 16) + b];
  }
  if(7 == s.length){
    let r1 = val_fn(s,1,2);
    let r0 = val_fn(s,2,3);
    let g1 = val_fn(s,3,4);
    let g0 = val_fn(s,4,5);
    let b1 = val_fn(s,5,6);
    let b0 = val_fn(s,6,7);
    return [(r1 * 16) + r0,(g1 * 16) + g0,(b1 * 16) + b0];
  }
  return [0,0,0];
}

function rgb__gthex(rgb){
  let [r,g,b] = rgb;
  return "#" + n__gthex(r) + n__gthex(g) + n__gthex(b);
}

function rgb__gthue(r,g,b,value,delta,fallback){
  if(0 == delta){
    return fallback;
  }
  let segment = null;
  if(value == r){
    segment = ((g - b) / delta);
  }
  else if(value == g){
    segment = ((b - r) / delta);
  }
  else{
    segment = ((r - g) / delta);
  }
  let shift = null;
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

function rgb__gthsl(rgb,fallback){
  let [r,g,b] = rgb;
  let value = Math.max(r,g,b);
  let whiteness = Math.min(r,g,b);
  let delta = value - whiteness;
  if(null == fallback){
    fallback = 0;
  }
  let h = rgb__gthue(r,g,b,value,delta,fallback);
  let l = (100 * (value + whiteness) * 0.5) / 255;
  let s = null;
  if(delta == 0){
    s = 0;
  }
  else{
    s = (((delta / 255) * 10000) / (100 - Math.abs((2 * l) - 100)));
  }
  return [h,Math.min(100,s),Math.min(100,l)];
}

function hue__gtv(t1,t2,h){
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

function hsl__gtrgb(hsl){
  let [hi,si,li] = hsl;
  let h = hi / 360;
  let s = si / 100;
  let l = li / 100;
  if(s == 0){
    return [xtm.round(255 * l),xtm.round(255 * l),xtm.round(255 * l)];
  }
  else{
    let t2 = (l < 0.5) ? (l * (s + 1)) : ((l + s) - (s * l));
    let t1 = (2 * l) - t2;
    return [
      xtm.round(255 * hue__gtv(t1,t2,h + (1 / 3))),
      xtm.round(255 * hue__gtv(t1,t2,h)),
      xtm.round(255 * hue__gtv(t1,t2,h - (1 / 3)))
    ]
  }
}

function named__gthsl(s){
  return rgb__gthsl(named__gtrgb(s),null);
}

function named__gthex(s){
  return rgb__gthex(named__gtrgb(s));
}

function hex__gthsl(s){
  return rgb__gthsl(hex__gtrgb(s),null);
}

module.exports = {
  ["HEX"]:HEX,
  ["LU"]:LU,
  ["NAMED"]:NAMED,
  ["named__gtrgb"]:named__gtrgb,
  ["hex__gtn"]:hex__gtn,
  ["n__gthex"]:n__gthex,
  ["hex__gtrgb"]:hex__gtrgb,
  ["rgb__gthex"]:rgb__gthex,
  ["rgb__gthue"]:rgb__gthue,
  ["rgb__gthsl"]:rgb__gthsl,
  ["hue__gtv"]:hue__gtv,
  ["hsl__gtrgb"]:hsl__gtrgb,
  ["named__gthsl"]:named__gthsl,
  ["named__gthex"]:named__gthex,
  ["hex__gthsl"]:hex__gthsl
}