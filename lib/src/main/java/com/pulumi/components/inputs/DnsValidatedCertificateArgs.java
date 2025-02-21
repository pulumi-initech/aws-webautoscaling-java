package com.pulumi.components.inputs;

import lombok.Getter;

import com.pulumi.core.Output;
import com.pulumi.resources.ResourceArgs;

@Getter
public class DnsValidatedCertificateArgs extends ResourceArgs {
    private Output<String> domainName;
    private Output<String> zoneId;
    private DnsValidatedCertificateArgs() {}
}
