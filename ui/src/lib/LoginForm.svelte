<script lang="ts">
  import { Button, FormGroup, Input, Label, Alert } from 'sveltestrap'
  import { Client, RequestError } from '../kube/client'

  let token = ''
  let displayError = ''
  let alertVisible = false

  function login() {
    let client = new Client()
    client
      .login(token)
      .then(ssar => {
        if (ssar.status.allowed) {
          dismissError()
        } else {
          showError('You are not allowed to view blueprints.')
          return
        }
      })
      .catch(err => {
        console.log(err)
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
    displayError = ''
  }
</script>

{#if alertVisible}
  <Alert
    color="danger"
    fade={false}
    isOpen={alertVisible}
    toggle={dismissError}
    dismissible>{displayError}</Alert
  >
{/if}

<FormGroup floating label="Token">
  <Input placeholder="Token" id="token" type="password" bind:value={token} />
</FormGroup>
<Button on:click={login} disabled={!token} id="btn-submit" color="primary">Submit</Button>
