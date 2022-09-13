import 'bootstrap/dist/css/bootstrap.min.css'
import App from './App.svelte'
import { writable } from 'svelte/store'

const app = new App({
  target: document.getElementById('app')
})

export const authToken = writable('')

export default app
