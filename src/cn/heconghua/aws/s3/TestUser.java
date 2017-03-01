package cn.heconghua.aws.s3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.StorageClass;

public class TestUser
{
    public String name;
    public List<TestBucket> buckets = new ArrayList<TestBucket>();
    boolean expect;
    List<String> result = new ArrayList<String>();
    
    public AmazonS3 s3 = null;
    
    private String getMethodName()
    {  
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();  
        StackTraceElement e = stacktrace[2];  
        String methodName = e.getMethodName();  
        return methodName;  
    }
    
    public TestUser(String name)
    {
        this.name = name;
    }
    
    private void messageInfo(String msg)
    {
        //System.out.println("[INFO]" + name + ": " + msg);
    }
    
    public TestUser addListBucket(List<TestBucket> listBucket)
    {
        buckets = listBucket;
        return this;
    }
    
    public TestUser withListBucket(List<TestBucket> listBucket)
    {
        buckets.addAll(listBucket);
        return this;
    }
    
    public TestUser withCreateS3Client(String accessKey, String secretKey)
    {
        s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.CN_NORTH_1)
                .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(accessKey, secretKey)))
                .build();
        return this;
    }
    
    public TestUser withExpect(boolean exp)
    {
        expect = exp;
        return this;
    }
    
    public boolean deleteObject(String bucketName, String objectName)
    {
        boolean retValue= false;
        try {
            s3.deleteObject(bucketName, objectName);
            retValue = true;
        } catch (AmazonServiceException e) {
        }
        return retValue;
    }
    
    public boolean deleteObject(TestBucket tBucket, TestObject tObject)
    {
        boolean retValue = deleteObject(tBucket.getName(), tObject.getName());
        addResult(getMethodName(), tBucket.getName() + "/" + tObject.getName(), retValue);
        return retValue;
        
    }
    
    public boolean createBucketwithAcl(String bucketName)
    {
        boolean retValue = false;
        try
        {
            s3.createBucket(bucketName);
            retValue = true;
        } catch (AmazonServiceException e)
        {
        }
        return retValue;
    }
    
    public boolean createBucketWithAcl(String bucketName, AccessControlList acl)
    {
        boolean retValue = false;
        try
        {
            s3.createBucket(new CreateBucketRequest(bucketName).withAccessControlList(acl));
            retValue = true;
        } catch (AmazonServiceException e)
        {
        }
        return retValue;
    }
    
    private void addResult(String op, String what, boolean retValue)
    {
        result.add(name + " | " + op + " | " + what + " | " + retValue);
    }
    
    public boolean createBucket(TestBucket tBucket)
    {
        boolean retValue = createBucketWithAcl(tBucket.getName(), tBucket.getTestAcl().getAwsAcl());
        addResult(getMethodName(), tBucket.getName(), retValue);
        return retValue;
    }
    
    public boolean deleteBucket(String bucketName)
    {
        boolean retValue = false;
        try
        {
            s3.deleteBucket(bucketName);
            retValue = true;
        } catch (AmazonServiceException e)
        {
        }
        return retValue;
    }
    
    public boolean deleteBucket(TestBucket tBucket)
    {
        boolean retValue = deleteBucket(tBucket.getName());
        addResult(getMethodName(), tBucket.getName(), retValue);
        return retValue;
    }
    
    public void testDeleteBuckets()
    {
        for (TestBucket tBucket : buckets)
        {
            deleteBucket(tBucket);
        }
    }
    
    public void testDeleteBucketsAndObjects()
    {
        for (TestBucket tBucket : buckets)
        {
            messageInfo("test delete bucket: " + tBucket);
            for (TestObject tObject : tBucket.objects)
            {
                messageInfo("   -> test delete object: " + tObject);
                deleteObject(tBucket, tObject);
            }
            deleteBucket(tBucket);
        }
    }
    
    public boolean createObjectWithAcl(String bucketName, String ObjectName, AccessControlList acl)
    {
        boolean retValue = false;
        try {
            s3.putObject(bucketName, ObjectName, ObjectName);
            if (acl != null)
            {
                AccessControlList acl1 = s3.getObjectAcl(bucketName, ObjectName);
                for (Grant g : acl.getGrantsAsList())
                {
                    acl1.grantPermission(g.getGrantee(), g.getPermission());
                }
                s3.setObjectAcl(bucketName, ObjectName, acl1);
            }
            retValue = true;
        } catch (AmazonServiceException e) {
            //System.err.println(e.getErrorMessage());
        }
        return retValue;
    }
    
    public boolean createObject(TestBucket tBucket, TestObject tObject)
    {
        boolean retValue = createObjectWithAcl(tBucket.getName(), tObject.getName(), null);
        addResult(getMethodName(), tBucket.getName() + "/" + tObject.getName(), retValue);
        return retValue;
    }
    
    public boolean createObjectWithAcl(TestBucket tBucket, TestObject tObject)
    {
        boolean retValue = createObjectWithAcl(tBucket.getName(), tObject.getName(), tObject.getTestAct().getAwsAcl());
        addResult(getMethodName(), tBucket.getName() + "/" + tObject.getName(), retValue);
        return retValue;
    }
    
    private void testCreateBucketsOrAndObjectWithAcl(boolean isCreateBucket, boolean isCreateObjects)
    {
        for (TestBucket tBucket : buckets)
        {
            if (isCreateBucket)
            {
                messageInfo("test create bucket: " + tBucket);
                createBucket(tBucket);
            }
            if (isCreateObjects)
            {
                for (TestObject tObject : tBucket.objects)
                {
                    messageInfo("   -> test create object: " + tBucket + "/" + tObject);
                    createObjectWithAcl(tBucket, tObject);
                }
            }
        }
    }
    
    private void testCreateBucketsOrAndObject(boolean isCreateBucket, boolean isCreateObjects)
    {
        for (TestBucket tBucket : buckets)
        {
            if (isCreateBucket)
            {
                messageInfo("test create bucket: " + tBucket);
                createBucket(tBucket);
            }
            if (isCreateObjects)
            {
                for (TestObject tObject : tBucket.objects)
                {
                    messageInfo("   -> test create object: " + tBucket + "/" + tObject);
                    createObject(tBucket, tObject);
                }
            }
        }
    }
    
    public void testCreateBuckets()
    {
        testCreateBucketsOrAndObject(true, false);
    }
    
    public void testCreateObjects()
    {
        testCreateBucketsOrAndObject(false, true);
    }
    
    public void testCreateBucketsAndObjects()
    {
        testCreateBucketsOrAndObject(true, true);
    }
    
    public void testCreateBucketsAndObjectsWithAcl()
    {
        testCreateBucketsOrAndObjectWithAcl(true, true);
    }
    
    public boolean getObject(String bucketName, String objectName)
    {
        boolean retValue = false;
        byte[] bufferByte = new byte[64];
        try {
            S3Object s3Object = s3.getObject(bucketName, objectName);
            S3ObjectInputStream s3IS = s3Object.getObjectContent();
            s3IS.read(bufferByte);
            s3IS.close();
            
            String bufferString = new String(bufferByte).trim();
            if (objectName.equals(bufferString))
            {
                retValue = true;
            }
            else
            {
                System.err.println("测试出错，验证object内容不一致: " + bucketName + "/" + objectName);
            }
        } catch (AmazonServiceException e){
        } catch (IOException e){
        }       
        return retValue;
    }
    
    public boolean getObject(TestBucket tBucket, TestObject tObject)
    {
        boolean retValue = getObject(tBucket.getName(), tObject.getName());
        addResult(getMethodName(), tBucket.getName() + "/" + tObject.getName(), retValue);
        return retValue;
    }
    
    public void testGetObjects()
    {
        for (TestBucket tBucket : buckets)
        {
            for (TestObject tObject : tBucket.objects)
            {
                messageInfo("   <- test get object: " + tBucket + "/" + tObject);
                getObject(tBucket, tObject);
            }
        }
    }
    
    public void testdeleteObjects()
    {
        for (TestBucket tBucket : buckets)
        {
            for (TestObject tObject : tBucket.objects)
            {
                messageInfo("   <- test get object: " + tBucket + "/" + tObject);
                deleteObject(tBucket, tObject);
            }
        }
    }
    
    public boolean changeStorageClass(String bucketName, String objectName, StorageClass sc)
    {
        boolean retValue = false;
        try {
            s3.copyObject(new CopyObjectRequest(bucketName, objectName, bucketName, objectName)
                .withStorageClass(sc.toString()));
            retValue = true;
        } catch (AmazonServiceException e) {
        }
        return retValue;
    }
    
    public boolean changeStorageClass(TestBucket tBucket, TestObject tObject, StorageClass sc)
    {
        return changeStorageClass(tBucket.getName(), tObject.getName(), sc);
    }
    
    public void testChangeStorageClass(StorageClass sc)
    {
        boolean retValue = false;
        for (TestBucket tBucket : buckets)
        {
            for (TestObject tObject : tBucket.objects)
            {
                retValue = changeStorageClass(tBucket, tObject, sc);
                addResult(getMethodName() + "->" + sc, tBucket.getName() + "/" + tObject.getName(), retValue);
            }
        }
    }
    
    public void testChangeStorageClass()
    {
        List<StorageClass> sClasses = Arrays.asList(StorageClass.Standard,
                StorageClass.StandardInfrequentAccess,
                StorageClass.ReducedRedundancy);
        for (StorageClass sc : sClasses)
        {
            messageInfo("test change Storage Class:" + sc);
            testChangeStorageClass(sc);
        }
    }
    
    public void displayResult()
    {
        for (String s : result)
        {
            System.out.println(s);
        }
    }
    
    public void clearResult()
    {
        result.clear();
    }
    
    public void test()
    {
        /*
        System.out.println(s3.getS3AccountOwner().getId());
        AccessControlList aclAllRW = new AccessControlList();
        aclAllRW.setOwner(s3.getS3AccountOwner());
        Grantee cg = new CanonicalGrantee(s3.getS3AccountOwner().getId());
        aclAllRW.grantPermission(cg, Permission.FullControl);
        aclAllRW.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        s3.createBucket(new CreateBucketRequest("ygabc").withAccessControlList(aclAllRW));
        System.out.println("ygabc");
        List<Grant> g = s3.getBucketAcl("ygabc").getGrantsAsList();
        for (Grant g1 : g)
        {
            System.out.println(g1.getGrantee().getIdentifier() +" | "+ g1.getPermission());
        }
        System.out.println("bucket-function-test2");
        g = s3.getBucketAcl("bucket-function-test2").getGrantsAsList();
        for (Grant g1 : g)
        {
            System.out.println(g1.getGrantee().getIdentifier() +" | "+ g1.getPermission());
        }
        */
    }
}
