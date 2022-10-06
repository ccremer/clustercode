<script lang="ts">
  import { Button, FormGroup, Input, Alert } from "sveltestrap"
  import { Client, RequestError } from "../kube/client"
  import { ClientStore } from "../stores/ClientStore"
  import { onMount } from "svelte"
  import { ServerSettingsStore } from "../stores/SettingsStore"

  let token = ""
  let displayError = ""
  let alertVisible = false
  let cookieMaxAge: number

  ServerSettingsStore.subscribe(settings => {
    cookieMaxAge = settings.authCookieMaxAge
  })

  // returns the cookie with the given name, or undefined if not found
  function getCookie(name: string): string {
    // https://javascript.info/cookie
    const matches = document.cookie.match(
      new RegExp(`(?:^|; )${name.replace(/([.$?*|{}()[\]\\/+^])/g, "\\$1")}=([^;]*)`)
    )
    return matches ? decodeURIComponent(matches[1]) : undefined
  }

  onMount(() => {
    const cookie = getCookie("kubetoken")
    if (cookie) {
      console.log("retrieved token from cookie, logging in...")
      token = cookie
      login()
    }
  })

  function login() {
    let client = new Client()
    client
      .login(token)
      .then(ssar => {
        if (ssar.status.allowed) {
          console.log("successfully logged in")
          dismissError()
          ClientStore.set(client)
          const secure = location.protocol === "https:" ? "secure" : ""
          document.cookie = `kubetoken=${token}; max-age=${cookieMaxAge}; samesite=strict; ${secure}`
          return
        }
        showError("You are not allowed to view blueprints.")
      })
      .catch(err => {
        console.log("cannot login", err)
        if (err instanceof RequestError) {
          showError(`Kubernetes error: ${err.message}`)
        }
        if (err instanceof Error) {
          showError(`Cannot login: ${err.message}`)
        }
      })
  }
  function showError(message: string) {
    displayError = message
    alertVisible = true
  }
  function dismissError() {
    alertVisible = false
    displayError = ""
  }
</script>

{#if alertVisible}
  <Alert
    color="danger"
    fade={false}
    isOpen={alertVisible}
    dismissible={true}
    toggle={dismissError}
    data-cy="alert">{displayError}</Alert
  >
{/if}

<FormGroup floating={true} label="Token">
  <Input
    placeholder="Token"
    id="token"
    type="password"
    bind:value={token}
    data-cy="token"
  />
</FormGroup>
<Button
  on:click={login}
  disabled={!token}
  id="btn-submit"
  color="primary"
  data-cy="submit">Submit</Button
>
