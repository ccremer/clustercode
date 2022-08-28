package conditions

import (
	"fmt"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func SplitComplete() metav1.Condition {
	return metav1.Condition{
		Type:    "SplitComplete",
		Status:  metav1.ConditionTrue,
		Reason:  "SplitSuccessful",
		Message: "Source file successfully split into multiple slices",
	}
}

func CountComplete(amount int) metav1.Condition {
	return metav1.Condition{
		Type:    "CountComplete",
		Status:  metav1.ConditionTrue,
		Reason:  "CountedIntermediateFiles",
		Message: fmt.Sprintf("Counted slices: %d", amount),
	}
}

func Progressing() metav1.Condition {
	return metav1.Condition{
		Type:    "Progressing",
		Status:  metav1.ConditionTrue,
		Reason:  "SlicesScheduled",
		Message: "Slices are being processed",
	}
}

func MergeComplete() metav1.Condition {
	return metav1.Condition{
		Type:    "MergeComplete",
		Status:  metav1.ConditionTrue,
		Reason:  "MergedIntermediateFiles",
		Message: "Merged slices back together",
	}
}

func CleanupComplete() metav1.Condition {
	return metav1.Condition{
		Type:    "CleanupComplete",
		Status:  metav1.ConditionTrue,
		Reason:  "CleanedUpIntermediateFiles",
		Message: "Cleaned up obsolete intermediate files",
	}
}
