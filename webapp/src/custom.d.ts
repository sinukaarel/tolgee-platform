import API from '@openreplay/tracker';
import { PaletteColor } from '@mui/material/styles';
import { PaletteColorOptions } from '@mui/material';
import {
  Tooltip,
  Activity,
  BillingProgress,
  Cell,
  Editor,
  Emphasis,
  ExampleBanner,
  Marker,
  Navbar,
  Placeholders,
  QuickStart,
  Tile,
  TipsBanner,
  TopBanner,
  LanguageChips,
  Login,
  Input,
  RevisionFilterBanner,
} from './colors';

declare module '*.svg' {
  const content: React.FunctionComponent<React.SVGAttributes<SVGElement>>;
  export default content;
}
import { colors } from './colors';

const all = { ...colors.light, ...colors.dark };

declare module '@mui/material/styles/createPalette' {
  interface Palette {
    primaryText: string;
    divider1: string;
    tooltip: Tooltip;
    tile: Tile;
    cell: Cell;
    default: PaletteColor;
    navbar: Navbar;
    emphasis: Emphasis;
    activity: Activity;
    editor: Editor;
    billingProgress: BillingProgress;
    billingPlan: PaletteColor;
    globalLoading: PaletteColor;
    marker: Marker;
    topBanner: TopBanner;
    quickStart: QuickStart;
    import: typeof all.import;
    exampleBanner: ExampleBanner;
    tipsBanner: TipsBanner;
    tokens: typeof all.tokens;
    placeholders: Placeholders;
    languageChips: LanguageChips;
    login: Login;
    input: Input;
    revisionFilterBanner: RevisionFilterBanner;
  }

  interface PaletteOptions {
    primaryText: string;
    divider1: string;
    tooltip: Tooltip;
    tile: Tile;
    cell: Cell;
    default: PaletteColor;
    navbar: Navbar;
    emphasis: Emphasis;
    activity: Activity;
    editor: Editor;
    billingProgress: BillingProgress;
    billingPlan: PaletteColorOptions;
    globalLoading: PaletteColorOptions;
    marker: Marker;
    topBanner: TopBanner;
    quickStart: QuickStart;
    import: typeof all.import;
    exampleBanner: ExampleBanner;
    tipsBanner: TipsBanner;
    tokens: typeof all.tokens;
    placeholders: Placeholders;
    languageChips: LanguageChips;
    login: Login;
    input: Input;
    revisionFilterBanner: RevisionFilterBanner;
  }
}

declare module '@mui/material/Button' {
  interface ButtonPropsColorOverrides {
    default: true;
  }
}

declare global {
  interface Window {
    openReplayTracker?: API;
  }
}

declare module 'react' {
  interface HTMLAttributes<T> extends AriaAttributes, DOMAttributes<T> {
    webkitdirectory?: boolean;
  }
}
