import { describe, it, expect } from "vitest"
import { KubernetesClient } from "./kubernetes-client"
import { afterAll, afterEach, beforeAll } from "vitest"
import { setupServer } from "msw/node"
import { rest } from "msw"
import { newSelfSubjectRulesReview } from "./types/self-subject-rules-review"
import type { SelfSubjectRulesReview } from "./types/self-subject-rules-review"

describe("kubernetes client", () => {
	const server = setupServer()

	beforeAll(() => {
		server.listen()
	})
	afterAll(() => server.close())
	afterEach(() => server.resetHandlers())

	it("should make API request and return response", async () => {
		server.use(
			rest.post("http://localhost:8080/apis/authorization.k8s.io/v1/selfsubjectrulesreviews", async (req, res, ctx) => {
				const response = newSelfSubjectRulesReview("default")
				response.status = {
					evaluationError: "",
					incomplete: false,
					resourceRules: [
						{
							resources: ["namespaces"],
							apiGroups: [""],
							verbs: ["create"],
						},
					],
					nonResourceRules: [],
				}
				await expect(req.json<SelfSubjectRulesReview>()).resolves.toMatchObject({
					spec: { namespace: "default" },
				})
				return res(ctx.json(response))
			})
		)

		const client = new KubernetesClient(fetch, "token", "http://localhost:8080")
		await expect(client.create<SelfSubjectRulesReview>(newSelfSubjectRulesReview("default"))).resolves.toMatchObject({
			status: { resourceRules: [{ verbs: ["create"] }] },
		})
	})
})
