package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

type (
	// +kubebuilder:object:root=true
	// +kubebuilder:subresource:status

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
		SourceUrl  string `json:"sourceUrl,omitempty"`
		TargetUrl  string `json:"targetUrl,omitempty"`
		Suspend    bool   `json:"suspend,omitempty"`
		EncodeSpec `json:"encodeSpec"`
	}

	ClustercodeTaskStatus struct {
		Conditions          []metav1.Condition `json:"conditions,omitempty"`
		SourceMediaFileName string             `json:"sourceMediaFileName,omitempty"`
		TargetMediaFileName string             `json:"targetMediaFileName,omitempty"`
		SliceCount          int                `json:"sliceCount,omitempty"`
	}
)

func init() {
	SchemeBuilder.Register(&ClustercodeTask{}, &ClustercodeTaskList{})
}
