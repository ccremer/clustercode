package cfg

// Configuration holds a strongly-typed tree of the configuration
type (
	Configuration struct {
		Operator  OperatorConfig
		Scan      ScanConfig
		Log       LogConfig
		Count     CountConfig
		Cleanup   CleanupConfig
		Namespace string `koanf:"namespace"`
	}
	OperatorConfig struct {
		MetricsBindAddress string `koanf:"metrics-bind-address"`

		// Enabling this will ensure there is only one active controller manager.
		EnableLeaderElection      bool   `koanf:"enable-leader-election"`
		WatchNamespace            string `koanf:"watch-namespace"`
		ClustercodeContainerImage string `koanf:"clustercode-image"`
		FfmpegContainerImage      string `koanf:"ffmpeg-image"`
	}
	LogConfig struct {
		Debug bool `koanf:"debug"`
	}
	ScanConfig struct {
		RoleKind      string `koanf:"role-kind"`
		RoleName      string `koanf:"role-name"`
		BlueprintName string `koanf:"blueprint-name"`
		SourceRoot    string `koanf:"source-root"`
		TargetRoot    string `koanf:"target-root"`
	}
	CountConfig struct {
		TaskName string `koanf:"task-name"`
	}
	CleanupConfig struct {
		TaskName string `koanf:"task-name"`
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
			MetricsBindAddress:   ":9090",
			FfmpegContainerImage: "docker.io/jrottenberg/ffmpeg:4.1-alpine",
			EnableLeaderElection: true,
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
