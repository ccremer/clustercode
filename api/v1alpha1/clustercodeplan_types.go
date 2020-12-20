package v1alpha1

import (
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

type (
	// ClustercodePlanSpec defines the desired state of Archive.
	ClustercodePlanSpec struct {
		// +kubebuilder:default=1
		ScanIntervalSeconds int64 `json:"scanIntervalMinutes,omitempty"`
		// +kubebuilder:validation:Required
		SourceVolume corev1.Volume `json:"sourceVolume,omitempty"`
	}

	// ClustercodePlan is the Schema for the archives API
	// +kubebuilder:object:root=true
	// +kubebuilder:subresource:status
	ClustercodePlan struct {
		metav1.TypeMeta   `json:",inline"`
		metav1.ObjectMeta `json:"metadata,omitempty"`

		Spec   ClustercodePlanSpec   `json:"spec,omitempty"`
		Status ClustercodePlanStatus `json:"status,omitempty"`
	}

	// ClustercodePlanList contains a list of Archive
	// +kubebuilder:object:root=true
	ClustercodePlanList struct {
		metav1.TypeMeta `json:",inline"`
		metav1.ListMeta `json:"metadata,omitempty"`
		Items           []ClustercodePlan `json:"items"`
	}
	ClustercodePlanStatus struct {
		Conditions []metav1.Condition `json:"conditions,omitempty"`
	}
)

func init() {
	SchemeBuilder.Register(&ClustercodePlan{}, &ClustercodePlanList{})
}
