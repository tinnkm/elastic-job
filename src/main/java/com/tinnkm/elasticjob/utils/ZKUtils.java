package com.tinnkm.elasticjob.utils;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.transaction.CuratorTransactionBridge;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * zk工具类，用完记得关闭连接
 */
@Slf4j
public class ZKUtils {
    private  ThreadLocal<CuratorFramework> threadLocal = new ThreadLocal<>();
    private final Map<String, TreeCache> caches = new HashMap<>();
    private String serverLists;
    private String namespace;
    private int baseSleepTimeMilliseconds;
    private int maxRetries;
    private int maxSleepTimeMilliseconds;
    private int sessionTimeoutMilliseconds;
    private int connectionTimeoutMilliseconds;
    private String digest;

    public ZKUtils(String serverLists, String namespace, int baseSleepTimeMilliseconds, int maxRetries, int maxSleepTimeMilliseconds, int sessionTimeoutMilliseconds, int connectionTimeoutMilliseconds, String digest) {
        this.serverLists = serverLists;
        this.namespace = namespace;
        this.baseSleepTimeMilliseconds = baseSleepTimeMilliseconds;
        this.maxRetries = maxRetries;
        this.maxSleepTimeMilliseconds = maxSleepTimeMilliseconds;
        this.sessionTimeoutMilliseconds = sessionTimeoutMilliseconds;
        this.connectionTimeoutMilliseconds = connectionTimeoutMilliseconds;
        this.digest = digest;
        init();
    }

    public ZKUtils(String serverLists, String namespace) {
        this.serverLists = serverLists;
        this.namespace = namespace;
        this.baseSleepTimeMilliseconds = 1000;
        this.maxRetries = 3;
        this.maxSleepTimeMilliseconds = 3000;
        init();
    }

    /**
     * 初始化客户端
     * @return 客户端
     */
    private void init(){
        if (null != threadLocal.get()){
            return ;
        }
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(serverLists).retryPolicy(new ExponentialBackoffRetry(baseSleepTimeMilliseconds, maxRetries, maxSleepTimeMilliseconds)).namespace(namespace);
        if (0 != sessionTimeoutMilliseconds) {
            builder.sessionTimeoutMs(sessionTimeoutMilliseconds);
        }

        if (0 != connectionTimeoutMilliseconds) {
            builder.connectionTimeoutMs(connectionTimeoutMilliseconds);
        }

        if (!Strings.isNullOrEmpty(digest)) {
            builder.authorization("digest", digest.getBytes(Charsets.UTF_8)).aclProvider(new ACLProvider() {
                @Override
                public List<ACL> getDefaultAcl() {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }

                @Override
                public List<ACL> getAclForPath(String path) {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }
            });
        }

        CuratorFramework client = builder.build();

        client.start();

        try {
            if (!client.blockUntilConnected(maxSleepTimeMilliseconds * maxRetries, TimeUnit.MILLISECONDS)) {
                client.close();
                throw new KeeperException.OperationTimeoutException();
            }
            threadLocal.set(client);
        } catch (Exception e) {
            log.error("初始化客户端失败",e);
        }
    }

    /**
     * 关闭客户端
     */
    public void close() {
        CuratorFramework curatorFramework = threadLocal.get();
        Iterator i$ = this.caches.entrySet().iterator();
        while(i$.hasNext()) {
            Map.Entry<String, TreeCache> each = (Map.Entry)i$.next();
            ((TreeCache)each.getValue()).close();
        }
        if (null != curatorFramework){
            this.waitForCacheClose();
            CloseableUtils.closeQuietly(curatorFramework);
            threadLocal.remove();
        }
    }

