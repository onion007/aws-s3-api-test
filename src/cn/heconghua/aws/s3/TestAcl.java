package cn.heconghua.aws.s3;

import com.amazonaws.services.s3.model.AccessControlList;

public class TestAcl
{
    public String name;
    public AccessControlList awsAcl;

    public TestAcl(String name)
    {
        this.name = name;
    }
    
    public TestAcl withAwsAcl(AccessControlList acl)
    {
        awsAcl = acl;
        return this;
    }
    
    public AccessControlList getAwsAcl()
    {
        return awsAcl;
    }
    
    public String toString()
    {
        return name;
    }
}
