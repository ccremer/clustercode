<script lang="ts">
  import { Button, Label, Form, FormGroup, Input } from "sveltestrap";

  let token = "";

  async function login() {
    // TODO: implement
  }
</script>

<div>

</div>
<Form on:submit={login}>
  <FormGroup floating label="Token">
    <Input placeholder="Token" id="token" type="password" bind:value={token} />
  </FormGroup>
  <Button id="btn-submit" color="primary">Submit</Button>
</Form>
