/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        primary: '#1B4F8A',
        secondary: '#2E7D32',
        danger: '#C62828',
        warning: '#F57F17',
        surface: '#F5F7FA',
      },
      fontFamily: {
        sans: ['Arial', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
