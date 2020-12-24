package controllers

var(
	ClusterCodeLabels = map[string]string {
		"app.kubernetes.io/managed-by": "clustercode",
	}
)

func mergeLabels(labels ...map[string]string) map[string]string {
	merged := make(map[string]string)
	for _, labelMap := range labels {
		for k, v := range labelMap {
			merged[k] = v
		}
	}
	return merged
}
