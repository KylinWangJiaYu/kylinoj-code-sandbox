package com.kylin.kylinojcodesandbox.security;

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager{
    //检查所有的权限
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不做任何权限限制"+perm.toString());
        //super.checkPermission(perm);
        //throw new SecurityException("权限不足"+perm.getActions());
    }
}
