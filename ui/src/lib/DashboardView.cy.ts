import DashboardView from "./DashboardView.svelte"
import { ClientStore } from "./stores/ClientStore"
import { KubernetesClient } from "./kube/kubernetesClient"

describe("DashboardView", () => {
	beforeEach(() => {
		ClientStore.set(new KubernetesClient())
	})

	it("should render empty table if no blueprints found", () => {
		cy.intercept("GET", "/apis/clustercode.github.io/v1alpha1/blueprints", {
			statusCode: 200,
			body: {
				kind: "BlueprintList",
				apiVersion: "clustercode.github.io/v1alpha1",
				items: [],
			},
		}).as("blueprintlist")
		cy.mount(DashboardView)
		cy.wait("@blueprintlist")
		getTable().should("not.exist")
	})

	it("should render table if blueprints listed", () => {
		cy.intercept("GET", "/apis/clustercode.github.io/v1alpha1/blueprints", {
			body: {
				statusCode: 200,
				kind: "BlueprintList",
				apiVersion: "clustercode.github.io/v1alpha1",
				items: [
					{
						apiVersion: "clustercode.github.io/v1alpha1",
						kind: "Blueprint",
						metadata: {
							name: "test-blueprint",
							namespace: "clustercode-system",
						},
						spec: {
							scan: {
								schedule: "*/10 * * * *",
							},
						},
					},
				],
			},
		}).as("blueprintlist")
		cy.mount(DashboardView)
		cy.wait("@blueprintlist")
		getTable().should("be.visible")
	})

	it("should render an alert if forbidden", () => {
		cy.intercept("GET", "/apis/clustercode.github.io/v1alpha1/blueprints", {
			statusCode: 403,
			body: {
				status: "Failure",
				message:
					'blueprints.clustercode.github.io is forbidden: User "system:anonymous" cannot list resource "blueprints" in API group "clustercode.github.io" at the cluster scope',
				reason: "Forbidden",
				code: 403,
			},
		}).as("blueprintlist")
		cy.mount(DashboardView)
		cy.wait("@blueprintlist")
		getTable().should("not.exist")
		getAlert().should("be.visible").should("contain.text", "You are not allowed to view blueprints.")
	})
})

function getTable() {
	return cy.get("table")
}

function getAlert() {
	return cy.get("div[class=alert]")
}
