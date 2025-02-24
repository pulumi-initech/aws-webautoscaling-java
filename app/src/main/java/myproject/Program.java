package myproject;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.aws.route53.Route53Functions;
import com.pulumi.aws.route53.inputs.GetZoneArgs;
import com.pulumi.core.Output;
import java.util.List;

import com.pulumi.components.acm.DnsValidatedCertificate;
import com.pulumi.components.acm.inputs.DnsValidatedCertificateArgs;
import com.pulumi.components.web.WebEnvironment;
import com.pulumi.components.web.inputs.WebEnvironmentArgs;


public class Program {

	public static void main(String[] args) {
		Pulumi.run(Program::stack);
	}

	// A simple stack to be tested.
	static void stack(Context ctx) {

		var config = ctx.config();

		var vpcId = config.get("VpcId").orElse("");
		var vpcCidr = config.get("CidrBlock").orElse("");
		var publicSubnetIds = config.getObject("PublicSubnetIds", String[].class).orElse(new String[] {});
		var privateSubnetIds = config.getObject("PrivateSubnetIds", String[].class).orElse(new String[] {});
		var imageId = config.get("imageId").orElse("");
		var instanceType = config.get("instanceType").orElse("m3.medium");
		var instanceCount = config.getInteger("instanceCount").orElse(2);
		var hostedZoneName = config.get("hostedZoneName").orElse("pulumi-ce.team");
		var subdomain = config.get("subDomain").orElse("dev");
		var fqdn = subdomain + "." + hostedZoneName;

		Output<String> zoneIdOutput = Route53Functions.getZone(GetZoneArgs.builder().name(hostedZoneName).build())
				.apply(zone -> Output.of(zone.zoneId()));

		var cert = new DnsValidatedCertificate("cert",
			DnsValidatedCertificateArgs.builder()
				.domainName(Output.of(fqdn))
				.zoneId(Output.of(hostedZoneName))
					.build()
			, null);

		new WebEnvironment("autoscaling",
			WebEnvironmentArgs.builder()
				.vpcId(Output.of(vpcId))
				.vpcCidr(Output.of(vpcCidr))
				.publicSubnetIds(Output.of(List.of(publicSubnetIds)))
				.privateSubnetIds(Output.of(List.of(privateSubnetIds)))
				.imageId(Output.of(imageId))
				.instanceType(Output.of(instanceType))
				.instanceCount(Output.of(instanceCount))
				.subdomain(Output.of(subdomain))
				.zoneId(zoneIdOutput)
				.certificateArn(cert.certificateArn)
				.build()
			, null);

		ctx.export("fqdn", fqdn);
	}
}
