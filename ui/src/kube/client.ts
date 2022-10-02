import { authToken } from '../stores/AuthStore'
import type { KubeObject } from './object'

let token = ''
authToken.subscribe(value => {
  token = value
})

export class Client {
  async get<T extends KubeObject>(obj: T): Promise<T> {
    const endpoint = `/apis/${obj.apiVersion}/${obj.kind.toLowerCase()}s`
    return await fetch(endpoint, {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      },
      body: JSON.stringify(obj),
      method: 'POST',
    }).then(response => {
      if (response.ok) {
        return response.json() as Promise<T>
      }
      console.log(response.json())
      throw new Error(response.statusText)
    })
  }
}
