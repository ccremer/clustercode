package webui

// Settings controls various aspects of the frontend.
type Settings struct {
	// AuthCookieMaxAge sets the max-age when saving the cookie after a successful login.
	// This basically controls how long a user does not need to re-login.
	AuthCookieMaxAge int `json:"authCookieMaxAge"`
}
