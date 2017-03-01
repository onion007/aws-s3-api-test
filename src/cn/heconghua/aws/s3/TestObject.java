package cn.heconghua.aws.s3;

public class TestObject
{
    public String  name;
    public TestAcl acl;
    
    public TestObject(String name)
    {
        this.name = name;
    }
    
    public TestObject withTestAcl(TestAcl acl)
    {
        this.acl = acl;
        return this;
    }
    
    public String toString()
    {
        return name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public TestAcl getTestAct()
    {
        return acl;
    }
}