    private void waitForCacheClose() {
        try {
            Thread.sleep(500L);
        } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
        }

    }

    /**
     * 获取值
     * @param key key
     * @return 值
     */
    public String get(String key) {
        TreeCache cache = this.findTreeCache(key);
        if (null == cache) {
            return this.getDirectly(key);
        } else {
            ChildData resultInCache = cache.getCurrentData(key);
            if (null != resultInCache) {
                return null == resultInCache.getData() ? null : new String(resultInCache.getData(), Charsets.UTF_8);
            } else {
                return this.getDirectly(key);
            }
        }
    }

    private TreeCache findTreeCache(String key) {
        Iterator i$ = this.caches.entrySet().iterator();

        Map.Entry entry;
        do {
            if (!i$.hasNext()) {
                return null;
            }

            entry = (Map.Entry)i$.next();
        } while(!key.startsWith((String)entry.getKey()));

        return (TreeCache)entry.getValue();
    }

    public String getDirectly(String key) {
        try {
            CuratorFramework curatorFramework = threadLocal.get();
            return new String(curatorFramework.getData().forPath(key), Charsets.UTF_8);
        } catch (Exception var3) {
            return null;
        }
    }

    public List<String> getChildrenKeys(String key) {
        try {
            CuratorFramework curatorFramework = threadLocal.get();
            List result = curatorFramework.getChildren().forPath(key);
            Collections.sort(result, Comparator.reverseOrder());
            return result;
        } catch (Exception var3) {
            return Collections.emptyList();
        }
    }

    public int getNumChildren(String key) {
        try {
            CuratorFramework curatorFramework = threadLocal.get();
            Stat stat = curatorFramework.checkExists().forPath(key);
            if (null != stat) {
                return stat.getNumChildren();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return 0;
    }

    public boolean isExisted(String key) {
        try {
            CuratorFramework curatorFramework = threadLocal.get();
            return null != curatorFramework.checkExists().forPath(key);
        } catch (Exception var3) {
            return false;
        }
    }

    public void persist(String key, String value) {
        try {
            if (!this.isExisted(key)) {
                CuratorFramework curatorFramework = threadLocal.get();
                ((ACLBackgroundPathAndBytesable)curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)).forPath(key, value.getBytes(Charsets.UTF_8));
            } else {
                this.update(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void update(String key, String value) {
        try {
            CuratorFramework curatorFramework = threadLocal.get();
            curatorFramework.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(Charsets.UTF_8)).and().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void persistEphemeral(String key, String value) {
        try {
            CuratorFramework curatorFramework = threadLocal.get();
            if (this.isExisted(key)) {
                curatorFramework.delete().deletingChildrenIfNeeded().forPath(key);
            }

            ((ACLBackgroundPathAndBytesable)curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)).forPath(key, value.getBytes(Charsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String persistSequential(String key, String value) {
        try {
            CuratorFramework curatorFramework = threadLocal.get();
            return (String)((ACLBackgroundPathAndBytesable)curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)).forPath(key, value.getBytes(Charsets.UTF_8));
        } catch (Exception var4) {
            return null;
        }
    }

    public void persistEphemeralSequential(String key) {
        try {
            CuratorFramework curatorFramework = threadLocal.get();
            ((ACLBackgroundPathAndBytesable)curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)).forPath(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void remove(String key) {
        try {
            CuratorFramework curatorFramework = threadLocal.get();
            curatorFramework.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public long getRegistryCenterTime(String key) {
        long result = 0L;

        try {
            this.persist(key, "");
            CuratorFramework curatorFramework = threadLocal.get();
            result = ((Stat)curatorFramework.checkExists().forPath(key)).getMtime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Preconditions.checkState(0L != result, "Cannot get registry center time.");
        return result;
    }


    public void addCacheData(String cachePath) {
        CuratorFramework curatorFramework = threadLocal.get();
        TreeCache cache = new TreeCache(curatorFramework, cachePath);

        try {
            cache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.caches.put(cachePath + "/", cache);
    }

    public void evictCacheData(String cachePath) {
        TreeCache cache = this.caches.remove(cachePath + "/");
        if (null != cache) {
            cache.close();
        }

    }

    public Object getRawCache(String cachePath) {
        return this.caches.get(cachePath + "/");
    }

    /**
     * 切换命名空间
     * @param namespace
     * @return
     */
    public boolean usingNamespace(String namespace){
        CuratorFramework curatorFramework = threadLocal.get();
        CuratorFramework newCuratorFramework = curatorFramework.usingNamespace(namespace);
        if (null == newCuratorFramework){
            log.warn("can't find namespace will use old curatorFramework");
            return false;
        }
        threadLocal.set(newCuratorFramework);
        return true;
    }

    /**
     * 创建命名空间
     * @param namespace
     * @return
     */
    public boolean createNamespace(String namespace){
        CuratorFramework curatorFramework = threadLocal.get();
        try {
            curatorFramework.createContainers(namespace);
        } catch (Exception e) {
            log.error("create namespace failed",e);
            return false;
        }
        return true;
    }
}
