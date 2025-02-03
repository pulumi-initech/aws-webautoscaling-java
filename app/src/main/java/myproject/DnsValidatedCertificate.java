package myproject;

import com.pulumi.aws.acm.Certificate;
import com.pulumi.aws.acm.CertificateArgs;
import com.pulumi.aws.acm.CertificateValidation;
import com.pulumi.aws.acm.CertificateValidationArgs;
import com.pulumi.aws.route53.Record;
import com.pulumi.aws.route53.RecordArgs;
import com.pulumi.core.Either;
import com.pulumi.core.Output;
import com.pulumi.resources.ComponentResource;
import com.pulumi.resources.ComponentResourceOptions;
import com.pulumi.resources.CustomResourceOptions;
import java.util.List;
import java.util.Map;

public class DnsValidatedCertificate extends ComponentResource {

  public final Output<String> certificateArn;

  public DnsValidatedCertificate(
      String name, String domainName, Output<String> zoneId, ComponentResourceOptions options) {
    super("custom:resource:DnsValidatedCertificate", name, options);

    var cert =
        new Certificate(
            "default",
            new CertificateArgs.Builder().domainName(domainName).validationMethod("DNS").build(),
            CustomResourceOptions.builder().parent(this).build());

    Record certValidationRecord =
        new Record(
            domainName + "-valid",
            new RecordArgs.Builder()
                .name(
                    cert.domainValidationOptions()
                        .apply(o -> Output.of(o.getFirst().resourceRecordName().get())))
                .records(
                    cert.domainValidationOptions()
                        .apply(o -> Output.of(List.of(o.getFirst().resourceRecordValue().get()))))
                .type(
                    cert.domainValidationOptions()
                        .apply(
                            o -> Output.of(Either.ofLeft(o.getFirst().resourceRecordType().get()))))
                .ttl(60)
                .zoneId(zoneId)
                .build(),
            CustomResourceOptions.builder().parent(this).build());

    var certCertifcateValidation =
        new CertificateValidation(
            "cert",
            new CertificateValidationArgs.Builder()
                .certificateArn(cert.arn())
                .validationRecordFqdns(
                    certValidationRecord.fqdn().apply(fqdn -> Output.of(List.of(fqdn))))
                .build(),
            CustomResourceOptions.builder().parent(cert).build());

    this.certificateArn = certCertifcateValidation.certificateArn();

    this.registerOutputs(Map.of("certificateArn", this.certificateArn));
  }
}
