package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func init() {
	SchemeBuilder.Register(&ClustercodeTask{}, &ClustercodeTaskList{})
}

type (
	// +kubebuilder:object:root=true
	// +kubebuilder:subresource:status
	// +kubebuilder:printcolumn:name="Source",type="string",JSONPath=".spec.sourceUrl",description="Source file name"
	// +kubebuilder:printcolumn:name="Target",type="string",JSONPath=".spec.targetUrl",description="Target file name"
	// +kubebuilder:printcolumn:name="Plan",type="string",JSONPath=`.metadata.ownerReferences[?(@.controller)].name`,description="Clustercode Plan"
	// +kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"

	// ClustercodePlan is the Schema for the archives API
	ClustercodeTask struct {
		metav1.TypeMeta   `json:",inline"`
		metav1.ObjectMeta `json:"metadata,omitempty"`

		Spec   ClustercodeTaskSpec   `json:"spec,omitempty"`
		Status ClustercodeTaskStatus `json:"status,omitempty"`
	}

	// +kubebuilder:object:root=true

	// ClustercodeTaskList contains a list of ClusterCodeTasks
	ClustercodeTaskList struct {
		metav1.TypeMeta `json:",inline"`
		metav1.ListMeta `json:"metadata,omitempty"`
		Items           []ClustercodeTask `json:"items"`
	}

	// EncodingTaskSpec defines the desired state of ClustercodeTask.
	ClustercodeTaskSpec struct {
		StorageSpec StorageSpec    `json:"storageSpec,omitempty"`
		SourceUrl   ClusterCodeUrl `json:"sourceUrl,omitempty"`
		TargetUrl   ClusterCodeUrl `json:"targetUrl,omitempty"`
		Suspend     bool           `json:"suspend,omitempty"`
		EncodeSpec  EncodeSpec     `json:"encodeSpec"`
	}

	ClustercodeTaskStatus struct {
		Conditions      []metav1.Condition `json:"conditions,omitempty"`
		SlicesPlanned   int                `json:"slicesPlanned,omitempty"`
		SlicesScheduled int                `json:"slicesSchedules,omitempty"`
		SlicesFinished  int                `json:"slicesFinished,omitempty"`
	}
)
