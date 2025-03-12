package com.zhihu.fust.armeria.grpc.client;

import com.linecorp.armeria.client.endpoint.EndpointGroup;

public interface EndpointGroupBuilder {
    EndpointGroup build(String targetName);
}
