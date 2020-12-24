package cfg

// Configuration holds a strongly-typed tree of the configuration
type (
	Configuration struct {
		Operator OperatorConfig
		Scan     ScanConfig
		Log      LogConfig
	}
	OperatorConfig struct {
		MetricsBindAddress string `koanf:"metrics-bind-address"`

		// Enabling this will ensure there is only one active controller manager.
		EnableLeaderElection bool   `koanf:"enable-leader-election"`
		WatchNamespace       string `koanf:"watch-namespace"`
	}
	LogConfig struct {
		Debug bool `koanf:"debug"`
	}
	ScanConfig struct {
		RoleKind            string `koanf:"role-kind"`
		RoleName            string `koanf:"role-name"`
		ClustercodePlanName string `koanf:"clustercode-plan-name"`
		Namespace           string `koanf:"namespace"`
		SourceRoot          string `koanf:"source-root"`
		TargetRoot          string `koanf:"target-root"`
	}
)

var (
	Config = NewDefaultConfig()
)

const (
	ClusterRole = "ClusterRole"
	Role        = "Role"
)

// NewDefaultConfig retrieves the config with sane defaults
func NewDefaultConfig() *Configuration {
	return &Configuration{
		Operator: OperatorConfig{
			MetricsBindAddress: ":9090",
		},
		Scan: ScanConfig{
			SourceRoot: "/clustercode",
			RoleName:   "clustercode-clustercode-editor-role",
			RoleKind:   "ClusterRole",
		},
	}
}

func (c Configuration) ValidateSyntax() error {
	return nil
}
