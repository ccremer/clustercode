<script lang="ts">
  import { Button, FormGroup, Input, Label } from 'sveltestrap'
  import { authToken } from '../stores/AuthStore'
  import { Client } from '../kube/client'
  import { SelfSubjectAccessReview } from '../kube/types/selfSubjectAccessReview'
    import { SelfSubjectRulesReview } from '../kube/types/selfSubjectRulesReview'

  let token = ''
  let allowed = false

  function login() {
    authToken.set(token)
    let client = new Client()
    let obj = new SelfSubjectRulesReview(     ''    )
    client
      .get<SelfSubjectRulesReview>(obj)
      .then(obj => {
        console.log(obj)
        allowed = true
      })
      .catch(err => {
        console.log(err)
      })
  }
</script>

<FormGroup floating label="Token">
  <Input placeholder="Token" id="token" type="password" bind:value={token} />
</FormGroup>
<Button on:click={login} id="btn-submit" color="primary">Submit</Button>
<Label>{allowed}</Label>
