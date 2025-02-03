package myproject;

import static com.pulumi.test.PulumiTest.extractValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatPredicate;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.outputs.SecurityGroupIngress;
import com.pulumi.test.Mocks;
import com.pulumi.test.PulumiTest;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class InfraTest {

	@AfterAll
	static void cleanup() {
		PulumiTest.cleanup();
	}

	@TestFactory
	Stream<DynamicTest> testInstanceHasNameTag() {
		var result = PulumiTest.withMocks(new MyMocks()).runTest(Program::stack);

		var webEnvironment = result.resources().stream()
				.filter(r -> r instanceof WebEnvironment)
				.map(r -> (WebEnvironment) r)
				.findFirst();
		assertThat(webEnvironment).isPresent();

		return Stream.of(
				dynamicTest(
						"type is correct",
						() -> {
							assertThat(webEnvironment.get().pulumiResourceType())
									.isSameAs("custom:resource:Webserver");
						}),

				dynamicTest(
						"instance security group must not have ssh ports open to internet",
						() -> {
							var children = webEnvironment.get().pulumiChildResources();
							children.stream()
									.filter(r -> r instanceof SecurityGroup)
									.map(r -> (SecurityGroup) r)
									.forEach(
											sg -> {
												var name = extractValue(sg.id());
												if(name == "autoscaling-instance-sg") {
													var ingress = extractValue(sg.ingress());
													assertThatPredicate(
															(SecurityGroupIngress r) -> !(r.fromPort() == 22
																	&& r.cidrBlocks().stream()
																			.anyMatch(b -> b.equals("0.0.0.0/0"))))
															.describedAs(
																	"Illegal SSH port 22 open to the Internet (CIDR 0.0.0.0/0) on group %s",
																	extractValue(sg.getUrn()))
															.acceptsAll(ingress);
												}
											});
						}));
	}

	// Mock the engine state.
	public static class MyMocks implements Mocks {
		@Override
		public CompletableFuture<ResourceResult> newResourceAsync(ResourceArgs args) {
			return CompletableFuture.completedFuture(
					ResourceResult.of(Optional.of(args.name + "_id"), args.inputs));
		}
	}
}
