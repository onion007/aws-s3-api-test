package cn.heconghua.aws.s3;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.CanonicalGrantee;

public class run
{
    static final String akidFull = "RW_ACCESS_KEY";
    static final String skidFull = "RW_SECRET_KEY";
    static final String akidRead = "R_ACCESS_KEY";
    static final String skidRead = "R_SECRET_KEY";
    static final String akidNone = "NONE_ACCESS_KEY";
    static final String skidNone = "NONE_SECRET_KEY";
    
    public static void testTitle(String title)
    {
        System.out.println("########################################");
        System.out.println("###  " + title);
        System.out.println("########################################");
    }
    
    public static void main(String[] args)
    {
        TestUser user1 = new TestUser("user1")
                .withCreateS3Client(akidFull, skidFull);
        TestUser user2 = new TestUser("user2")
                .withCreateS3Client(akidRead, skidRead);
        TestUser user3 = new TestUser("user3")
                .withCreateS3Client(akidNone, skidNone);
        
        Owner owner = user1.s3.getS3AccountOwner();
        
        List<TestAcl> lTestAcl = new ArrayList<TestAcl>();
        AccessControlList aclOwnerRW = new AccessControlList();
        aclOwnerRW.setOwner(owner);
        aclOwnerRW.grantPermission(new CanonicalGrantee(owner.getId()), Permission.Write);
        aclOwnerRW.grantPermission(new CanonicalGrantee(owner.getId()), Permission.Read);
        AccessControlList aclOwnerR = new AccessControlList();
        aclOwnerR.setOwner(owner);
        aclOwnerR.grantPermission(new CanonicalGrantee(owner.getId()), Permission.Read);
        AccessControlList aclAuthRW = new AccessControlList();
        aclAuthRW.setOwner(owner);
        aclAuthRW.grantPermission(GroupGrantee.AuthenticatedUsers, Permission.Write);
        aclAuthRW.grantPermission(GroupGrantee.AuthenticatedUsers, Permission.Read);
        AccessControlList aclAuthR = new AccessControlList();
        aclAuthR.setOwner(owner);
        aclAuthR.grantPermission(GroupGrantee.AuthenticatedUsers, Permission.Read);
        AccessControlList aclAllRW = new AccessControlList();
        aclAllRW.setOwner(owner);
        aclAllRW.grantPermission(GroupGrantee.AllUsers, Permission.Write);
        aclAllRW.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        AccessControlList aclAllR = new AccessControlList();
        aclAllR.setOwner(owner);
        aclAllR.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        AccessControlList aclNone = new AccessControlList();
        aclNone.setOwner(owner);
        aclNone.grantPermission(GroupGrantee.LogDelivery, Permission.ReadAcp);
        lTestAcl.add(new TestAcl("acl-owner-rw").withAwsAcl(aclOwnerRW));
        lTestAcl.add(new TestAcl("acl-owner-r").withAwsAcl(aclOwnerR));
        lTestAcl.add(new TestAcl("acl-authenticatedusers-rw").withAwsAcl(aclAuthRW));
        lTestAcl.add(new TestAcl("acl-authenticatedusers-r").withAwsAcl(aclAuthR));
        lTestAcl.add(new TestAcl("acl-all-rw").withAwsAcl(aclAllRW));
        lTestAcl.add(new TestAcl("acl-all-r").withAwsAcl(aclAllR));
        lTestAcl.add(new TestAcl("acl-none").withAwsAcl(aclNone));
        
        List<TestBucket> listTestBucket = new ArrayList<TestBucket>();
        for (TestAcl acl : lTestAcl)
        {
            String aclName = acl.name;
            String bucketName = "poc-bucket-" + aclName;
            TestBucket tBucket = new TestBucket(bucketName).withTestAcl(acl);
            listTestBucket.add(tBucket);
        }
        
        List<TestObject> listTestObject = new ArrayList<TestObject>();
        for (TestAcl acl : lTestAcl)
        {
            String aclName = acl.name;
            String objectName = "poc-object-" + aclName;
            TestObject tObject = new TestObject(objectName).withTestAcl(acl);
            listTestObject.add(tObject);
        }
        for (TestBucket testBucket : listTestBucket)
        {
            testBucket.addListTestObject(listTestObject);
        }
        

        user1.withListBucket(listTestBucket);
        user2.withListBucket(listTestBucket);
        user3.withListBucket(listTestBucket);

        
        
        // 基本功能测试
        testTitle("基本功能测试 -- aws用户(S3:RW)测试所有权限");
        user1.testCreateBucketsAndObjectsWithAcl();
        user1.testGetObjects();
        user1.testChangeStorageClass();
        user1.testDeleteBucketsAndObjects();
        user1.displayResult();
        user1.clearResult();
                
        
        // 权限写测试
        testTitle("权限写测试 -- 创建存储桶");
        user2.testCreateBuckets();
        user3.testCreateBuckets();
        user1.testDeleteBucketsAndObjects();
        
        testTitle("权限写测试 -- 创建文件");
        user1.testCreateBuckets();
        user2.testCreateObjects();
        user3.testCreateObjects();
        user1.testDeleteBucketsAndObjects();
        
        testTitle("权限写测试 -- 删除文件");
        user1.testCreateBucketsAndObjectsWithAcl();
        user2.testdeleteObjects();
        user3.testdeleteObjects();
        user1.testDeleteBucketsAndObjects();
        
        testTitle("存储类型更改测试");
        user1.testCreateBucketsAndObjectsWithAcl();
        user2.testChangeStorageClass();
        user3.testChangeStorageClass();
        user1.testDeleteBucketsAndObjects();
        
        
        // 权限读测试
        testTitle("权限读测试 -- 读取对象");
        user1.testCreateBucketsAndObjectsWithAcl();
        user2.testGetObjects();
        user3.testGetObjects();
        user1.testDeleteBucketsAndObjects();


        user2.displayResult();
        user3.displayResult();
        // Finish
        testTitle("Finish: 完成全部测试!!!");
    }
}
