package v1alpha1

import (
	corev1 "k8s.io/api/core/v1"
)

type PodTemplate struct {
	// +kubebuilder:validation:Optional

	// Metadata that will be added to the Job's Pod.
	Metadata *PodMetadata `json:"metadata,omitempty"`

	// +kubebuilder:validation:Optional

	// PodSecurityContext holds pod-level security attributes and common container settings.
	// Defaults to empty.
	// See type description for default values of each field.
	PodSecurityContext *corev1.PodSecurityContext `json:"podSecurityContext,omitempty"`

	// +kubebuilder:validation:Optional

	// Containers configures the init containers within a Pod.
	InitContainers []ContainerTemplate `json:"initContainers,omitempty" patchStrategy:"merge" patchMergeKey:"name"`

	// +kubebuilder:validation:Optional

	// Containers configures the containers within a Pod.
	Containers []ContainerTemplate `json:"containers,omitempty" patchStrategy:"merge" patchMergeKey:"name"`

	// +kubebuilder:validation:Optional

	// List of volumes that can be mounted by containers belonging to the pod.
	// More info: https://kubernetes.io/docs/concepts/storage/volumes
	Volumes []corev1.Volume `json:"volumes,omitempty" patchStrategy:"merge" patchMergeKey:"name"`

	// +kubebuilder:validation:Optional

	// If specified, the pod's scheduling constraints
	Affinity *corev1.Affinity `json:"affinity,omitempty"`

	// +kubebuilder:validation:Optional

	// NodeSelector is a selector which must be true for the pod to fit on a node.
	// Selector which must match a node's labels for the pod to be scheduled on that node.
	// More info: https://kubernetes.io/docs/concepts/configuration/assign-pod-node/
	NodeSelector map[string]string `json:"nodeSelector,omitempty"`

	// +kubebuilder:validation:Optional

	// If specified, the pod's tolerations.
	Tolerations []corev1.Toleration `json:"tolerations,omitempty"`
}

type PodMetadata struct {
	// Map of string keys and values that can be used to organize and categorize (scope and select) objects.
	// This will only affect labels on the pod, not the pod selector.
	// Labels will be merged with internal labels used by crossplane, and labels with a `clustercode.github.io` key might be overwritten.
	// More info: http://kubernetes.io/docs/user-guide/labels
	Labels map[string]string `json:"labels,omitempty"`

	// Annotations is an unstructured key value map stored with a Pod that may be set by external tools to store and retrieve arbitrary metadata.
	// More info: http://kubernetes.io/docs/user-guide/annotations
	Annotations map[string]string `json:"annotations,omitempty"`
}

type ContainerTemplate struct {
	// Name of the container specified as a DNS_LABEL.
	// Each container in a pod must have a unique name (DNS_LABEL).
	Name string `json:"name"`

	// +kubebuilder:validation:Optional

	// Image name.
	// More info: https://kubernetes.io/docs/concepts/containers/images
	Image string `json:"image,omitempty"`

	// +kubebuilder:validation:Optional

	// Command Entrypoint array.
	// More info: https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#running-a-command-in-a-shell
	Command []string `json:"command,omitempty"`

	// +kubebuilder:validation:Optional

	// Args to the entrypoint.
	// More info: https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#running-a-command-in-a-shell
	Args []string `json:"args,omitempty"`

	// +kubebuilder:validation:Optional

	// Ports is a List of ports to expose from the container.
	Ports []corev1.ContainerPort `json:"ports,omitempty" patchStrategy:"merge" patchMergeKey:"containerPort"`

	// +kubebuilder:validation:Optional

	// EnvFrom is a List of sources to populate environment variables in the container.
	EnvFrom []corev1.EnvFromSource `json:"envFrom,omitempty"`

	// +kubebuilder:validation:Optional

	// Env is a list of environment variables to set in the container.
	Env []corev1.EnvVar `json:"env,omitempty" patchStrategy:"merge" patchMergeKey:"name"`

	// +kubebuilder:validation:Optional

	// ImagePullPolicy.
	// One of Always, Never, IfNotPresent.
	// Defaults to Always if `latest` tag is specified, or `IfNotPresent` otherwise.
	// More info: https://kubernetes.io/docs/concepts/containers/images#updating-images
	ImagePullPolicy corev1.PullPolicy `json:"imagePullPolicy,omitempty"`

	// +kubebuilder:validation:Optional

	// VolumeMounts to mount into the container's filesystem.
	VolumeMounts []corev1.VolumeMount `json:"volumeMounts,omitempty" patchStrategy:"merge" patchMergeKey:"mountPath"`

	// Compute Resources required by this container.
	// More info: https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/
	Resources corev1.ResourceRequirements `json:"resources,omitempty"`

	// +kubebuilder:validation:Optional

	// SecurityContext defines the security options the container should be run with.
	// If set, the fields of SecurityContext override the equivalent fields of PodSecurityContext.
	// More info: https://kubernetes.io/docs/tasks/configure-pod-container/security-context/
	SecurityContext *corev1.SecurityContext `json:"securityContext,omitempty"`
}

// ToContainer returns a native Container based from the template.
func (in *ContainerTemplate) ToContainer() corev1.Container {
	return corev1.Container{
		Name:            in.Name,
		Image:           in.Image,
		Command:         in.Command,
		Args:            in.Args,
		Ports:           in.Ports,
		EnvFrom:         in.EnvFrom,
		Env:             in.Env,
		Resources:       in.Resources,
		VolumeMounts:    in.VolumeMounts,
		ImagePullPolicy: in.ImagePullPolicy,
		SecurityContext: in.SecurityContext,
	}
}
