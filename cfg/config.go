package cfg

// Configuration holds a strongly-typed tree of the configuration
type (
	Configuration struct {
		Operator OperatorConfig
		Log      LogConfig
	}
	OperatorConfig struct {
		MetricsBindAddress string `koanf:"metrics-bind-address"`

		// Enabling this will ensure there is only one active controller manager.
		EnableLeaderElection bool `koanf:"enable-leader-election"`
	}
	LogConfig struct {
		Debug bool `koanf:"debug"`
	}
	ScanConfig struct {
		ClustercodePlanName string `koanf:"clustercode-plan-name"`
	}
)

var (
	Config = NewDefaultConfig()
)

// NewDefaultConfig retrieves the config with sane defaults
func NewDefaultConfig() *Configuration {
	return &Configuration{
		Operator: OperatorConfig{
			MetricsBindAddress:   ":9090",
			EnableLeaderElection: false,
		},
	}
}

func (c Configuration) ValidateSyntax() error {
	return nil
}
