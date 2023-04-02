import type { Condition, KubeObject } from "../object"

export interface Blueprint extends KubeObject {
	kind: "Blueprint"
	apiVersion: "clustercode.github.io/v1alpha1"
	spec?: BlueprintSpec
	status?: BlueprintStatus
}

export interface BlueprintSpec {
	taskConcurrencyStrategy: ClustercodeStrategy
	scan?: ScanSpec
	cleanup?: CleanupSpec
}

export interface ClustercodeStrategy {
	concurrentCountStrategy?: unknown
}

export interface ScanSpec {
	schedule: string
	mediaFileExtensions?: string[]
}

export interface BlueprintStatus {
	conditions?: Condition[]
	currentTasks?: TaskRef[]
}

export interface TaskRef {
	taskName?: string
}

export interface CleanupSpec {
	podTemplate?: object
}

export function newBlueprint(name?: string, namespace?: string): Blueprint {
	return {
		kind: "Blueprint",
		apiVersion: "clustercode.github.io/v1alpha1",
		metadata: {
			name: name ?? "",
			namespace: namespace,
		},
	}
}

/*
"spec": {
  "cleanup": {
    "podTemplate": {
      "containers": [
        {
          "imagePullPolicy": "IfNotPresent",
          "name": "clustercode",
          "resources": {},
          "securityContext": {
            "runAsUser": 1000
          }
        }
      ]
    }
  },
  "encode": {
    "mergeCommandArgs": [
      "-y",
      "-hide_banner",
      "-nostats",
      "-f",
      "concat",
      "-safe",
      "0",
      "-i",
      "${INPUT}",
      "-c",
      "copy",
      "${OUTPUT}"
    ],
      "podTemplate": {
      "containers": [
        {
          "imagePullPolicy": "IfNotPresent",
          "name": "clustercode",
          "resources": {},
          "securityContext": {
            "runAsUser": 1000
          }
        }
      ]
    },
    "sliceSize": 1,
      "splitCommandArgs": [
      "-y",
      "-hide_banner",
      "-nostats",
      "-i",
      "${INPUT}",
      "-c",
      "copy",
      "-map",
      "0",
      "-segment_time",
      "${SLICE_SIZE}",
      "-f",
      "segment",
      "${OUTPUT}"
    ],
      "transcodeCommandArgs": [
      "-y",
      "-hide_banner",
      "-nostats",
      "-i",
      "${INPUT}",
      "-c:v",
      "copy",
      "-c:a",
      "copy",
      "${OUTPUT}"
    ]
  },
  "maxParallelTasks": 1,
    "scan": {
    "mediaFileExtensions": [
      "mp4"
    ],
      "schedule": "*!/1 * * * *"
  },
  "storage": {
    "intermediatePvc": {
      "claimName": "intermediate",
        "subPath": "intermediate"
    },
    "sourcePvc": {
      "claimName": "source",
        "subPath": "source"
    },
    "targetPvc": {
      "claimName": "target",
        "subPath": "target"
    }
  },
  "taskConcurrencyStrategy": {
    "concurrentCountStrategy": {
      "maxCount": 1
    }
  }
}
*/
