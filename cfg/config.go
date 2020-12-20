package cfg

// Configuration holds a strongly-typed tree of the configuration
type Configuration struct {
	MetricsBindAddress string `koanf:"metrics-bindaddress"`

	// Enabling this will ensure there is only one active controller manager.
	EnableLeaderElection bool `koanf:"enable-leader-election"`
}

var (
	Config = NewDefaultConfig()
)

// NewDefaultConfig retrieves the config with sane defaults
func NewDefaultConfig() *Configuration {
	return &Configuration{
		EnableLeaderElection: true,
	}
}

func (c Configuration) ValidateSyntax() error {
	return nil
}
