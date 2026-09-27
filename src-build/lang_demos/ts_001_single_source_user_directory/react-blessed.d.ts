declare module "react-blessed" {
  import type { ReactElement } from "react";

  export function render(element: ReactElement, screen: unknown): void;
}

declare namespace JSX {
  interface IntrinsicElements {
    box: any;
  }
}
