import LoginForm from './LoginForm.svelte'
import mount from 'cypress-svelte-unit-test'

describe('LoginForm', () => {
  it('should render text box', () => {
    mount(LoginForm)
    cy.get('#token').should('exist')
  })
})
