import * as Lucide from 'lucide-react'

import * as FigmaUi from '@xtalk/figma-ui'

import React from 'react'

// code.dev.client.app.components.theme-editor/defaultTheme [9] 
export var defaultTheme = {
  "colors":{
    "textSecondary":"#6b7280",
    "primary":"#3b82f6",
    "background":"#ffffff",
    "secondary":"#8b5cf6",
    "warning":"#f59e0b",
    "success":"#10b981",
    "error":"#ef4444",
    "border":"#e5e7eb",
    "accent":"#ec4899",
    "surface":"#f9fafb",
    "text":"#111827"
  },
  "typography":{
    "fontFamily":"Inter, system-ui, sans-serif",
    "fontFamilyHeading":"Inter, system-ui, sans-serif",
    "fontFamilyMono":"JetBrains Mono, monospace",
    "fontSize":{
      "xs":"0.75rem",
      "sm":"0.875rem",
      "base":"1rem",
      "lg":"1.125rem",
      "xl":"1.25rem",
      "2xl":"1.5rem",
      "3xl":"1.875rem",
      "4xl":"2.25rem"
    }
  },
  "spacing":{
    "xs":"0.25rem",
    "sm":"0.5rem",
    "md":"1rem",
    "lg":"1.5rem",
    "xl":"2rem",
    "2xl":"3rem",
    "3xl":"4rem"
  },
  "borderRadius":{
    "none":"0",
    "sm":"0.25rem",
    "md":"0.5rem",
    "lg":"0.75rem",
    "xl":"1rem",
    "full":"9999px"
  },
  "shadows":{
    "sm":"0 1px 2px 0 rgb(0 0 0 / 0.05)",
    "md":"0 4px 6px -1px rgb(0 0 0 / 0.1)",
    "lg":"0 10px 15px -3px rgb(0 0 0 / 0.1)",
    "xl":"0 20px 25px -5px rgb(0 0 0 / 0.1)"
  }
};

// code.dev.client.app.components.theme-editor/ColorInput [51] 
export function ColorInput({label,value,onChange}){
  return (
    <div className="flex items-center gap-3">
      <div className="flex-1">
        <FigmaUi.Label className="text-xs text-gray-400 mb-1 block">{label}</FigmaUi.Label>
        <FigmaUi.Input
          type="text"
          value={value}
          onChange={function (e){
              return onChange(e.target.value);
            }}
          className="h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs">
        </FigmaUi.Input>
      </div>
      <div className="pt-5">
        <input
          type="color"
          value={value}
          onChange={function (e){
              return onChange(e.target.value);
            }}
          className="w-10 h-7 rounded border border-[#3a3a3a] cursor-pointer bg-[#1e1e1e]">
        </input>
      </div>
    </div>);
}

// code.dev.client.app.components.theme-editor/TextInput [69] 
export function TextInput({label,value,onChange}){
  return (
    <div>
      <FigmaUi.Label className="text-xs text-gray-400 mb-1 block">{label}</FigmaUi.Label>
      <FigmaUi.Input
        type="text"
        value={value}
        onChange={function (e){
            return onChange(e.target.value);
          }}
        className="h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs">
      </FigmaUi.Input>
    </div>);
}

// code.dev.client.app.components.theme-editor/ShadowInput [80] 
export function ShadowInput({label,value,onChange}){
  return (
    <div>
      <FigmaUi.Label className="text-xs text-gray-400 mb-1 block">{label}</FigmaUi.Label>
      <div className="flex items-center gap-2">
        <FigmaUi.Input
          type="text"
          value={value}
          onChange={function (e){
              return onChange(e.target.value);
            }}
          className="flex-1 h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs">
        </FigmaUi.Input>
        <div
          className="w-10 h-7 bg-white rounded border border-[#3a3a3a]"
          style={{"boxShadow":value}}>
        </div>
      </div>
    </div>);
}

