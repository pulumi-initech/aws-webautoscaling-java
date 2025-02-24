package com.pulumi.components.acm.inputs;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import com.pulumi.core.Output;
// import com.pulumi.core.annotations.CustomType.Builder;
import com.pulumi.resources.ResourceArgs;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DnsValidatedCertificateArgs extends ResourceArgs {
	private Output<String> domainName;
    private Output<String> zoneId;
}
