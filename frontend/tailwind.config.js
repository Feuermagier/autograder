const colors = require('tailwindcss/colors')

module.exports = {
  purge: {
    enabled: !process.env.ROLLUP_WATCH,
    content: ['./public/index.html', './src/**/*.svelte'],
    options: {
      defaultExtractor: content => [
        ...(content.match(/[^<>"'`\s]*[^<>"'`\s:]/g) || []),
        ...(content.match(/(?<=class:)[^=>\/\s]*/g) || []),
      ],
    },
  },
  darkMode: false, // or 'media' or 'class'
  theme: {
    extend: {
      maxWidth: {
        '1/4': '25%',
        '1/2': '50%',
        '3/4': '75%',
        'nearly-full': '90%',
      },
      maxHeight: {
        '1/4': '25%',
        '1/2': '50%',
        '3/4': '75%',
        'nearly-full': '90%',
      },
      width: {
        '1/4-screen': '25vw',
        '1/2-screen': '50vw',
        '3/4-screen': '75vw',
        'nearly-full-screen': '90vw',
      },
      height: {
        '1/4-screen': '25vw',
        '1/2-screen': '50vw',
        '3/4-screen': '75vw',
        'nearly-full-screen': '90vw',
      },
      colors: {
        'ok-green': 'chartreuse',
        'error-red': '#EF5350',
        'primary': 'rgb(37, 99, 235)',
      },
    },
  },
  variants: {
    extend: {
      opacity: ['disabled'],
      cursor: ['disabled']
    },
  },
  plugins: [],
}