// code.dev.client.app.components.theme-editor/ThemeEditor [95] 
export function ThemeEditor({theme,onThemeChange}){
  let [activeSection,setActiveSection] = React.useState("colors");
  let updateColor = function (key,value){
    onThemeChange({"colors":{key:value,...theme.colors},...theme});
  };
  let updateTypography = function (key,value){
    if(Object.keys(theme.typography).includes(key)){
      onThemeChange({"typography":{key:value,...theme.typography},...theme});
    }
    else{
      onThemeChange({
        "typography":{
                "fontSize":{key:value,...theme.typography.fontSize},
                ...theme.typography
              },
        ...theme
      });
    }
  };
  let updateSpacing = function (key,value){
    onThemeChange({"spacing":{key:value,...theme.spacing},...theme});
  };
  let updateBorderRadius = function (key,value){
    onThemeChange({"borderRadius":{key:value,...theme.borderRadius},...theme});
  };
  let updateShadow = function (key,value){
    onThemeChange({"shadows":{key:value,...theme.shadows},...theme});
  };
  let resetTheme = function (){
    onThemeChange(defaultTheme);
  };
  let exportTheme = async function (){
    let themeJSON = JSON.stringify(theme,null,2);
    try{
      await navigator.clipboard.writeText(themeJSON);
    }
    catch(err){
      let textArea = document.createElement("textarea");
      textArea.value = themeJSON;
      textArea.style.position = "fixed";
      textArea.style.left = "-999999px";
      textArea.style.top = "-999999px";
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      try{
        document.execCommand("copy");
      }
      catch(err2){
        console.error("Failed to copy to clipboard");
      }
      document.body.removeChild(textArea);
    }
  };
  return (
    <div className="flex flex-col h-full bg-[#252525]">
      <div
        className="h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3 justify-between">
        <span className="text-xs text-gray-400">Theme Editor</span>
        <div className="flex gap-1">
          <FigmaUi.Button
            variant="ghost"
            size="sm"
            onClick={exportTheme}
            className="h-6 px-2 text-xs text-gray-400 hover:text-gray-200 hover:bg-[#323232]"><Lucide.Copy className="w-3 h-3 mr-1"></Lucide.Copy>Copy
          </FigmaUi.Button>
          <FigmaUi.Button
            variant="ghost"
            size="sm"
            onClick={resetTheme}
            className="h-6 px-2 text-xs text-gray-400 hover:text-gray-200 hover:bg-[#323232]">
            <Lucide.RotateCcw className="w-3 h-3 mr-1"></Lucide.RotateCcw>
            Reset
          </FigmaUi.Button>
        </div>
      </div>
      <div className="flex border-b border-[#323232] bg-[#2b2b2b]">
        <button
          onClick={function (){
              return setActiveSection("colors");
            }}
          className={"flex items-center gap-2 px-4 py-2 text-xs transition-colors " + ((activeSection === "colors") ? "bg-[#323232] text-gray-200 border-b-2 border-blue-500" : "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]")}><Lucide.Palette className="w-3 h-3"></Lucide.Palette>Colors
        </button>
        <button
          onClick={function (){
              return setActiveSection("typography");
            }}
          className={"flex items-center gap-2 px-4 py-2 text-xs transition-colors " + ((activeSection === "typography") ? "bg-[#323232] text-gray-200 border-b-2 border-blue-500" : "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]")}><Lucide.Type className="w-3 h-3"></Lucide.Type>Typography
        </button>
        <button
          onClick={function (){
              return setActiveSection("spacing");
            }}
          className={"flex items-center gap-2 px-4 py-2 text-xs transition-colors " + ((activeSection === "spacing") ? "bg-[#323232] text-gray-200 border-b-2 border-blue-500" : "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]")}><Lucide.Ruler className="w-3 h-3"></Lucide.Ruler>Spacing
        </button>
        <button
          onClick={function (){
              return setActiveSection("borders");
            }}
          className={"flex items-center gap-2 px-4 py-2 text-xs transition-colors " + ((activeSection === "borders") ? "bg-[#323232] text-gray-200 border-b-2 border-blue-500" : "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]")}>
          <Lucide.Maximize2 className="w-3 h-3"></Lucide.Maximize2>
          Borders
        </button>
        <button
          onClick={function (){
              return setActiveSection("shadows");
            }}
          className={"flex items-center gap-2 px-4 py-2 text-xs transition-colors " + ((activeSection === "shadows") ? "bg-[#323232] text-gray-200 border-b-2 border-blue-500" : "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]")}>Shadows
        </button>
      </div>
      <FigmaUi.ScrollArea className="flex-1">
        <div className="p-4 space-y-4">
          {(activeSection === "colors") ? (
            <React.Fragment>
              <div className="space-y-3">
                <h3 className="text-xs text-gray-500 uppercase tracking-wider">Brand Colors</h3>
                <ColorInput
                  label="Primary"
                  value={theme.colors.primary}
                  onChange={function (v){
                      return updateColor("primary",v);
                    }}>
                </ColorInput>
                <ColorInput
                  label="Secondary"
                  value={theme.colors.secondary}
                  onChange={function (v){
                      return updateColor("secondary",v);
                    }}>
                </ColorInput>
                <ColorInput
                  label="Accent"
                  value={theme.colors.accent}
                  onChange={function (v){
                      return updateColor("accent",v);
                    }}>
                </ColorInput>
              </div>
              <div className="h-[1px] bg-[#323232]"></div>
              <div className="space-y-3">
                <h3 className="text-xs text-gray-500 uppercase tracking-wider">Surface Colors</h3>
                <ColorInput
                  label="Background"
                  value={theme.colors.background}
                  onChange={function (v){
                      return updateColor("background",v);
                    }}>
                </ColorInput>
                <ColorInput
                  label="Surface"
                  value={theme.colors.surface}
                  onChange={function (v){
                      return updateColor("surface",v);
                    }}>
                </ColorInput>
                <ColorInput
                  label="Border"
                  value={theme.colors.border}
                  onChange={function (v){
                      return updateColor("border",v);
                    }}>
                </ColorInput>
              </div>
              <div className="h-[1px] bg-[#323232]"></div>
              <div className="space-y-3">
                <h3 className="text-xs text-gray-500 uppercase tracking-wider">Text Colors</h3>
                <ColorInput
                  label="Text"
                  value={theme.colors.text}
                  onChange={function (v){
                      return updateColor("text",v);
                    }}>
                </ColorInput>
                <ColorInput
                  label="Text Secondary"
                  value={theme.colors.textSecondary}
                  onChange={function (v){
                      return updateColor("textSecondary",v);
                    }}>
                </ColorInput>
              </div>
              <div className="h-[1px] bg-[#323232]"></div>
              <div className="space-y-3">
                <h3 className="text-xs text-gray-500 uppercase tracking-wider">Semantic Colors</h3>
                <ColorInput
                  label="Success"
                  value={theme.colors.success}
                  onChange={function (v){
                      return updateColor("success",v);
                    }}>
                </ColorInput>
                <ColorInput
                  label="Warning"
                  value={theme.colors.warning}
                  onChange={function (v){
                      return updateColor("warning",v);
                    }}>
                </ColorInput>
                <ColorInput
                  label="Error"
                  value={theme.colors.error}
                  onChange={function (v){
                      return updateColor("error",v);
                    }}>
                </ColorInput>
              </div>
            </React.Fragment>) : null}
          {(activeSection === "typography") ? (
            <React.Fragment>
              <div className="space-y-3">
                <h3 className="text-xs text-gray-500 uppercase tracking-wider">Font Families</h3>
                <TextInput
                  label="Body Font"
                  value={theme.typography.fontFamily}
                  onChange={function (v){
                      return updateTypography("fontFamily",v);
                    }}>
                </TextInput>
                <TextInput
                  label="Heading Font"
                  value={theme.typography.fontFamilyHeading}
                  onChange={function (v){
                      return updateTypography("fontFamilyHeading",v);
                    }}>
                </TextInput>
                <TextInput
                  label="Mono Font"
                  value={theme.typography.fontFamilyMono}
                  onChange={function (v){
                      return updateTypography("fontFamilyMono",v);
                    }}>
                </TextInput>
              </div>
              <div className="h-[1px] bg-[#323232]"></div>
              <div className="space-y-3">
                <h3 className="text-xs text-gray-500 uppercase tracking-wider">Font Sizes</h3>
                <TextInput
                  label="XS"
                  value={theme.typography.fontSize.xs}
                  onChange={function (v){
                      return updateTypography("xs",v);
                    }}>
                </TextInput>
                <TextInput
                  label="SM"
                  value={theme.typography.fontSize.sm}
                  onChange={function (v){
                      return updateTypography("sm",v);
                    }}>
                </TextInput>
                <TextInput
                  label="Base"
                  value={theme.typography.fontSize.base}
                  onChange={function (v){
                      return updateTypography("base",v);
                    }}>
                </TextInput>
                <TextInput
                  label="LG"
                  value={theme.typography.fontSize.lg}
                  onChange={function (v){
                      return updateTypography("lg",v);
                    }}>
                </TextInput>
                <TextInput
                  label="XL"
                  value={theme.typography.fontSize.xl}
                  onChange={function (v){
                      return updateTypography("xl",v);
                    }}>
                </TextInput>
                <TextInput
                  label="2XL"
                  value={theme.typography.fontSize["2xl"]}
                  onChange={function (v){
                      return updateTypography("2xl",v);
                    }}>
                </TextInput>
                <TextInput
                  label="3XL"
                  value={theme.typography.fontSize["3xl"]}
                  onChange={function (v){
                      return updateTypography("3xl",v);
                    }}>
                </TextInput>
                <TextInput
                  label="4XL"
                  value={theme.typography.fontSize["4xl"]}
                  onChange={function (v){
                      return updateTypography("4xl",v);
                    }}>
                </TextInput>
              </div>
            </React.Fragment>) : null}
          {(activeSection === "spacing") ? (
            <div className="space-y-3">
              <h3 className="text-xs text-gray-500 uppercase tracking-wider">Spacing Scale</h3>
              <TextInput
                label="XS"
                value={theme.spacing.xs}
                onChange={function (v){
                    return updateSpacing("xs",v);
                  }}>
              </TextInput>
              <TextInput
                label="SM"
                value={theme.spacing.sm}
                onChange={function (v){
                    return updateSpacing("sm",v);
                  }}>
              </TextInput>
              <TextInput
                label="MD"
                value={theme.spacing.md}
                onChange={function (v){
                    return updateSpacing("md",v);
                  }}>
              </TextInput>
              <TextInput
                label="LG"
                value={theme.spacing.lg}
                onChange={function (v){
                    return updateSpacing("lg",v);
                  }}>
              </TextInput>
              <TextInput
                label="XL"
                value={theme.spacing.xl}
                onChange={function (v){
                    return updateSpacing("xl",v);
                  }}>
              </TextInput>
              <TextInput
                label="2XL"
                value={theme.spacing["2xl"]}
                onChange={function (v){
                    return updateSpacing("2xl",v);
                  }}>
              </TextInput>
              <TextInput
                label="3XL"
                value={theme.spacing["3xl"]}
                onChange={function (v){
                    return updateSpacing("3xl",v);
                  }}>
              </TextInput>
            </div>) : null}
          {(activeSection === "borders") ? (
            <div className="space-y-3">
              <h3 className="text-xs text-gray-500 uppercase tracking-wider">Border Radius</h3>
              <TextInput
                label="None"
                value={theme.borderRadius.none}
                onChange={function (v){
                    return updateBorderRadius("none",v);
                  }}>
              </TextInput>
              <TextInput
                label="SM"
                value={theme.borderRadius.sm}
                onChange={function (v){
                    return updateBorderRadius("sm",v);
                  }}>
              </TextInput>
              <TextInput
                label="MD"
                value={theme.borderRadius.md}
                onChange={function (v){
                    return updateBorderRadius("md",v);
                  }}>
              </TextInput>
              <TextInput
                label="LG"
                value={theme.borderRadius.lg}
                onChange={function (v){
                    return updateBorderRadius("lg",v);
                  }}>
              </TextInput>
              <TextInput
                label="XL"
                value={theme.borderRadius.xl}
                onChange={function (v){
                    return updateBorderRadius("xl",v);
                  }}>
              </TextInput>
              <TextInput
                label="Full"
                value={theme.borderRadius.full}
                onChange={function (v){
                    return updateBorderRadius("full",v);
                  }}>
              </TextInput>
            </div>) : null}
          {(activeSection === "shadows") ? (
            <div className="space-y-3">
              <h3 className="text-xs text-gray-500 uppercase tracking-wider">Box Shadows</h3>
              <ShadowInput
                label="SM"
                value={theme.shadows.sm}
                onChange={function (v){
                    return updateShadow("sm",v);
                  }}>
              </ShadowInput>
              <ShadowInput
                label="MD"
                value={theme.shadows.md}
                onChange={function (v){
                    return updateShadow("md",v);
                  }}>
              </ShadowInput>
              <ShadowInput
                label="LG"
                value={theme.shadows.lg}
                onChange={function (v){
                    return updateShadow("lg",v);
                  }}>
              </ShadowInput>
              <ShadowInput
                label="XL"
                value={theme.shadows.xl}
                onChange={function (v){
                    return updateShadow("xl",v);
                  }}>
              </ShadowInput>
            </div>) : null}
        </div>
      </FigmaUi.ScrollArea>
    </div>);
}