import * as T from 'tamagui'

import React from 'react'

export var ButtonNormal = T.Button;

export var ThemeGlobalContext = React.createContext(null);

export function useThemeGlobal(){
  return React.useContext(ThemeGlobalContext);
}

export function t(text){
  return text;
}

export function useWindowDimensions(){
  let read = function (){
    return {
      "width":((typeof window) == "undefined") ? 0 : window.innerWidth,
      "height":((typeof window) == "undefined") ? 0 : window.innerHeight
    };
  };
  let [size,setSize] = React.useState({"width":0,"height":0});
  React.useEffect(function (){
    let update = function (){
      setSize(read());
    };
    update();
    window.addEventListener("resize",update);
    return function (){
      window.removeEventListener("resize",update);
    };
  },[]);
  return size;
}

export function PopoverMenu({
  open,
  onOpenChange,
  icon,
  button,
  buttonProps,
  contentProps,
  arrowProps,
  triggerProps,
  children,
  ...props
}){
  return React.createElement(T.Popover,Object.assign({},props,{
    "allowFlip":true,
    "onOpenChange":onOpenChange,
    "open":open,
    "resize":true,
    "size":"$6",
    "stayInFrame":true
  }),React.createElement(
    T.PopoverTrigger,
    Object.assign({},triggerProps,{"asChild":true}),
    button || React.createElement(T.Button,Object.assign({},buttonProps,{"icon":icon}))
  ),React.createElement(T.PopoverContent,Object.assign({
    "animation":["quick",{"opacity":{"overshootClamping":true}}],
    "borderColor":"$borderColor",
    "borderWidth":1,
    "elevate":true,
    "elevation":5,
    "enterStyle":{"opacity":0,"y":-10},
    "exitStyle":{"opacity":0,"y":-10},
    "padding":"$4"
  },contentProps),React.createElement(
    T.PopoverArrow,
    Object.assign({"borderColor":"$borderColor","borderWidth":1},arrowProps)
  ),children));
}