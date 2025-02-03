package myproject;

import com.pulumi.core.Output;
import com.pulumi.resources.ResourceArgs;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class WebEnvironmentArgs extends ResourceArgs {
	private final Output<String> vpcId;

	private final Output<String> vpcCidr;

	private final Output<String> imageId;

	private final Output<Integer> instanceCount;

	private final Output<String> instanceType;

	private final Output<List<String>> publicSubnetIds;

	private final Output<List<String>> privateSubnetIds;

	private final Output<String> certificateArn;

	private final Output<String> zoneId;

	private final Output<String> subdomain;
}
