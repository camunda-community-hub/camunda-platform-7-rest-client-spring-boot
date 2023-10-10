<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/" xmlns:dmndi="https://www.omg.org/spec/DMN/20191111/DMNDI/" xmlns:dc="http://www.omg.org/spec/DMN/20180521/DC/" xmlns:modeler="http://camunda.org/schema/modeler/1.0" xmlns:camunda="http://camunda.org/schema/1.0/dmn" xmlns:di="http://www.omg.org/spec/DMN/20180521/DI/" id="test" name="Test" namespace="http://camunda.org/schema/1.0/dmn" exporter="Camunda Modeler" exporterVersion="5.14.0" modeler:executionPlatform="Camunda Platform" modeler:executionPlatformVersion="7.19.0">
  <decision id="table1" name="Table 1" camunda:versionTag="1">
    <decisionTable id="DecisionTable_0pwcv54">
      <input id="Input_1" label="Input 1" camunda:inputVariable="input1">
        <inputExpression id="InputExpression_1" typeRef="string">
          <text></text>
        </inputExpression>
      </input>
      <output id="Output_1" label="Output 1" name="output1" typeRef="string" />
      <rule id="DecisionRule_1l3y2km">
        <inputEntry id="UnaryTests_0o8jbva">
          <text>"Rule1"</text>
        </inputEntry>
        <outputEntry id="LiteralExpression_1p1mhdg">
          <text>"Rule1Output"</text>
        </outputEntry>
      </rule>
    </decisionTable>
  </decision>
  <decision id="table2" name="Table 2" camunda:versionTag="1">
    <informationRequirement id="InformationRequirement_1nhgmin">
      <requiredDecision href="#table1" />
    </informationRequirement>
    <decisionTable id="DecisionTable_1ykqhh8">
      <input id="InputClause_1fe5omf" label="Output 1" camunda:inputVariable="output1">
        <inputExpression id="LiteralExpression_09u7xh0" typeRef="string">
          <text></text>
        </inputExpression>
      </input>
      <output id="OutputClause_0uit40j" label="Output 2" name="output2" typeRef="string" />
      <rule id="DecisionRule_1ckyfgd">
        <inputEntry id="UnaryTests_0x55e4s">
          <text>"Rule1Output"</text>
        </inputEntry>
        <outputEntry id="LiteralExpression_1a3h8yh">
          <text>"Rule1Output2"</text>
        </outputEntry>
      </rule>
    </decisionTable>
  </decision>
  <dmndi:DMNDI>
    <dmndi:DMNDiagram>
      <dmndi:DMNShape dmnElementRef="table1">
        <dc:Bounds height="80" width="180" x="160" y="100" />
      </dmndi:DMNShape>
      <dmndi:DMNShape id="DMNShape_005tcx7" dmnElementRef="table2">
        <dc:Bounds height="80" width="180" x="160" y="230" />
      </dmndi:DMNShape>
      <dmndi:DMNEdge id="DMNEdge_1dk4k4p" dmnElementRef="InformationRequirement_1nhgmin">
        <di:waypoint x="250" y="180" />
        <di:waypoint x="250" y="210" />
        <di:waypoint x="250" y="230" />
      </dmndi:DMNEdge>
    </dmndi:DMNDiagram>
  </dmndi:DMNDI>
</definitions>
