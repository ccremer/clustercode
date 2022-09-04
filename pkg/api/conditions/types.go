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

func ProgressingSuccessful() metav1.Condition {
	return metav1.Condition{
		Type:    "Progressing",
		Status:  metav1.ConditionFalse,
		Reason:  "AllSlicesCompleted",
		Message: "All planned slices successfully processed",
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

func Ready() metav1.Condition {
	return metav1.Condition{
		Type:    "Ready",
		Status:  metav1.ConditionTrue,
		Reason:  "TaskProcessedSuccessfully",
		Message: "Task has been successfully processed",
	}
}
