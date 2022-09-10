package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
)

func init() {
	SchemeBuilder.Register(&Task{}, &TaskList{})
}

// +kubebuilder:object:root=true
// +kubebuilder:subresource:status
// +kubebuilder:printcolumn:name="Source",type="string",JSONPath=".spec.sourceUrl",description="Source file name"
// +kubebuilder:printcolumn:name="Target",type="string",JSONPath=".spec.targetUrl",description="Target file name"
// +kubebuilder:printcolumn:name="Blueprint",type="string",JSONPath=`.metadata.ownerReferences[?(@.kind=='Blueprint')].name`,description="Blueprint reference"
// +kubebuilder:printcolumn:name="Slices",type="string",JSONPath=`.spec.slicesPlannedCount`,description="Clustercode Total Slices"
// +kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"

// Task is a projection out of a Blueprint
type Task struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   TaskSpec   `json:"spec,omitempty"`
	Status TaskStatus `json:"status,omitempty"`
}

// +kubebuilder:object:root=true

// TaskList contains a list of Task
type TaskList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Task `json:"items"`
}

// TaskSpec defines the desired state of Task.
type TaskSpec struct {
	TaskId               ClustercodeTaskId   `json:"taskId,omitempty"`
	Storage              StorageSpec         `json:"storage,omitempty"`
	SourceUrl            ClusterCodeUrl      `json:"sourceUrl,omitempty"`
	TargetUrl            ClusterCodeUrl      `json:"targetUrl,omitempty"`
	Suspend              bool                `json:"suspend,omitempty"`
	Encode               EncodeSpec          `json:"encode,omitempty"`
	Cleanup              CleanupSpec         `json:"cleanup,omitempty"`
	ServiceAccountName   string              `json:"serviceAccountName,omitempty"`
	FileListConfigMapRef string              `json:"fileListConfigMapRef,omitempty"`
	ConcurrencyStrategy  ClustercodeStrategy `json:"concurrencyStrategy,omitempty"`
	SlicesPlannedCount   int                 `json:"slicesPlannedCount,omitempty"`
}

type TaskStatus struct {
	Conditions           []metav1.Condition    `json:"conditions,omitempty"`
	SlicesScheduledCount int                   `json:"slicesScheduledCount,omitempty"`
	SlicesFinishedCount  int                   `json:"slicesFinishedCount,omitempty"`
	SlicesScheduled      []ClustercodeSliceRef `json:"slicesScheduled,omitempty"`
	SlicesFinished       []ClustercodeSliceRef `json:"slicesFinished,omitempty"`
}

type ClustercodeSliceRef struct {
	JobName    string `json:"jobName,omitempty"`
	SliceIndex int    `json:"sliceIndex"`
}

type ClustercodeStrategy struct {
	ConcurrentCountStrategy *ClustercodeCountStrategy `json:"concurrentCountStrategy,omitempty"`
}

type ClustercodeCountStrategy struct {
	MaxCount int `json:"maxCount,omitempty"`
}
type ClustercodeTaskId string

const (
	ClustercodeTaskIdLabelKey = "clustercode.github.io/task-id"
)

func (id ClustercodeTaskId) AsLabels() labels.Set {
	return map[string]string{
		ClustercodeTaskIdLabelKey: id.String(),
	}
}

func (id ClustercodeTaskId) String() string {
	return string(id)
}
