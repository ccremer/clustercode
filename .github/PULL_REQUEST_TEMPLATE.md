## Summary

* Short summary of what's included in the PR
* Give special note to breaking changes: List the exact changes or provide links to documentation.

## Checklist

<!--
Do *not* mix code changes with chart changes, it will break the release process.
Delete the checklist section that doesn't apply to the change.
-->

### For Code changes

- [ ] Categorize the PR by setting a good title and adding one of the labels:
      `kind:bug`, `kind:enhancement`, `kind:documentation`, `kind:change`, `kind:breaking`, `kind:dependency`
      as they show up in the changelog
- [ ] PR contains the label `area:operator`
- [ ] Link this PR to related issues
- [ ] I have not made _any_ changes in the `charts/` directory.

### For Helm Chart changes

- [ ] Categorize the PR by setting a good title and adding one of the labels:
      `kind:bug`, `kind:enhancement`, `kind:documentation`, `kind:change`, `kind:breaking`, `kind:dependency`
      as they show up in the changelog
- [ ] PR contains the label `area:chart`
- [ ] PR contains the chart label, e.g. `chart:clustercode`
- [ ] Variables are documented in the values.yaml using the format required by [Helm-Docs](https://github.com/norwoodj/helm-docs#valuesyaml-metadata).
- [ ] Chart Version bumped if immediate release after merging is planned
- [ ] I have run `make chart-docs`
- [ ] Link this PR to related code release or other issues.

<!--
Remove the section and checklist items that do not apply.
For completed items, change [ ] to [x].

NOTE: these things are not required to open a PR and can be done afterwards,
while the PR is open.
-->
