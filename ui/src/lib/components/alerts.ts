import { writable } from "svelte/store"
import type { Subscriber, Unsubscriber } from "svelte/types/runtime/store"

export interface AlertStore {
	subscribe(this: void, run: Subscriber<Message[]>, invalidate?: (value?: Message[]) => void): Unsubscriber

	add(msg: Message): void

	remove(msg: Message): void
}

export const alertStore: AlertStore = createAlerts()

export interface Message {
	message: string
	title?: string
	severity?: "error" | "info" | "info-square" | "success" | "warning" | "warning-alt"
	id?: number
	sticky?: boolean
	timeout?: number
}

function createAlerts() {
	const { subscribe, update } = writable<Message[]>([])
	return {
		subscribe,
		add(msg: Message): void {
			msg.id = Math.floor(Math.random() * 10000)
			if (!msg.severity) {
				msg.severity = "info"
			}
			update((arr) => {
				return [...arr, msg]
			})
			if (!msg.sticky) {
				setTimeout(() => {
					this.remove(msg)
				}, msg.timeout ?? 4000)
			}
		},
		remove(msg: Message): void {
			update((arr) => {
				return arr.filter((m) => m.id !== msg.id)
			})
		},
	}
}
