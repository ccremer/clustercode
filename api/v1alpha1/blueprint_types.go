package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func init() {
	SchemeBuilder.Register(&Blueprint{}, &BlueprintList{})
}

type (
	// +kubebuilder:object:root=true
	// +kubebuilder:subresource:status
	// +kubebuilder:printcolumn:name="Schedule",type="string",JSONPath=".spec.scanSchedule",description="Cron schedule of media scans"
	// +kubebuilder:printcolumn:name="Suspended",type="boolean",JSONPath=".spec.suspend",description="Whether media scanning is suspended"
	// +kubebuilder:printcolumn:name="Current Tasks",type="integer",JSONPath=".status.currentTasks",description="Currently active tasks"
	// +kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"

	// Blueprint is the Schema for the Blueprint API
	Blueprint struct {
		metav1.TypeMeta   `json:",inline"`
		metav1.ObjectMeta `json:"metadata,omitempty"`

		Spec   BlueprintSpec   `json:"spec,omitempty"`
		Status BlueprintStatus `json:"status,omitempty"`
	}

	// +kubebuilder:object:root=true

	// BlueprintList contains a list of Blueprints.
	BlueprintList struct {
		metav1.TypeMeta `json:",inline"`
		metav1.ListMeta `json:"metadata,omitempty"`
		Items           []Blueprint `json:"items"`
	}

	// BlueprintSpec specifies Clustercode settings
	BlueprintSpec struct {
		ScanSchedule string `json:"scanSchedule"`
		// +kubebuilder:validation:Required
		Storage StorageSpec `json:"storage,omitempty"`
		// +kubebuilder:default=1
		MaxParallelTasks int `json:"maxParallelTasks,omitempty"`

		Suspend                 bool                `json:"suspend,omitempty"`
		TaskConcurrencyStrategy ClustercodeStrategy `json:"taskConcurrencyStrategy,omitempty"`

		ScanSpec   ScanSpec   `json:"scanSpec,omitempty"`
		EncodeSpec EncodeSpec `json:"encodeSpec,omitempty"`
	}

	ScanSpec struct {
		// +kubebuilder:default=mkv;mp4;avi
		MediaFileExtensions []string `json:"mediaFileExtensions,omitempty"`
	}

	BlueprintStatus struct {
		Conditions   []metav1.Condition   `json:"conditions,omitempty"`
		CurrentTasks []ClusterCodeTaskRef `json:"currentTasks,omitempty"`
	}

	ClusterCodeTaskRef struct {
		TaskName string `json:"taskName,omitempty"`
	}
)

// IsMaxParallelTaskLimitReached will return true if the count of current task has reached MaxParallelTasks.
func (in *Blueprint) IsMaxParallelTaskLimitReached() bool {
	return len(in.Status.CurrentTasks) >= in.Spec.MaxParallelTasks
}

// GetServiceAccountName retrieves a ServiceAccount name that would go along with this Blueprint.
func (in *Blueprint) GetServiceAccountName() string {
	return in.Name + "-clustercode"
}
