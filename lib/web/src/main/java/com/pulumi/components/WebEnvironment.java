package com.pulumi.components.web;
import com.pulumi.aws.alb.Listener;
import com.pulumi.aws.alb.ListenerArgs;
import com.pulumi.aws.alb.LoadBalancer;
import com.pulumi.aws.alb.LoadBalancerArgs;
import com.pulumi.aws.alb.TargetGroup;
import com.pulumi.aws.alb.TargetGroupArgs;
import com.pulumi.aws.autoscaling.Policy;
import com.pulumi.aws.autoscaling.PolicyArgs;
import com.pulumi.aws.alb.inputs.ListenerDefaultActionArgs;
import com.pulumi.aws.alb.inputs.ListenerDefaultActionRedirectArgs;
import com.pulumi.aws.autoscaling.Attachment;
import com.pulumi.aws.autoscaling.AttachmentArgs;
import com.pulumi.aws.autoscaling.Group;
import com.pulumi.aws.autoscaling.GroupArgs;
import com.pulumi.aws.autoscaling.inputs.GroupLaunchTemplateArgs;
import com.pulumi.aws.cloudwatch.MetricAlarm;
import com.pulumi.aws.cloudwatch.MetricAlarmArgs;
import com.pulumi.aws.ec2.KeyPair;
import com.pulumi.aws.ec2.KeyPairArgs;
import com.pulumi.aws.ec2.LaunchTemplate;
import com.pulumi.aws.ec2.LaunchTemplateArgs;
import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.inputs.SecurityGroupEgressArgs;
import com.pulumi.aws.ec2.inputs.SecurityGroupIngressArgs;
import com.pulumi.aws.route53.Record;
import com.pulumi.aws.route53.RecordArgs;
import com.pulumi.aws.route53.inputs.RecordAliasArgs;
import com.pulumi.components.web.inputs.WebEnvironmentArgs;
import com.pulumi.core.Output;
import com.pulumi.resources.ComponentResource;
import com.pulumi.resources.ComponentResourceOptions;
import com.pulumi.resources.CustomResourceOptions;
import com.pulumi.tls.PrivateKey;
import com.pulumi.tls.PrivateKeyArgs;

