Here are currently implemented methods. The version behind the service name denotes the last version in which the service has been touched.

## Task Service @ 0.0.6

* `# createTaskQuery`
* `# claim`
* `# deleteTask`
* `# deleteTasks`
* `# resolveTask`
* `# complete`
* `# saveTask`
* `# setAssignee`
* `# setPriority`
* `# setVariable`
* `# setVariables`
* `# setVariableLocal`
* `# setVariablesLocal`
* `# getVariable`
* `# getVariableLocal`
* `# getVariableTyped`
* `# getVariableLocal`
* `# getVariableLocalTyped`
* `# getVariables`
* `# getVariablesLocal`
* `# getVariablesTyped`
* `# getVariablesLocalTyped`
* `# removeVariable`
* `# removeVariableLocal`
* `# removeVariables`
* `# removeVariablesLocal`
* `# getIdentityLinksForTask`
* `# addCandidateUser`
* `# addCandidateGroup`
* `# addUserIdentityLink`
* `# addGroupIdentityLink`
* `# deleteCandidateUser`
* `# deleteCandidateGroup`
* `# deleteUserIdentityLink`
* `# deleteGroupIdentityLink`
* `# handleBpmnError`
* `# handleEscalation`

## Runtime Service @ 0.0.7

* `# startProcessInstanceByKey`
* `# startProcessInstanceById`
* `# correlateMessage`
* `# createMessageCorrelation`
* `# createProcessInstanceQuery`
* `# signal`
* `# signalEventReceived`
* `# createSignalEvent`
* `# getVariable`
* `# getVariables`
* `# setVariable`
* `# setVariables`
* `# removeVariable`
* `# removeVariables`
* `# getVariableTyped`
* `# getVariablesTyped`
* `# setVariableTyped`
* `# setVariablesTyped`
* `# getVariableLocal`
* `# getVariablesLocal`
* `# setVariableLocal`
* `# setVariablesLocal`
* `# removeVariableLocal`
* `# removeVariablesLocal`
* `# getVariableTypedLocal`
* `# getVariablesTypedLocal`
* `# setVariableTypedLocal`
* `# setVariablesTypedLocal`
* `# createIncidentQuery`
* `# createIncident`
* `# resolveIncident`
* `# setAnnotationForIncidentById`
* `# clearAnnotationForIncidentById`
* `# suspendProcessInstanceByProcessDefinitionKey`
* `# activateProcessInstanceByProcessDefinitionKey`
* `# suspendProcessInstanceByProcessDefinitionId`
* `# activateProcessInstanceByProcessDefinitionId`
* `# suspendProcessInstanceById`
* `# activateProcessInstanceById`
* `# updateProcessInstanceSuspensionState`
* `# deleteProcessInstance`
* `# deleteProcessInstanceIfExists`
* `# deleteProcessInstances`
* `# deleteProcessInstancesIfExists`
* `# deleteProcessInstancesAsync`

## RepositoryService @ 7.20.2

* `# createProcessDefinitionQuery`
* `# updateDecisionDefinitionHistoryTimeToLive`
* `# updateProcessDefinitionHistoryTimeToLive`
* `# createDeployment`
* `# deleteDeployment`
* `# deleteDeploymentCascade`
* `# createDeploymentQuery`
* `# deleteProcessDefinition`
* `# updateProcessDefinitionSuspensionState`
* `# getBpmnModelInstance`

## ExternalTaskService @ 0.0.5

* `# complete`
* `# handleBpmnError`
* `# handleFailure`

## HistoryService @ 0.0.7

* `# createHistoricProcessInstanceQuery`

## DecisionService @ 7.17.3

* `# evaluateDecisionById`
* `# evaluateDecisionByKey`
* `# evaluateDecisionTableById`
* `# evaluateDecisionTableByKey`
* `# evaluateDecisionTableByKeyAndVersion`

## RuntimeService @ 7.17.3

* `# createEventSubscriptionQuery`
* `# createExecutionQuery`
