import { readable } from "svelte/store"

/**
 * ServerSettings contains properties transferred over from the server
 */
export interface ServerSettings {
  authCookieMaxAge?: number
}

let cached: ServerSettings

export const ServerSettingsStore = readable<ServerSettings>(
  newDefaultSettings(),
  function start(set) {
    if (cached !== undefined) {
      set(cached)
      return
    }
    fetch("/settings", {
      headers: {
        "Content-Type": "application/json"
      },
      credentials: "omit"
    })
      .then(resp => {
        if (resp.ok) {
          return resp.json()
        }
        throw new Error(resp.statusText)
      })
      .then(js => {
        set(js as ServerSettings)
      })
      .catch(e => {
        console.log("could not load server settings", e)
      })
  }
)

function newDefaultSettings(): ServerSettings {
  return {
    authCookieMaxAge: 86400
  }
}
