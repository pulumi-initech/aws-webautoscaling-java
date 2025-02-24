package com.pulumi.components.web.inputs;

import com.pulumi.core.Output;
import com.pulumi.resources.ResourceArgs;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebEnvironmentArgs extends ResourceArgs {
	private Output<String> vpcId;

	private Output<String> vpcCidr;

	private Output<String> imageId;

	private Output<Integer> instanceCount;

	private Output<String> instanceType;

	private Output<List<String>> publicSubnetIds;

	private Output<List<String>> privateSubnetIds;

	private Output<String> certificateArn;

	private Output<String> zoneId;

	private Output<String> subdomain;
}
