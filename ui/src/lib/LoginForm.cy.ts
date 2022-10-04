import LoginForm from './LoginForm.svelte'

describe('LoginForm', () => {
  beforeEach(() => {
    cy.mount(LoginForm)
  })

  it('should render empty form', () => {
    getTextInput().should('be.visible').should('not.contain.text')
    getButton().should('be.disabled').should('have.text', 'Submit')
  })

  it('should enable button if text box has content', () => {
    getTextInput().type('a')
    getButton().should('be.enabled')
  })

  it('should disable button after text box has been cleared', () => {
    getTextInput().type('a')
    getButton().should('be.enabled')
    getTextInput().type('{backspace}')
    getButton().should('be.disabled')
  })

  it('should display alert if input not JWT', () => {
    getTextInput().type('a')
    getButton().click()
    getAlert()
      .should('be.visible')
      .should('contain.text', 'Cannot login: Invalid JWT')
      .should('have.class', 'alert-danger')
  })

  it('should display auth error if token invalid', () => {
    cy.intercept('POST', '/apis/authorization.k8s.io/v1/selfsubjectaccessreviews', {
      statusCode: 401,
      body: {
        kind: 'Status',
        apiVersion: 'v1',
        metadata: {},
        status: 'Failure',
        message: 'Unauthorized',
        reason: 'Unauthorized',
        code: 401
      }
    }).as('selfSubjectAccessReview')

    getTextInput().type(e2eToken, { delay: 0 })
    getButton().click()
    getAlert().should('be.visible').should('contain.text', 'Cannot login: Unauthorized')
  })

  it('should display permission error if token has insufficient RBAC permissions', () => {
    cy.intercept('POST', '/apis/authorization.k8s.io/v1/selfsubjectaccessreviews', {
      statusCode: 401,
      body: {
        status: {
          allowed: false
        }
      }
    }).as('selfSubjectAccessReview')

    getTextInput().type(e2eToken, { delay: 0 })
    getButton().click()
    getAlert()
      .should('be.visible')
      .should('contain.text', 'You are not allowed to view blueprints.')
  })
})

function getButton() {
  return cy.get('[data-cy="submit"]')
}

function getTextInput() {
  return cy.get('[data-cy="token"]')
}

function getAlert() {
  return cy.get('[data-cy="alert"]')
}

/**
 * {
 *   "aud": [
 *     "https://kubernetes.default.svc.cluster.local"
 *   ],
 *   "iss": "https://kubernetes.default.svc.cluster.local",
 *   "kubernetes.io": {
 *     "namespace": "clustercode-system",
 *     "serviceaccount": {
 *       "name": "cypress",
 *       "uid": "6bf6a38d-dadc-4300-8d49-9000d0aa62c4"
 *     }
 *   },
 *   "sub": "system:serviceaccount:clustercode-system:clustercode"
 * }
 */
const e2eToken =
  'eyJhbGciOiJSUzI1NiIsImtpZCI6IlJQcVlRZFFPSWVhNE8tODY5LVUwNVJMUlF1TkhMZWdxX2IyZW0tNFlLRTgifQ.eyJhdWQiOlsiaHR0cHM6Ly9rdWJlcm5ldGVzLmRlZmF1bHQuc3ZjLmNsdXN0ZXIubG9jYWwiXSwiaXNzIjoiaHR0cHM6Ly9rdWJlcm5ldGVzLmRlZmF1bHQuc3ZjLmNsdXN0ZXIubG9jYWwiLCJrdWJlcm5ldGVzLmlvIjp7Im5hbWVzcGFjZSI6ImNsdXN0ZXJjb2RlLXN5c3RlbSIsInNlcnZpY2VhY2NvdW50Ijp7Im5hbWUiOiJjeXByZXNzIiwidWlkIjoiNmJmNmEzOGQtZGFkYy00MzAwLThkNDktOTAwMGQwYWE2MmM0In19LCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6Y2x1c3RlcmNvZGUtc3lzdGVtOmNsdXN0ZXJjb2RlIn0.D4eIkVyljd_zekmNYtGwJ3lA3dbez79lNE35nrOZl_pZPslpT6_ToZTw4u_BH006d24RuU-SWYuc25gTNCSA4B4FU84MVwb6FIODhTw1eVgfVlwJ3gsVvv9q9QZNpNd-r7qjtTkKsQbOPb6EFY1glqjzIZwFE9mlrKYKL3yD8k8AStHgoD-Dundw3kLVCJ4kh-eRPKCvuL2TVrL2cTykv9rg9GglgBnRb4qLfXNpcX68qbFZL4COA1d1nURU0_f6DYfrpUFQES0JaBLJiLo81UzfyROVGCgx8vwn7Hb5rVyxDn-itj7BEcVaDBFz_YU3yR_V05UyFuWlGu_-dzOLkw'
