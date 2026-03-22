import { definePreset, palette } from '@primeng/themes';
import Aura from '@primeng/themes/aura';

export const CarebridgeTheme = definePreset(Aura, {
  primitive: {
    emerald: palette('#6EA06F'),
    ocean: palette('#92C8D9'),
    sky: palette('#7799CF'),
    rose: palette('#BA615E'),
    alert: palette('#BC0000'),
    slate: palette('#F1FAF1'),
    ink: palette('#000000')
  },
  semantic: {
    primary: palette('{emerald}'),
    surface: palette('{slate}'),
    colorScheme: {
      light: {
        surface: {
          0: '#ffffff',
          50: '#F1FAF1',
          100: '#E2EEE2',
          200: '#C8D9C8',
          500: '#5F705F',
          700: '#000000',
          950: '#000000'
        },
        primary: {
          color: '{emerald.600}',
          contrastColor: '#ffffff'
        }
      }
    }
  },
  components: {
    primary: {
      background: '{emerald.600}',
      borderColor: 'transparent',
      color: '#ffffff',
      shadow: '0 6px 18px 0 rgba(110, 160, 111, 0.35)',
      hoverBackground: '{emerald.700}',
      hoverShadow: '0 8px 22px 0 rgba(110, 160, 111, 0.45)',
    },
    secondary: {
      outlined: {
        background: '#ffffff',
        borderColor: '{surface.200}',
        color: '{surface.700}',
        borderWidth: '2px',
        shadow: '0 4px 10px rgba(0, 0, 0, 0.05)',
        hoverBackground: '{surface.50}',
        hoverBorderColor: '{surface.300}',
      }
    },
    card: {
      root: {
        background: '#ffffff',
        color: '#000000',
        borderRadius: '24px',
        borderWidth: '1.5px',
        borderStyle: 'solid',
        borderColor: '{surface.200}',
        shadow: '0 10px 25px -5px rgba(0, 0, 0, 0.05)'
      },
      title: {
        color: '#000000'
      }
    },
    inputtext: {
      root: {
        background: '#ffffff',
        color: '#000000',
        borderWidth: '1.5px',
        borderColor: '{surface.200}',
        borderRadius: '12px',
        padding: '0.75rem 1rem',
        placeholder: {
          color: '{surface.500}'
        }
      },
      focus: {
        borderColor: '{emerald.500}',
        shadow: '0 0 0 4px rgba(110, 160, 111, 0.15)'
      }
    },
    button: {
      root: {
        borderRadius: '12px',
        fontWeight: '600',
        padding: '0.75rem 1.5rem'
      },
      secondary: {
        background: '{surface.100}',
        color: '{ink}',
        borderColor: '{surface.200}'
      },
      danger: {
        background: '{alert.600}',
        borderColor: '{alert.700}',
        color: '#ffffff'
      }
    },
    checkbox: {
      root: {
        borderRadius: '6px',
        borderColor: '{surface.300}',
        background: '#ffffff'
      }
    },
    tabs: {
      tab: {
        color: '{surface.600}',
        activeColor: '{emerald.700}'
      }
    },
    table: {
      root: {
        borderWidth: '1px',
        borderColor: '{surface.200}',
        borderRadius: '16px',
        background: '#ffffff'
      },
      header: {
        background: '{surface.50}',
        color: '#000000',
        fontWeight: '700'
      }
    }
  }
});
