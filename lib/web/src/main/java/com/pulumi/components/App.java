package com.pulumi.components.web;

import java.io.IOException;
import com.pulumi.provider.internal.Metadata;
import com.pulumi.provider.internal.ComponentProviderHost;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
		new ComponentProviderHost(new Metadata("web"), App.class.getPackage()).start(args);
    }
}

