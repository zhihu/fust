package com.zhihu.fust.armeria.commons;

import io.netty.util.AttributeKey;

public abstract class RpcConst {

    public static final AttributeKey<String> PEER_SERVICE = AttributeKey.newInstance("peer.service");

}
