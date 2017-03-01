package cn.heconghua.aws.s3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestBucket
{
    public String  name;
    public TestAcl acl;
    public List<TestObject> objects;
    public Map<String, Boolean> result = new HashMap<String, Boolean>();
    
    public TestBucket(String name)
    {
        this.name = name;
    }
    
    public TestBucket withTestAcl(TestAcl acl)
    {
        this.acl = acl;
        return this;
    }
    
    public String getName()
    {
        return name;
    }
    
    public TestAcl getTestAcl()
    {
        return acl;
    }
    
    public List<TestObject> getTestObject()
    {
        return objects;
    }
    
    public void addListTestObject(List<TestObject> listTestObject)
    {
        this.objects = listTestObject;
    }
    
    public String toString()
    {
        return name;
    }
}