import lombok.val;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class WebEnvironment extends ComponentResource {
  public WebEnvironment(String name, WebEnvironmentArgs args, ComponentResourceOptions options) {
    super("web:index:WebEnvironment", name, options);

    var albSg =
        new SecurityGroup(
            name + "-alb-sg",
            new SecurityGroupArgs.Builder()
                .vpcId(args.getVpcId())
                .ingress(
                    new SecurityGroupIngressArgs.Builder()
                        .protocol("TCP")
                        .fromPort(443)
                        .toPort(443)
                        .cidrBlocks("0.0.0.0/0")
                        .build(),
                    new SecurityGroupIngressArgs.Builder()
                        .protocol("TCP")
                        .fromPort(80)
                        .toPort(80)
                        .cidrBlocks("0.0.0.0/0")
                        .build())
                .egress(
                    new SecurityGroupEgressArgs.Builder()
                        .protocol("-1")
                        .fromPort(0)
                        .toPort(0)
                        .cidrBlocks("0.0.0.0/0")
                        .build())
                .build(),
            CustomResourceOptions.builder()
                .retainOnDelete(true)
                .protect(true)
                .parent(this)
                .build());

    var instanceSg =
        new SecurityGroup(
            name + "-instance-sg",
            new SecurityGroupArgs.Builder()
                .vpcId(args.getVpcId())
                .ingress(
                    new SecurityGroupIngressArgs.Builder()
                        .protocol("TCP")
                        .fromPort(22)
                        .toPort(22)
                        .cidrBlocks(args.getVpcCidr().apply(c -> Output.of(List.of(c))))
                        .build(),
                    new SecurityGroupIngressArgs.Builder()
                        .protocol("TCP")
                        .fromPort(80)
                        .toPort(80)
                        .securityGroups(Output.all(albSg.id()))
                        .build())
                .egress(
                    new SecurityGroupEgressArgs.Builder()
                        .protocol("-1")
                        .fromPort(0)
                        .toPort(0)
                        .cidrBlocks("0.0.0.0/0")
                        .build())
                .build(),
            CustomResourceOptions.builder().parent(this).build());


    var material =
        new PrivateKey(
            name,
            new PrivateKeyArgs.Builder().algorithm("RSA").build(),
            CustomResourceOptions.builder().parent(this).build());

    var sshKey =
        new KeyPair(
            name,
            new KeyPairArgs.Builder().publicKey(material.publicKeyOpenssh()).build(),
            CustomResourceOptions.builder().parent(material).build());

    var launchTemplate =
        new LaunchTemplate(
            "launch-config",
            new LaunchTemplateArgs.Builder()
                .namePrefix(name + "-web")
                .instanceType(args.getInstanceType())
                .imageId(args.getImageId())
                .keyName(sshKey.keyName())
                .vpcSecurityGroupIds(Output.all(instanceSg.id()))
                .tags(Map.of("Role", "Webserver"))
                .userData(
                    new String(
                        Base64.getEncoder()
                            .encode(
                                """
                        #!/bin/bash
                        sudo yum update -y
                        sudo amazon-linux-extras install nginx1 -y
                        sudo systemctl enable nginx
                        sudo systemctl start nginx"""
                                    .getBytes())))
                .build(),
            CustomResourceOptions.builder().parent(this).build());

    var asg =
        new Group(
            name + "-asg",
            new GroupArgs.Builder()
                .vpcZoneIdentifiers(args.getPublicSubnetIds())
                .desiredCapacity(1)
                .maxSize(1)
                .minSize(1)
                .launchTemplate(
                    new GroupLaunchTemplateArgs.Builder()
                        .id(launchTemplate.id())
                        .version("$Latest")
                        .build())
                .build(),
            CustomResourceOptions.builder().parent(launchTemplate).build());



    var alb =
        new LoadBalancer(
            name + "-alb",
            new LoadBalancerArgs.Builder()
                .internal(false)
                .securityGroups(Output.all(albSg.id()))
                .subnets(args.getPublicSubnetIds())
                .build(),
            CustomResourceOptions.builder().parent(this).build());

	var scaleOutPolicy = new Policy("scaleOutPolicy", PolicyArgs.builder()
			.adjustmentType("ChangeInCapacity")
			.scalingAdjustment(1)
			.autoscalingGroupName(asg.name())
			.build(),
            CustomResourceOptions.builder().parent(asg).build());

	var scaleInPolicy = new Policy("scaleInPolicy", PolicyArgs.builder()
			.adjustmentType("ChangeInCapacity")
			.scalingAdjustment(-1)
			.cooldown(180)
			.autoscalingGroupName(asg.name())
			.build(),
            CustomResourceOptions.builder().parent(asg).build());

	var scaleOutAlarm = new MetricAlarm("albHighRequestAlarm", MetricAlarmArgs.builder()
		.name("alb-high-requests-alarm")
		.namespace("AWS/ApplicationELB")
		.metricName("RequestCount")
		.dimensions(Map.of("LoadBalancer", name + "-alb"))
		.period(180)
		.evaluationPeriods(2)
		.threshold(1000.0)
		.comparisonOperator("GreaterThanOrEqualToThreshold")
		.statistic("Sum")
		.alarmActions(scaleOutPolicy.arn().apply(arn -> Output.of(List.of(arn))))
		.build(),
		CustomResourceOptions.builder().parent(scaleOutPolicy).build());

	var scaleInAlarm = new MetricAlarm("albLowRequestAlarm", MetricAlarmArgs.builder()
		.name("alb-low-requests-alarm")
		.namespace("AWS/ApplicationELB")
		.metricName("RequestCount")
		.dimensions(Map.of("LoadBalancer", name + "-alb"))
		.period(180)
		.evaluationPeriods(2)
		.threshold(500.0)
		.comparisonOperator("LessThanOrEqualToThreshold")
		.statistic("Sum")
		.alarmActions(scaleInPolicy.arn().apply(arn -> Output.of(List.of(arn))))
		.build(),
		CustomResourceOptions.builder().parent(scaleInPolicy).build());

    var tg =
        new TargetGroup(
            name + "-tg",
            new TargetGroupArgs.Builder()
                .targetType("instance")
                .port(80)
                .protocol("HTTP")
                .vpcId(args.getVpcId())
                .build(),
            CustomResourceOptions.builder().parent(alb).build());

    new Listener(
        name + "-frontend-https",
        new ListenerArgs.Builder()
            .loadBalancerArn(alb.arn())
            .port(443)
            .protocol("HTTPS")
            .certificateArn(args.getCertificateArn())
            .defaultActions(
                new ListenerDefaultActionArgs.Builder()
                    .type("forward")
                    .targetGroupArn(tg.arn())
                    .build())
            .build(),
        CustomResourceOptions.builder().parent(alb).build());

    new Listener(
        name + "-frontend-redir",
        new ListenerArgs.Builder()
            .loadBalancerArn(alb.arn())
            .port(80)
            .protocol("HTTP")
            .defaultActions(
                new ListenerDefaultActionArgs.Builder()
                    .type("redirect")
                    .redirect(
                        new ListenerDefaultActionRedirectArgs.Builder()
                            .protocol("HTTPS")
                            .port("443")
                            .statusCode("HTTP_301")
                            .build())
                    .build())
            .build(),
        CustomResourceOptions.builder().parent(alb).build());

    new Attachment(
        name + "-alb-att",
        new AttachmentArgs.Builder()
            .autoscalingGroupName(asg.name())
            .lbTargetGroupArn(tg.arn())
            .build(),
        CustomResourceOptions.builder().parent(asg).build());

    if (args.getZoneId() != null && args.getSubdomain() != null) {
      new Record(
          "alias",
          new RecordArgs.Builder()
              .zoneId(args.getZoneId())
              .name(args.getSubdomain())
              .type("A")
              .aliases(
                  new RecordAliasArgs.Builder()
                      .name(alb.dnsName())
                      .zoneId(alb.zoneId())
                      .evaluateTargetHealth(true)
                      .build())
              .build(),
          CustomResourceOptions.builder().parent(this).build());
    }

    this.registerOutputs(Map.of());
  }
}
