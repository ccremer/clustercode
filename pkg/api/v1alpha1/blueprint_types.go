package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func init() {
	SchemeBuilder.Register(&Blueprint{}, &BlueprintList{})
}

// +kubebuilder:object:root=true
// +kubebuilder:subresource:status
// +kubebuilder:printcolumn:name="Schedule",type="string",JSONPath=".spec.scanSchedule",description="Cron schedule of media scans"
// +kubebuilder:printcolumn:name="Suspended",type="boolean",JSONPath=".spec.suspend",description="Whether media scanning is suspended"
// +kubebuilder:printcolumn:name="Current Tasks",type="integer",JSONPath=".status.currentTasks",description="Currently active tasks"
// +kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"

// Blueprint is the Schema for the Blueprint API
type Blueprint struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   BlueprintSpec   `json:"spec,omitempty"`
	Status BlueprintStatus `json:"status,omitempty"`
}

// +kubebuilder:object:root=true

// BlueprintList contains a list of Blueprints.
type BlueprintList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Blueprint `json:"items"`
}

// BlueprintSpec specifies Clustercode settings
type BlueprintSpec struct {
	// +kubebuilder:validation:Required
	Storage StorageSpec `json:"storage,omitempty"`
	// +kubebuilder:default=1
	MaxParallelTasks int `json:"maxParallelTasks,omitempty"`

	Suspend                 bool                `json:"suspend,omitempty"`
	TaskConcurrencyStrategy ClustercodeStrategy `json:"taskConcurrencyStrategy,omitempty"`

	Scan    ScanSpec    `json:"scan,omitempty"`
	Encode  EncodeSpec  `json:"encode,omitempty"`
	Cleanup CleanupSpec `json:"cleanup,omitempty"`
}

type ScanSpec struct {
	Schedule string `json:"schedule"`
	// +kubebuilder:default=mkv;mp4;avi
	MediaFileExtensions []string `json:"mediaFileExtensions,omitempty"`
}

type CleanupSpec struct {
	// PodTemplate contains a selection of fields to customize the spawned ffmpeg-based pods.
	// Some fields will be overwritten:
	//  * Volumes and volume mounts will be set based on StorageSpec.
	//  * Container args of the `ffmpeg` container will be set based on SplitCommandArgs, TranscodeCommandArgs, MergeCommandArgs.
	PodTemplate PodTemplate `json:"podTemplate,omitempty"`
}

type BlueprintStatus struct {
	Conditions   []metav1.Condition `json:"conditions,omitempty"`
	CurrentTasks []TaskRef          `json:"currentTasks,omitempty"`
}

type TaskRef struct {
	TaskName string `json:"taskName,omitempty"`
}

// IsMaxParallelTaskLimitReached will return true if the count of current task has reached MaxParallelTasks.
func (in *Blueprint) IsMaxParallelTaskLimitReached() bool {
	return len(in.Status.CurrentTasks) >= in.Spec.MaxParallelTasks
}

// GetServiceAccountName retrieves a ServiceAccount name that would go along with this Blueprint.
func (in *Blueprint) GetServiceAccountName() string {
	return in.Name + "-clustercode"
}
