import { writable } from "svelte/store"
import type { ServiceAccountToken } from "../kube/kubernetes-client"
import { decodeToken } from "../kube/kubernetes-client"
import type { Subscriber, Unsubscriber } from "svelte/types/runtime/store"

export class User {
	private readonly _fullName: string

	constructor(public rawToken?: string, public decodedToken?: ServiceAccountToken) {
		this._fullName = decodedToken ? `${decodedToken.namespace}/${decodedToken.name}` : ""
	}

	get loggedIn(): boolean {
		return this.decodedToken ? this.decodedToken.expires.getTime() > new Date().getTime() : false
	}

	get fullName(): string {
		return this._fullName
	}

	get name(): string {
		return this.decodedToken?.name ?? ""
	}

	get namespace(): string {
		return this.decodedToken?.namespace ?? ""
	}
}

export interface UserStore {
	subscribe(this: void, run: Subscriber<User>, invalidate?: (value?: User) => void): Unsubscriber

	logIn(token: string): void

	logOut(): void

	restore(): boolean
}

export const user: UserStore = createUser()

function createUser(): UserStore {
	const { subscribe, set } = writable<User>(new User())
	return {
		subscribe,
		logIn: (token: string) => {
			const decoded = decodeToken(token)
			localStorage.setItem("kubetoken", token)
			set(new User(token, decoded))
			console.debug("saved kubetoken")
		},
		logOut: () => {
			localStorage.removeItem("kubetoken")
			set(new User())
			console.debug("deleted kubetoken")
		},
		restore: (): boolean => {
			const token = localStorage.getItem("kubetoken")
			if (token) {
				try {
					const decoded = decodeToken(token)
					const user = new User(token, decoded)
					set(user)
					return user.loggedIn
				} catch (err) {
					console.error(`saved token in local storage is not valid JWT: ${err}`)
					return false
				}
			}
			return false
		},
	}
}
