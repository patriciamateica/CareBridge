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
    ink: palette('#1A1A1A')
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
          700: '{ink}',
          950: '{ink}'
        },
        primary: {
          color: '{emerald.600}',
          contrastColor: '#ffffff'
        }
      }
    }
  },
  components: {
    card: {
      root: {
        background: '#ffffff',
        color: '{ink}',
        borderRadius: '24px',
        borderWidth: '1.5px',
        borderStyle: 'solid',
        borderColor: '{surface.200}',
        shadow: '0 10px 25px -5px rgba(0, 0, 0, 0.05)'
      },
      title: {
        color: '{ink}'
      }
    },
    inputtext: {
      root: {
        background: '#ffffff',
        color: '{ink}',
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
        color: '{emerald.800}',
        fontWeight: '700'
      }
    }
  }
});
