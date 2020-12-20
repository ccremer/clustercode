package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

type (

	// EncodingTaskSpec defines the desired state of Archive.
	ClustercodeTaskSpec struct {
		SourceUrl string `json:"sourceUrl,omitempty"`
		TargetUrl string `json:"targetUrl,omitempty"`
		Suspend bool `json:"suspend,omitempty"`
		EncodeSpec `json:"encodeSpec"`
	}

	// ClustercodePlan is the Schema for the archives API
	// +kubebuilder:object:root=true
	// +kubebuilder:subresource:status
	ClustercodeTask struct {
		metav1.TypeMeta   `json:",inline"`
		metav1.ObjectMeta `json:"metadata,omitempty"`

		Spec   ClustercodeTaskSpec   `json:"spec,omitempty"`
		Status ClustercodeTaskStatus `json:"status,omitempty"`
	}
	// ClustercodeTaskList contains a list of Archive
	// +kubebuilder:object:root=true
	ClustercodeTaskList struct {
		metav1.TypeMeta `json:",inline"`
		metav1.ListMeta `json:"metadata,omitempty"`
		Items           []ClustercodeTask `json:"items"`
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
