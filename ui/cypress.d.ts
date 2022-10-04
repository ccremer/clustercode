import { mount } from 'cypress/svelte'

declare global {
  namespace Cypress {
    interface Chainable {
      mount: typeof mount
    }
  }
}
